package com.ticketsystem.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Singleton wrapper around Hibernate's {@link SessionFactory}.
 * The SessionFactory is expensive to create — we build it once and reuse it.
 */
public final class HibernateUtil {

    private static SessionFactory sessionFactory;

    private HibernateUtil() {}

    /**
     * Returns the singleton SessionFactory, initialising it on first call.
     */
    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                sessionFactory = new Configuration()
                        .configure("hibernate.cfg.xml")
                        .buildSessionFactory();
            } catch (Exception e) {
                System.err.println("Błąd inicjalizacji Hibernate: " + e.getMessage());
                System.err.println("Upewnij się, że baza danych MySQL jest uruchomiona (docker-compose up -d)");
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }

    /**
     * Shuts down the SessionFactory gracefully.
     * Should be called on application exit.
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
