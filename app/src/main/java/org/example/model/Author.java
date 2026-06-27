package org.example.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * An author.
 *
 * <p>Demonstrates a {@code @OneToOne} to {@link AuthorProfile} — one author has one profile.
 */
@Entity
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Owning side of the one-to-one: the FK column (profile_id) lives on the Author table.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private AuthorProfile profile;

    protected Author() {
        // Required no-arg constructor for Hibernate.
    }

    public Author(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AuthorProfile getProfile() {
        return profile;
    }

    public void setProfile(AuthorProfile profile) {
        this.profile = profile;
    }
}
