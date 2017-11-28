# Discord Entities for SqlSauce

A package of Discord related entities and classes for [SqlSauce](https://github.com/napstr/SqlSauce).

![alt text](https://i.imgur.com/FgnBhVR.gif "Discord API robo mech stuff gif")

Current main purpose: Provide a framework to save settings for users / guilds combined with keeping some data on them,
like names or avatar urls etc.


## Adding SqlSauce Discord Entities to your project

Make sure to include the core package [SqlSauce](https://github.com/napstr/SqlSauce) in your project.
This module can then be added through this additional dependency

###### Gradle build.gradle
```groovy
    dependencies {
        compile group: 'space.npstr.SqlSauce', name: 'discord-entities', version: '0.0.3-SNAPSHOT'
    }

```

###### Maven pom.xml
```xml
    <dependency>
        <groupId>space.npstr.SqlSauce</groupId>
        <artifactId>discord-entities</artifactId>
        <version>0.0.3-SNAPSHOT</version>
    </dependency>
```


## Usage

Make sure to read the documentation of [SqlSauce](https://github.com/napstr/SqlSauce) as this package relies on many of its concepts.

Example class:

```java
   @Entity(name = "MyGuildSettings")
   @Table(name = "guild_settings")
   public class MyGuildSettings extends DiscordGuild<MyGuildSettings> {
   
       @Column(name = "prefix")
       private String prefix = "!";
   
       //for JPA / SaucedEntity
       public MyGuildSettings() {
       }
   
       public static String getPrefix(long guildId) throws DatabaseException {
           return load(guildId, MyGuildSettings.class).getPrefix();
       }
   
       public static MyGuildSettings setPrefix(long guildId, String prefix) throws DatabaseException {
           return load(guildId, MyGuildSettings.class)
                   .setPrefix(prefix)
                   .save();
       }
   
       public String getPrefix() {
           return prefix;
       }
   
       public MyGuildSettings setPrefix(String prefix) {
           this.prefix = prefix;
           return this;
       }
   }
```

Setting the entity up to be automatically cached with [JDA](https://github.com/DV8FromTheWorld/JDA):

```java
        JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT)
                [...]
                .addEventListener(new GuildCachingListener<>(MyGuildSettings.class))
                [...];
```


## Changelog

### v0.0.2
- Initial release with Proof of Concept for Guilds and a JDA listener


## TODOs

- static getters should not load the entity and instead look up the field by a direct query
- provide better support for mass syncing data on shard creation. this would allow this to be used as a full cache of discord entities
- indices?


## Dependencies

- **Java Discord API**
  - [GitHub](https://github.com/DV8FromTheWorld/JDA)
  - [The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)
  - [JCenter](https://bintray.com/dv8fromtheworld/maven/JDA)