package org.github.phillipkruger.weeklystatus.report;

import java.util.List;

/**
 * Create an event
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReportEvent {
    private String token;
    private String email; 
    private List<String> repositories;

    public ReportEvent() {
    }

    public ReportEvent(String token, String email, List<String> repositories) {
        this.token = token;
        this.email = email;
        this.repositories = repositories;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }
    
    
}
