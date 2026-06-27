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

## `@JoinColumn`

Names the FK column that stores the association. Goes on the **owning side** (the side whose
table holds the FK). Omit it and Hibernate defaults the name to `<field>_<referenced-pk>`.

```java
// AuthorProfile.java — owning side
@OneToOne
@JoinColumn(name = "author_id")  // FK column on the AuthorProfile table
private Author author;
```

## What goes in `mappedBy`

The **field name on the owning side** (the one with `@JoinColumn`) — a Java field name, not a
column name. It tells the inverse side "the FK is already mapped over there," so no second column
is created.

```java
// Author.java — inverse side
@OneToOne(mappedBy = "author")   // ← AuthorProfile.author (the field), not "author_id"
private AuthorProfile profile;
```

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

### The `optional = false` "trick" is a myth (for separate-FK mappings)

A common claim is that `optional = false` makes a `@OneToOne` lazy. With a separate FK column it
does **not**:

- **Owning side** — lazy already works with the default `optional = true` (the FK value is on the
  row, so Hibernate can proxy and defer). `optional` adds nothing to laziness here.
- **Inverse (`mappedBy`) side** — `optional = false` does **not** enable lazy. Verified in this
  project: with `optional = false` the profile still loaded eagerly (and N+1). There is no FK
  value on the Author row to build a proxy from, so Hibernate must query regardless.

So the flag affects laziness on neither side here — it mainly controls the `NOT NULL` constraint /
whether the association is required. The only real fix for inverse-side lazy is bytecode
enhancement. (The trick has *some* basis for shared-primary-key mappings via `@MapsId` /
`@PrimaryKeyJoinColumn`, which is a different case.)

## EAGER does not mean "join" — it won't avoid N+1 for you

Hibernate won't intelligently add a join fetch for eager associations to avoid N+1. The
behaviour depends entirely on *how* you load the entity:

- **Load by id** (`session.find`/`get`): an `EAGER` association *is* pulled in via a single
  SELECT with a join.
- **Load via a query** (`select a from Author a`): Hibernate runs the query, then fires **one
  extra SELECT per row** to satisfy each eager association — the classic N+1. EAGER *causes*
  this; it doesn't prevent it.

`@Fetch(FetchMode.JOIN)` is **ignored for HQL/JPQL queries** — it only affects the load-by-id
path, so it won't rescue a multi-row query either.

For query-based loading the join has to come from the **query**, not the mapping. Reliable
options (keep the mapping `LAZY`):

- **`join fetch`** — `select a from Author a join fetch a.profile`. Emits an *inner* join, so
  rows with a null association are dropped (use `left join fetch` to keep them).
- **Entity graph** — keeps the JPQL generic and lets the caller decide per-query; emits a
  *left* join:

  ```java
  var graph = session.createEntityGraph(Author.class);
  graph.addAttributeNode("profile");
  session.createSelectionQuery("select a from Author a", Author.class)
          .setEntityGraph(graph, GraphSemantic.FETCH)
          .getResultList();
  ```

- **`@BatchSize` / `hibernate.default_batch_fetch_size`** — stays lazy but collapses the N
  follow-up selects into a few `IN (...)` batches.
