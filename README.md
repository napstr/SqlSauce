# SqlSauce

[![Release](https://jitpack.io/v/space.npstr/SqlSauce.svg?style=flat-square)](https://jitpack.io/#space.npstr/SqlSauce) release  
[![Build Status Master Branch](https://img.shields.io/travis/napstr/SqlSauce/master.svg?style=flat-square)](https://travis-ci.org/napstr/SqlSauce/branches) master branch  
[![Build Status Development Branch](https://img.shields.io/travis/napstr/SqlSauce/dev.svg?style=flat-square)](https://travis-ci.org/napstr/SqlSauce/branches) dev branch  


SQL database stack I use between various projects.

Setting up and configuring database and ORM stuff has been identified by me as a major pain in the ass so this project has two main goals:
- Choose the best all-around tools and use them conveniently
- Strip away crazy config stuff with sane defaults and zero xml files

Bonus goal: Avoid using Spring cause I personally do not like it.


## Tooling

The chosen tools include
- [PostgreSQL](postgresql.org/) as the underlying database 
- [JPA](https://en.wikipedia.org/wiki/Java_Persistence_API) as the ORM framework
- [Hibernate](http://hibernate.org/orm/) as the ORM vendor
- [HikariCP](https://github.com/brettwooldridge/HikariCP) as the connection pool


Reasons for the choices:
- PostgreSQL because it's an _amazing_ jack of all trades database
- JPA and Hibernate as de-facto industry standards just have a lot of documentation available. Also proper care when writing your models will result in Hibernate's autoddl taking care of setting up and migrating your schema.
- HikariCP because just look at their benchmarks
 
 
This package is meant to use these specific tools to their fullest, e.g. there is and will be code specific for these chosen tools.

This package is not meant to make these tools exchangeable.


## Features

![alt text](https://i.imgur.com/CuzucQL.gif "SHOW ME WHAT YOU GOT")

- Ssh tunnel support with reconnect handling. Stay safe, don't expose your databases.

- Sauced Entities:
  Aware of their source, bringing convenience methods to handle them outside of transactions and persist them whenever needed
  
- Migrations:
  A really crude, one-way support to do entity level migrations. Assuming hibernate autoddl to create necessary columns and tables.
Sure, you can also run raw SQL queries.


## Adding SqlSauce to your project
Add through the [JitPack](https://jitpack.io/) repo to your project:

###### Gradle build.gradle
```gradle
    repositories {
        maven { url 'https://jitpack.io' }
    }

    dependencies {
        compile group: 'space.npstr', name: 'SqlSauce', version: '0.0.2'
    }

```

###### Maven pom.xml
```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

    <dependency>
        <groupId>space.npstr</groupId>
        <artifactId>SqlSauce</artifactId>
        <version>0.0.2</version>
    </dependency>
```

## Usage

Short descriptions of how to get started using this.

Using this package will create a table called "hstorex" in your database, which is used to persist
migration data, and any other Hstore entities that you might use.
Make sure your code isnt using a table with the same name for anything different.


Please also check out the comments and docs inside of the classes.
They will always be more detailed / up to date than this README.

### DatabaseConnection

Create a connection:

```java
DatabaseConnection databaseConnection = new DatabaseConnection.Builder("postgres",
     "jdbc:postgresql://localhost:5432/db?user=me&password=highlysecurepw")
        .addEntityPackage("space.npstr.db.entities")
        .setAppName("My Super Special App_Production"))
        .setSshDetails(new SshTunnel.SshDetails("db.example.com", "me")
                         .setSshPort(22)
                         .setLocalPort(1111)
                         .setRemotePort(5432)
                         .setKeyFile("my_secret_key_rsa")
                         .setPassphrase("anotherhighlysecurepw")
        )
        .build();
```

After creating the connection you get access to EntityManagers through `DatabaseConnection#getEntityManager`. Remember
to close them after using them!


### DatabaseWrapper

The `DatabaseWrapper` provides methods to execute plain SQL and JPQL queries, and also loading IEntities/SaucedEntites by id/class.

Creating one is pretty straight forward after setting up the connection:
```java
    DatabaseWrapper dbWrapper = new DatabaseWrapper(databaseConnection);
```

The DatabaseWrapper handles opening and closing EntityManagers and just getting stuff out of the database so you don't have to go through the hassle.

### SaucedEntities

Have your entities extend the abstract SaucedEntity. A SaucedEntity, loaded with the DatabaseWrapper, is aware of its data source.
This allows the class to provide convenience methods like `save()` to be merged back into the db it came from after you are done working with it.

The implementation of IEntity also makes sure you use proper ids for your entities. Remember that autogenerated ids only show up in your entity after you have 
written them to the database.


### Migrations

Create a package where you are going to drop your migration classes. It is important that once created, to never changed their class names
(changing packages is fine), as the class names will be used to identify whether a migration has already been run or not.

```java
    Migrations mainDbMigrations = new Migrations();
    mainDbMigrations.registerMigration(new m0000SqliteToPostgresUpvotes());
    mainDbMigrations.registerMigration(new m0001SqliteToPostgresGames());
    mainDbMigrations.runMigrations(databaseConnection);
```

Run this code before proceeding with the start of your app.
The data about migrations that have been run is saved in an Hstore entity.


## Additional Modules

[Discord Entities](https://github.com/napstr/SqlSauce/blob/master/sqlsauce-discord-entities/README.md)


## Changelog

### v0.0.2
- Initial release of Discord Entities module with Proof of Concept for Guilds and Users + JDA listeners to cache them
- Support plain SQL queries with result mapping
- Actually enable Hibernate statistics collection
- Set the Dialect to be used in the DatabaseConnection. This is needed in some cases when the automagic recognition fails due to several Dialects are present in the class path.
- Add an HStore converter that accepts Null values. Default HStore converter converts null values into empty maps.
- Better naming for some methods (more concise and adhering to general conventions)
- More annotations of all kinds
- Changed a wrong package name
- Writes and Reads for entities are secured by a cheap lock implementation. The acquiring of the lock can be overriden with custom implementations (lock striping, anyone?)
- Static default Sauce is set for all Sauced Entities whenever a DatabaseConnection is created - works great with single-connection apps
- Unwrap the DatabaseConnection from the DatabaseWrapper
- Less strict persist method, accepting IEntities instead of SaucedEntities

### v0.0.1
- Initial release

## TODOs

- test whether this can actually run more than one connection at a time
- improve security of the ssh tunnels
- make migrations part of the database creation instead of manually handling them
- 2nd level cache support (ehcache?)
- make entity locks less of a hack and more of a feature
