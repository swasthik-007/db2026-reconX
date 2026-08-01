package com.dbtraining.reconx.observability;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

/**
 * ============================================================================
 * TICKET-ADV059 — DatabaseHealthIndicator (timed SELECT 1)
 *
 * WHAT:    Custom actuator HealthIndicator that runs SELECT 1 with a
 *          2-second timeout and reports query latency.
 *
 * HOW:     Extends AbstractHealthIndicator and registers as reconxDatabase.
 *
 * WHY:     Default DataSource health checks only validate connection creation.
 *          This executes SQL and measures database responsiveness.
 *
 * OBSERVE: GET /api/actuator/health shows:
 *
 *          "reconxDatabase": {
 *              "status": "UP",
 *              "details": {
 *                  "query": "SELECT 1",
 *                  "elapsedMs": 5
 *              }
 *          }
 * ============================================================================
 */
@Component("reconxDatabase")
public class DatabaseHealthIndicator extends AbstractHealthIndicator {

    private static final String QUERY = "SELECT 1";
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final DataSource ds;

    public DatabaseHealthIndicator(DataSource ds) {
        super("ReconX database health check failed");
        this.ds = ds;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        long start = System.nanoTime();

        try (Connection c = ds.getConnection();
             Statement s = c.createStatement()) {

            s.setQueryTimeout((int) TIMEOUT.toSeconds());

            try (ResultSet rs = s.executeQuery(QUERY)) {
                rs.next();
            }

            long elapsedMs =
                    (System.nanoTime() - start) / 1_000_000;

            builder.up()
                    .withDetail("query", QUERY)
                    .withDetail("elapsedMs", elapsedMs);

        } catch (SQLException e) {

            builder.down(e)
                    .withDetail("query", QUERY);
        }
    }
}