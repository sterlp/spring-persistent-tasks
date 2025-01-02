package org.sterl.test;

import static org.junit.jupiter.api.Assertions.fail;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import jakarta.persistence.EntityManager;

public class HibernateAsserts {
    private final Statistics statistics;

    public HibernateAsserts(EntityManager entityManager) {
        try (Session session = entityManager.unwrap(org.hibernate.Session.class)) {
            @SuppressWarnings("resource")
            SessionFactory factory = session.getSessionFactory();
            factory.getStatistics().setStatisticsEnabled(true);
            statistics = factory.getStatistics();
        }
    }

    public HibernateAsserts assertTrxCount(int expected) {
        long value = statistics.getTransactionCount();
        if (value != expected) {
            logSummary();
            fail("Expected " + expected + " TransactionCount, but found " + value);
        }
        return this;
    }

    public HibernateAsserts assertInsertCount(int expected) {
        long value = statistics.getEntityInsertCount();
        if (value != expected) {
            logSummary();
            fail("Expected " + expected + " EntityInsertCount, but found " + value);
        }
        return this;
    }

    public void reset() {
        statistics.clear();
    }

    public void logSummary() {
        statistics.logSummary();
    }
}
