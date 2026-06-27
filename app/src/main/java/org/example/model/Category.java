package org.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * A genre/tag that can apply to many {@link Book} values.
 *
 * <p>This is the <em>inverse</em> side of the many-to-many: {@code mappedBy = "categories"}
 * points at the owning collection on {@link Book}. There is no second join table — both sides
 * share the one declared on {@code Book}.
 */
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "categories")
    private Set<Book> books = new HashSet<>();

    protected Category() {
        // Required no-arg constructor for Hibernate.
    }

    public Category(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Book> getBooks() {
        return books;
    }
}
