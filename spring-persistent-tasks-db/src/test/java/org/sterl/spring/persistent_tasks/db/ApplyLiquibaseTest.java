package org.sterl.spring.persistent_tasks.db;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

class ApplyLiquibaseTest {

    @Test
    void test() throws Exception {
        var dataSource = mssqlDb();
        try (Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(
                        new liquibase.database.jvm.JdbcConnection(dataSource.getConnection()))) {

            database.setDatabaseChangeLogTableName("PT_databasechangelog");
            database.setDatabaseChangeLogLockTableName("PT_databasechangeloglock");

            // Specify the changeset file location
            String changeLogFile = "spring-persistent-tasks/db.changelog-master.xml";

            new CommandScope("update").addArgumentValue("changelogFile", changeLogFile)
                    .addArgumentValue("database", database) //
                    // .addArgumentValue("contexts", null) //
                    // .addArgumentValue("databaseChangeLogTableName", "PT_databasechangelog") //
                    // .addArgumentValue("databaseChangeLogLockTableName",
                    // "PT_databasechangeloglock") //
                    .execute();
        }

        var template = new JdbcTemplate(dataSource);
        final List<Map<String, Object>> var = template.queryForList("SHOW TABLES");
        for (Map<String, Object> map : var) {
            System.err.println(map);
        }
    }

    private JdbcDataSource h2Db() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:db-test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("admin");
        dataSource.setPassword("password");
        return dataSource;
    }

    public static DataSource mssqlDb() {
        // Create and configure SQLServerDataSource
        SQLServerDataSource dataSource = new SQLServerDataSource();
        dataSource.setServerName("localhost");
        dataSource.setPortNumber(1433);
        dataSource.setTrustServerCertificate(true); // Accept self-signed certificates
        dataSource.setUser("sa");
        dataSource.setPassword("veryStrong123");

        return dataSource;
    }
}
