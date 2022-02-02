package org.github.phillipkruger.weeklystatus.report;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * a weekly report
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Entity
public class Report implements Serializable {
    static final long serialVersionUID = 42L;
    
    private Long id;
    private LocalDate date;
    private String email;
    private String name;
    private List<Repo> repos;
    
    public Report() {
    }
    
    public Report(String email, String name, List<Repo> repos, LocalDate date) {
        this.email = email;
        this.name = name;
        this.repos = repos;
        this.date = date;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.FALSE)
    public List<Repo> getRepos() {
        return repos;
    }

    public void setRepos(List<Repo> repos) {
        this.repos = repos;
    }
}
