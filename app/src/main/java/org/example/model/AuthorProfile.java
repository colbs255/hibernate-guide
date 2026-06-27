package org.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Extra biographical detail for an {@link Author}, split into its own table.
 *
 * <p>This is the <em>inverse</em> side of the {@code @OneToOne}. The FK lives on the
 * {@code Author} table (see {@link Author#getProfile()}), so this class holds no reference
 * back — a common, simple choice when you only navigate author → profile.
 */
@Entity
public class AuthorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bio;

    private String websiteUrl;

    protected AuthorProfile() {
        // Required no-arg constructor for Hibernate.
    }

    public AuthorProfile(String bio, String websiteUrl) {
        this.bio = bio;
        this.websiteUrl = websiteUrl;
    }

    public Long getId() {
        return id;
    }

    public String getBio() {
        return bio;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }
}
