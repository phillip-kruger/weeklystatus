package org.github.phillipkruger.weeklystatus.subscription;

import io.quarkus.qute.Template;
import java.io.IOException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.github.phillipkruger.weeklystatus.github.GitHubService;
import org.github.phillipkruger.weeklystatus.mail.Mailer;
import org.kohsuke.github.GHMyself;

@ApplicationScoped
public class SubscriptionService {

    @Inject
    Template subscribe;
    
    @Inject
    Template unsubscribe;
    
    @Inject
    Mailer mailer;
    
    @Inject
    GitHubService gitHubService;
    
    @Transactional
    public void subscribe(String email, String token, List<String> repoList) throws IOException{
        Subscription subscription = Subscription.findByEmail(email);
        if(subscription==null){
            subscription = new Subscription();
            subscription.email = email;    
        }
        subscription.token = token;
        subscription.repositories = repoList;
        subscription.persistAndFlush();
        
        // Also send an email
        GHMyself myself = gitHubService.getMyself(subscription.token);
        emailSubscription(email, myself.getName(), repoList);
    }

    @Transactional
    public Subscription unsubscribe(String email) throws IOException {
        Subscription subscription = Subscription.findByEmail(email);
        if(subscription!=null){
            subscription.delete();
            
            // Also send an email
            GHMyself myself = gitHubService.getMyself(subscription.token);
            emailUnsubscribe(subscription.email, myself.getName(), subscription.repositories);
        }
        return subscription;
    }
    
    public List<Subscription> subscriptions(){
        return Subscription.findAll().list();
    }
    
    private void emailSubscription(String emailAddress, String name, List<String> repositories) throws IOException {
        String mailBody = subscribe.data("name", name)
                .data("repositories", repositories)
                .render();

        mailer.send(emailAddress, "Subscribe", mailBody);
    }
    
    private void emailUnsubscribe(String emailAddress, String name, List<String> repositories) throws IOException {
        String mailBody = unsubscribe.data("name", name)
                .data("repositories", repositories)
                .render();

        mailer.send(emailAddress, "Unsubscribe", mailBody);
    }
    
}
