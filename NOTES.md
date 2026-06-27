# Hibernate Findings

## Owning side

The **owning side** of a relationship is the side that has the foreign key. It's the entity
**without** `mappedBy` — the side with `mappedBy` is the inverse (non-owning) side. Hibernate
writes the FK based on the owning side, so changes to the inverse side alone aren't persisted.

## Inverse side

The **inverse side** is the non-owning side — the one marked with `mappedBy`. It does **not**
hold the foreign key; `mappedBy = "x"` points at the field `x` on the owning entity that does.
It exists for navigation (e.g. reading the other side of a bidirectional link), but updating it
alone won't change the database — you must set the owning side for the FK to be persisted.

## Lazy-fetching a one-to-one

`@OneToOne` defaults to `EAGER`. You can make it `LAZY`, but it only works cleanly on the
**owning side** (the side that holds the FK): the FK value is on the entity's own row, so
Hibernate can hand back a proxy and defer the query until the association is first accessed.

On the inverse (`mappedBy`) side, lazy doesn't work without bytecode enhancement — Hibernate
has to run a query just to know whether the related row exists, so it loads it up front anyway.

In this project `Author` is the owning side (FK `profile_id` on the Author table), so lazy works:

```java
// Author.java — owning side, FK on Author table → LAZY works
@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
private AuthorProfile profile;
```

```java
// AuthorProfile.java — inverse side, no back-reference (unidirectional author → profile)
@Entity
public class AuthorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bio;
    private String websiteUrl;
    // ...
}
```

Result: `select a from Author a` issues **no** query against `AuthorProfile`; the profile is
only fetched when `author.getProfile()` is first called.

### The `optional` flag and lazy

The `optional` flag matters much more on the inverse side: an inverse-side `@OneToOne` with
`optional = true` can never be lazy (Hibernate must query to learn if the row exists), whereas
`optional = false` lets it assume presence. On your owning side, it's mainly about the
`NOT NULL` constraint.
