/*
 * Hibernate entity-relationship demo.
 *
 * Boots an in-memory H2 database, creates the schema from the annotated entities, inserts a
 * small sample dataset, then runs queries that exercise each relationship type. Run it with:
 *
 *     ./gradlew :app:run
 */
package org.example;

import jakarta.persistence.criteria.JoinType;
import org.example.model.Author;
import org.example.model.AuthorProfile;
import org.example.model.Book;
import org.example.model.Category;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class App {

    public static void main(String[] args) {
        try (SessionFactory sessionFactory = buildSessionFactory()) {
            seedData(sessionFactory);
            runQueries(sessionFactory);
        }
    }

    /**
     * Builds a SessionFactory wired to an in-memory H2 database. {@code create-drop} rebuilds the
     * schema from the entity annotations on startup, so there is nothing to set up by hand.
     */
    private static SessionFactory buildSessionFactory() {
        return new Configuration()
                .addAnnotatedClass(Author.class)
                .addAnnotatedClass(AuthorProfile.class)
                .addAnnotatedClass(Book.class)
                .addAnnotatedClass(Category.class)
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:library;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.format_sql", "true")
                .buildSessionFactory();
    }

    /** Inserts two authors, their profiles, books, and shared categories. */
    private static void seedData(SessionFactory sessionFactory) {
        sessionFactory.inTransaction(session -> {
            Category fantasy = new Category("Fantasy");
            Category scifi = new Category("Sci-Fi");
            Category classic = new Category("Classic");
            session.persist(fantasy);
            session.persist(scifi);
            session.persist(classic);

            Author tolkien = new Author("J.R.R. Tolkien");
            tolkien.setProfile(new AuthorProfile("Author of Middle-earth.", "https://tolkien.example"));
            Book hobbit = new Book("The Hobbit");
            hobbit.addCategory(fantasy);
            hobbit.addCategory(classic);
            Book lotr = new Book("The Lord of the Rings");
            lotr.addCategory(fantasy);
            tolkien.addBook(hobbit);
            tolkien.addBook(lotr);

            Author leguin = new Author("Ursula K. Le Guin");
            leguin.setProfile(new AuthorProfile("Author of Earthsea and the Hainish cycle.", "https://leguin.example"));
            Book lefthand = new Book("The Left Hand of Darkness");
            lefthand.addCategory(scifi);
            lefthand.addCategory(classic);
            leguin.addBook(lefthand);

            // Cascade ALL on Author.books and Author.profile means persisting the author
            // also persists its profile and books (and the join-table rows for categories).
            session.persist(tolkien);
            session.persist(leguin);
        });
    }

    private static void runQueries(SessionFactory sessionFactory) {
        sessionFactory.inTransaction(session -> {
            heading("@OneToMany / @ManyToOne — each author and their books");
            // JOIN FETCH pulls authors and their books in one query, avoiding the N+1 problem
            // you'd hit by lazily walking author.getBooks() in a loop.
            session.createSelectionQuery(
                            "select distinct a from Author a left join fetch a.books order by a.name",
                            Author.class)
                    .getResultList()
                    .forEach(author -> {
                        System.out.println(author.getName());
                        author.getBooks().forEach(b -> System.out.println("    - " + b.getTitle()));
                    });

            heading("@OneToOne — author profiles");
            session.createSelectionQuery(
                            "select a from Author a join fetch a.profile order by a.name", Author.class)
                    .getResultList()
                    .forEach(author ->
                            System.out.println(author.getName() + " — " + author.getProfile().getBio()));

            heading("@ManyToMany — books in the 'Classic' category");
            // Navigate from the inverse side (Category.books) using a join on the collection.
            session.createSelectionQuery(
                            "select b.title from Category c join c.books b where c.name = :name order by b.title",
                            String.class)
                    .setParameter("name", "Classic")
                    .getResultList()
                    .forEach(title -> System.out.println("    - " + title));

            heading("@ManyToMany (other direction) — categories of 'The Hobbit'");
            session.createSelectionQuery(
                            "select c.name from Book b join b.categories c where b.title = :title order by c.name",
                            String.class)
                    .setParameter("title", "The Hobbit")
                    .getResultList()
                    .forEach(name -> System.out.println("    - " + name));

            heading("Criteria API — same author+books query, built programmatically");
            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(Author.class);
            var root = query.from(Author.class);
            root.fetch("books", JoinType.LEFT);
            query.select(root).distinct(true).orderBy(cb.asc(root.get("name")));
            session.createSelectionQuery(query)
                    .getResultList()
                    .forEach(author -> System.out.println(
                            author.getName() + " (" + author.getBooks().size() + " books)"));
        });
    }

    private static void heading(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
