package org.github.phillipkruger.weeklystatus.mail;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class Mailer {
    
    @ConfigProperty(name = "quarkus.mailer.password")
    String apiKey;
            
    public void send(String to, String subject, String htmlBody) throws IOException{
        
        Email fromEmail = new Email();
        fromEmail.setName("Weekly status");
        fromEmail.setEmail("status@phillip-kruger.com");
        
        Email toEmail = new Email(to);
        
        Content content = new Content();
        content.setType("text/html");
        content.setValue(htmlBody);
        
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }
    
    
}
