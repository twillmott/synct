# Synct
An visual sync tool to copy media to storage based on trakt watched and collected progress

## Database Migration
Database migration is handled by liquibase. The liquibase migration scripts should be generated from the applications entities.

When modifying or adding a new entity, create a new database migration script by doing the following.

Run a liquibase diff. This diff uses the migration H2 database file as defined in the `liquibase.properties` file.

```
mvn package
mvn liquibase:diff
```

The above command will generate a new changelog file (`src/main/java/resources/db/changelog/changes/rename-me.yaml`). This new changelog file should be renamed to fit the naming strategy of other files.

A new entry in 'db.changelog-master.yaml' should then be made to include the new migration change log.

Finally, we need to apply the new migration to the migration h2 database file: 

```
mvn liquibase:update
```

Make sure you commit the migration db files.

This process could be automated like in [this article](https://www.sipios.com/blog-tech/generate-spring-boot-migrations-from-hibernate-entities)