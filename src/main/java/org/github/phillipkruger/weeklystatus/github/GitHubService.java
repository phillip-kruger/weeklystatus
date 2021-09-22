package org.github.phillipkruger.weeklystatus.github;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import okhttp3.OkHttpClient;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;

/**
 * Facade on the GitHub operations
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GitHubService {

    public GitHub getGitHub(String token) throws IOException{
        return GitHubBuilder.fromEnvironment()
                    .withConnector(new OkHttpConnector(new OkHttpClient()))
                    .withOAuthToken(token)
                    .build();
    }
    
    public GHMyself getMyself(String token) throws IOException {
        GitHub gitHub = getGitHub(token);
        return gitHub.getMyself();
    }
    
}
