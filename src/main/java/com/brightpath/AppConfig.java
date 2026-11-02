package com.brightpath;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * All runtime config comes from the environment so one artifact (WAR or image)
 * serves every customer. Each setting is read from an env var first, then a JVM
 * system property (handy for setenv.sh on a plain EC2 Tomcat install).
 */
public final class AppConfig {

    private static final String VERSION = loadVersion();

    static {
        // Drivers bundled in WEB-INF/lib are not auto-registered by Tomcat's
        // webapp classloader, so load it explicitly.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AppConfig() {}

    static String get(String name, String fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public static String customerName() {
        return get("CUSTOMER_NAME", "local");
    }

    public static String version() {
        return VERSION;
    }

    public static String gitSha() {
        return get("GIT_SHA", "unknown");
    }

    public static Connection connect() throws SQLException {
        String url = get("DB_URL", "jdbc:mysql://localhost:3306/brightpath");
        String user = get("DB_USER", "brightpath");
        String password = get("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    private static String loadVersion() {
        try (InputStream in = AppConfig.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            if (in != null) {
                props.load(in);
            }
            return props.getProperty("app.version", "unknown");
        } catch (IOException e) {
            return "unknown";
        }
    }
}
