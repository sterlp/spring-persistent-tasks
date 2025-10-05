# Liquibase DB setup

## maven Setup

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-db</artifactId>
    <version>${spt.version}</version>
</dependency>
```

## Spring Setup

```yaml
spring:
    liquibase:
        change-log: classpath:db/changelog/db.changelog-master.xml
```

## Liquibase setup

`db.changelog-master.xml`

```xml
<?xml version="1.1" encoding="UTF-8" standalone="no"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
    xmlns:pro="http://www.liquibase.org/xml/ns/pro"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd
                        http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-pro-latest.xsd
                        http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">


    <!-- any includes -->
    <include file="spring-persistent-tasks/db.changelog-master.xml" />
    <!-- any includes -->

</databaseChangeLog>
```
