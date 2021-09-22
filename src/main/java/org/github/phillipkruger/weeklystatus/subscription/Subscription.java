package org.github.phillipkruger.weeklystatus.subscription;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;

/**
 * Subscribe to Status reports
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Entity
public class Subscription extends PanacheEntity {
    public String email;
    public String token;
    @ElementCollection(fetch = FetchType.EAGER, targetClass=String.class)
    public List<String> repositories;
    
    public static Subscription findByEmail(String email){
        return find("email", email).firstResult();
    }
}
