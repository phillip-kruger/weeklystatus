package org.github.phillipkruger.weeklystatus.cron;

import org.github.phillipkruger.weeklystatus.subscription.Subscription;
import org.github.phillipkruger.weeklystatus.subscription.SubscriptionService;
import org.github.phillipkruger.weeklystatus.report.Report;
import io.quarkus.scheduler.Scheduled;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.github.phillipkruger.weeklystatus.report.ReportService;

/**
 * The weekly cron sending all the reports
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Singleton
public class WeeklyCron {
 
    @Inject
    SubscriptionService subscriptionService;
    
    @Inject
    ReportService reportService;
    
    @Scheduled(cron = "{cron.expr}")
    void cron() {
        List<Subscription> subscriptions = subscriptionService.subscriptions();
        for(Subscription s:subscriptions){
            try {
                Report report = reportService.createReport(s.token, s.email, s.repositories);
                reportService.emailReport(report);
                reportService.saveReport(report);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }   
    }
}
