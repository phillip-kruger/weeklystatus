package org.github.phillipkruger.weeklystatus;

import org.github.phillipkruger.weeklystatus.subscription.Subscription;
import org.github.phillipkruger.weeklystatus.subscription.SubscriptionService;
import org.github.phillipkruger.weeklystatus.report.Report;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.github.phillipkruger.weeklystatus.report.ReportService;

/**
 * Service Endpoint
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Path("")
public class WeeklyStatusApi {

    @Inject 
    ReportService reportService;
    
    @Inject
    SubscriptionService subscriptionService;
    
    @POST
    @Path("/createReport")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createReport(@NotNull @FormParam("token") String token, 
            @NotNull @FormParam("email") String email, 
            @NotNull @FormParam("repositories") String repositories) {
        try {
            Report report = reportService.createReport(token, email, toList(repositories));
            reportService.emailReport(report);
            return Response.accepted().build();
        }catch(Throwable t){
            return Response.serverError().header("reason", t.getMessage()).build();
        }
    }
    
    @POST
    @Path("/subscribe")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public void subscribe(@NotNull @FormParam("token") String token, 
            @NotNull @FormParam("email") String email, 
            @NotNull @FormParam("repositories") String repositories) throws Exception{
        subscriptionService.subscribe(email, token, toList(repositories));
    }
    
    @POST
    @Path("/unsubscribe")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public void unsubscribe(@NotNull @FormParam("email") String email) throws Exception{
        subscriptionService.unsubscribe(email);
    }
    
    @GET
    @Path("/subscriptions")
    @Produces(MediaType.TEXT_PLAIN)
    public String subscriptions() throws Exception{
        try(StringWriter sw = new StringWriter()){
            for(Subscription s:subscriptionService.subscriptions()){
                sw.write(s.email);
                sw.write("\n");
            }
            return sw.toString();
        }
    }
    
    @POST
    @Path("/history")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<Report> history(@NotNull @FormParam("email") String email) throws Exception{
        return reportService.getHistory(email);
    }
    
    private List<String> toList(String repos){
        return Arrays.asList(repos.split(","));
    }
}
