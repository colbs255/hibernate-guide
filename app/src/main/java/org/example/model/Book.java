package org.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

/**
 * A book, written by one {@link Author} and tagged with many {@link Category} values.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>{@code @ManyToOne} to {@link Author} — the <em>owning</em> side of the author/books
 *       relationship. The {@code author_id} FK column lives on this table.</li>
 *   <li>{@code @ManyToMany} to {@link Category} — owning side, mapped via a join table.</li>
 * </ul>
 */
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // Owning side: this is where the author_id FK lives. LAZY (the default for @ManyToOne is
    // actually EAGER) is set explicitly so the author is only loaded when first accessed.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    // Owning side of the many-to-many. @JoinTable names the link table and its two FK columns.
    // The inverse side (Category.books) just points back with mappedBy.
    @ManyToMany
    @JoinTable(
            name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    protected Book() {
        // Required no-arg constructor for Hibernate.
    }

    public Book(String title) {
        this.title = title;
    }

    /** Adds a category and syncs both sides of the many-to-many link. */
    public void addCategory(Category category) {
        categories.add(category);
        category.getBooks().add(this);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Set<Category> getCategories() {
        return categories;
    }
}
