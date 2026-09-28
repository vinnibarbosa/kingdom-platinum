package com.br.pokefichas.domain.core.ficha.usecase;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

class MemorialMigrationTest {

    @Test
    void migratesExistingDatabaseWithoutUsingOtherSitesHistory() throws Exception {
        final String url = "jdbc:h2:mem:memorial_migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE fichas (id BIGINT PRIMARY KEY)");
            statement.execute("CREATE TABLE flyway_schema_history (version VARCHAR(50))");
            statement.execute("INSERT INTO flyway_schema_history (version) VALUES ('043')");
        }

        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .table("flyway_schema_history_kp")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("42"))
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            var result = statement.executeQuery("SELECT COUNT(*) FROM flyway_schema_history_kp WHERE version = '043' AND success = TRUE");
            assertThat(result.next()).isTrue();
            assertThat(result.getInt(1)).isEqualTo(1);
            result.close();

            result = statement.executeQuery("SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'fichas' AND column_name = 'falecida'");
            assertThat(result.next()).isTrue();
            assertThat(result.getInt(1)).isEqualTo(1);
            result.close();

            result = statement.executeQuery("SELECT COUNT(*) FROM flyway_schema_history WHERE version = '043'");
            assertThat(result.next()).isTrue();
            assertThat(result.getInt(1)).isEqualTo(1);
        }
    }
}
