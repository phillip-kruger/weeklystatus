package org.github.phillipkruger.weeklystatus.report;

import java.io.Serializable;
import java.util.Set;
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
 * What is done and planed for a certain repo
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Entity
public class Repo implements Serializable {
    static final long serialVersionUID = 43L;
    
    private Long id;
    private String name;
    private Set<Done> done;
    private Set<Todo> todo;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.FALSE)
    public Set<Done> getDone() {
        return done;
    }

    public void setDone(Set<Done> done) {
        this.done = done;
    }

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.FALSE)
    public Set<Todo> getTodo() {
        return todo;
    }

    public void setTodo(Set<Todo> todo) {
        this.todo = todo;
    }
    
}
