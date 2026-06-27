package org.example.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

/**
 * An author who writes books.
 *
 * <p>Demonstrates two relationship types:
 * <ul>
 *   <li>{@code @OneToMany} to {@link Book} — one author has many books. This is the
 *       <em>inverse</em> (non-owning) side: {@code mappedBy = "author"} tells Hibernate the
 *       foreign key lives on the {@code Book} table, owned by {@link Book#getAuthor()}.</li>
 *   <li>{@code @OneToOne} to {@link AuthorProfile} — one author has one profile.</li>
 * </ul>
 */
@Entity
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Inverse side: no FK column here. Cascade + orphanRemoval means saving/deleting an
    // Author propagates to its Books. orphanRemoval deletes a Book once it leaves this list.
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Book> books = new ArrayList<>();

    // Owning side of the one-to-one: the FK column (profile_id) lives on the Author table.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AuthorProfile profile;

    protected Author() {
        // Required no-arg constructor for Hibernate.
    }

    public Author(String name) {
        this.name = name;
    }

    /**
     * Adds a book and keeps both sides of the bidirectional link in sync. Always update
     * relationships through helpers like this — Hibernate persists the FK based on the
     * <em>owning</em> side ({@code book.author}), so forgetting to set it leaves the column null.
     */
    public void addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
    }

    public void removeBook(Book book) {
        books.remove(book);
        book.setAuthor(null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Book> getBooks() {
        return books;
    }

    public AuthorProfile getProfile() {
        return profile;
    }

    public void setProfile(AuthorProfile profile) {
        this.profile = profile;
    }
}
