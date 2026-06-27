/*
 * Prints the CREATE TABLE DDL Hibernate would generate for the entities, without connecting to
 * a real database. Writes to build/schema.sql and echoes it to stdout. Run it with:
 *
 *     ./gradlew :app:schema
 */
package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import org.example.model.Author;
import org.example.model.AuthorProfile;
import org.example.model.Book;
import org.example.model.Category;
import org.hibernate.cfg.Configuration;

public class SchemaDump {

    public static void main(String[] args) throws Exception {
        Path target = Path.of("build/schema.sql");
        Files.createDirectories(target.getParent());

        // Standard JPA schema-generation properties: emit the CREATE script to a file. Only the
        // dialect is needed — no JDBC connection is opened to produce the script.
        Configuration cfg = new Configuration()
                .addAnnotatedClass(Author.class)
                .addAnnotatedClass(AuthorProfile.class)
                .addAnnotatedClass(Book.class)
                .addAnnotatedClass(Category.class)
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("jakarta.persistence.schema-generation.scripts.action", "create")
                .setProperty("jakarta.persistence.schema-generation.scripts.create-target", target.toString())
                .setProperty("hibernate.hbm2ddl.delimiter", ";")
                .setProperty("hibernate.format_sql", "true");

        // Building the SessionFactory triggers script generation; close it right away.
        cfg.buildSessionFactory().close();

        System.out.println();
        System.out.println("=== Generated schema (also written to " + target + ") ===");
        System.out.println(Files.readString(target));
    }
}
