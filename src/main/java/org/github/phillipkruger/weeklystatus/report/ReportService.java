package org.github.phillipkruger.weeklystatus.report;

import org.github.phillipkruger.weeklystatus.github.GitHubService;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.github.phillipkruger.weeklystatus.mail.Mailer;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

/**
 * Create a report
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@RequestScoped
public class ReportService {
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig()
            .withFormatting(true));
 
    @PersistenceContext
    EntityManager em;
    
    @Inject
    GitHubService gitHubService;
    
    @Inject
    Engine engine;
    
    @Inject
    Template report;
    
    @Inject
    Mailer mailer;
    
    public void createReportAndSendEmail(@ObservesAsync ReportEvent event) throws IOException{
        Report report = createReport(event.getToken(), event.getEmail(), event.getRepositories());
        emailReport(report);
    }
    
    public Report createReport(String token, String email, List<String> repositories) throws IOException {
        repositories = repositories.stream().map(String::trim).collect(Collectors.toList());
        
        GitHub gitHub = gitHubService.getGitHub(token);
        GHMyself myself = gitHub.getMyself();

        Map<String, Set<Done>> dones = getDones(myself, repositories);
        Map<String, Set<Todo>> todos = getTodos(gitHub, repositories);
        
        List<Repo> repos = new ArrayList<>();
        for(Map.Entry<String, Set<Done>> done:dones.entrySet()){
            Repo repo = new Repo();
            repo.setName(done.getKey());
            repo.setDone(done.getValue());

            if(todos.containsKey(done.getKey())){
                Set<Todo> todoInRepo = todos.remove(done.getKey());
                repo.setTodo(todoInRepo);
            }
            repos.add(repo);
        }

        if(!todos.isEmpty()){
            for(Map.Entry<String, Set<Todo>> todo:todos.entrySet()){
                Repo repo = new Repo();
                repo.setName(todo.getKey());
                repo.setTodo(todo.getValue());
                repos.add(repo);
            }
        }
        return new Report(email, myself.getName(), repos, getReportDate());
    }
    
    @Transactional
    public void saveReport(Report report){
        
        em.persist(report);
        em.flush();
    }
    
    @Transactional    
    public List<Report> getHistory(String email){
        Query historyQuery = em.createQuery("SELECT r FROM Report r WHERE r.email=:email");
        historyQuery.setParameter("email", email);
        return (List<Report>)historyQuery.getResultList();
    }
    
    public void emailReport(Report r) throws IOException{
        Map<String, Set<Done>> dones = new HashMap<>();
        Map<String, Set<Todo>> todos = new HashMap<>();
        for(Repo repo : r.getRepos()){
            if(repo.getDone()!=null && !repo.getDone().isEmpty()){
                dones.put(repo.getName(), repo.getDone());
            }
            if(repo.getTodo()!=null && !repo.getTodo().isEmpty()){
                todos.put(repo.getName(), repo.getTodo());
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyy");
        String subject = "Weekly Status: " + sdf.format(yesterday()) + " : " + r.getName();
        String mailBody = report.data("name", r.getName())
                .data("dones", dones)
                .data("todos", todos)
                .data("counter", new Counter())
                .render();

        mailer.send(r.getEmail(), subject, mailBody);
    }
    
    private Map<String, Set<Done>> getDones(GHUser user, List<String> repositories) throws IOException {    
        Map<String, Set<Done>> dones = new HashMap<>();
        PagedIterable<GHEventInfo> listEvents = user.listEvents();
        for(GHEventInfo event:listEvents){
            if(event.getType().equals(GHEvent.PULL_REQUEST)){
                if(event.getCreatedAt().after(weekAgo()) && event.getCreatedAt().before(new Date())){
                    GHEventPayload.PullRequest pullRequestEvent = event.getPayload(GHEventPayload.PullRequest.class);
                    GHPullRequest pullRequest = pullRequestEvent.getPullRequest();
                    if(pullRequest.getUser().getId()==user.getId()){
                        if(repositories.contains(event.getRepository().getFullName())){
                            String repo = event.getRepository().getName();
                            Done done = new Done(pullRequest.getTitle(), pullRequest.getHtmlUrl().toString());
                            Set<Done> doneInRepo = dones.getOrDefault(repo, new HashSet<>());
                            doneInRepo.add(done);
                            dones.put(repo, doneInRepo);
                        }
                    }
                }
            }    
        }
        return dones;
    } 
    
    private Map<String, Set<Todo>> getTodos(GitHub gitHub, List<String> repositories) throws IOException{
        Map<String, Set<Todo>> todos = new HashMap<>();
        for(String repo:repositories){
            try {
                GHRepository repository = gitHub.getRepository(repo);
                List<GHIssue> issues = repository.getIssues(GHIssueState.OPEN);
                for(GHIssue issue:issues){
                    if(issue.getAssignees().contains(gitHub.getMyself())){
                        Todo todo = new Todo(issue.getTitle(), issue.getHtmlUrl().toString());
                        Set<Todo> todoInRepo = todos.getOrDefault(repository.getName(), new HashSet<>());
                        todoInRepo.add(todo);
                        todos.put(repository.getName(), todoInRepo);
                    }
                }
            }catch(org.kohsuke.github.GHFileNotFoundException nfe){
                System.err.println("Repo not found: " + repo + " [" + gitHub.getMyself().getName() + "]");
            }
        }
        return todos;
    }
    
    private LocalDate getReportDate(){
        Date yesterday = yesterday();
        return yesterday.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }
    
    private Date yesterday(){
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }
    
    private Date weekAgo(){
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_MONTH, -8);
        return cal.getTime();
    }

}