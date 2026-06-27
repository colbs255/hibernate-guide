/*
 * Hibernate entity-relationship demo.
 *
 * Boots an in-memory H2 database, creates the schema from the annotated entities, inserts a
 * small sample dataset, then runs a query that exercises the @OneToOne relationship. Run it with:
 *
 *     ./gradlew :app:run
 */
package org.example;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.example.model.Author;
import org.example.model.AuthorProfile;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class App {

    public static void main(String[] args) {
        // With no SLF4J backend on the classpath, Hibernate logs through java.util.logging at
        // INFO. Raise the threshold to WARNING to silence the chatty startup banner/DDL notices.
        Logger.getLogger("org.hibernate").setLevel(Level.WARNING);

        try (SessionFactory sessionFactory = buildSessionFactory()) {
            seedData(sessionFactory);
            runQueries(sessionFactory);
        }
    }

    /**
     * Builds a SessionFactory wired to an in-memory H2 database. {@code create-drop} rebuilds the
     * schema from the entity annotations on startup, so there is nothing to set up by hand.
     *
     * <p>Connections are served by HikariCP via Hibernate's {@code HikariCPConnectionProvider}.
     * The {@code hibernate.hikari.*} properties are passed straight through to HikariCP; for a
     * single-threaded demo a tiny pool is plenty, but the same knobs scale to production.
     */
    private static SessionFactory buildSessionFactory() {
        return new Configuration()
                .addAnnotatedClass(Author.class)
                .addAnnotatedClass(AuthorProfile.class)
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:library;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty(
                        "hibernate.connection.provider_class",
                        "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
                .setProperty("hibernate.hikari.minimumIdle", "2")
                .setProperty("hibernate.hikari.maximumPoolSize", "10")
                .setProperty("hibernate.hikari.idleTimeout", "30000")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.format_sql", "true")
                .buildSessionFactory();
    }

    /** Inserts a couple of authors, each with a profile. */
    private static void seedData(SessionFactory sessionFactory) {
        sessionFactory.inTransaction(session -> {
            Author tolkien = new Author("J.R.R. Tolkien");
            tolkien.setProfile(new AuthorProfile("Author of Middle-earth.", "https://tolkien.example"));

            Author leguin = new Author("Ursula K. Le Guin");
            leguin.setProfile(new AuthorProfile("Author of Earthsea and the Hainish cycle.", "https://leguin.example"));

            // Cascade ALL on Author.profile means persisting the author also persists its profile.
            session.persist(tolkien);
            session.persist(leguin);
        });
    }

    private static void runQueries(SessionFactory sessionFactory) {
        sessionFactory.inTransaction(session -> {
            heading("@OneToOne — author profiles");
            var x = session.createSelectionQuery(
                            "select a from Author a", Author.class)
                    .getResultList();
            x.forEach(v -> System.out.println(v.getProfile().getBio()));
        });
    }

    private static void heading(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
