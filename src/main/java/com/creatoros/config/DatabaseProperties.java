package com.creatoros.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Connection settings for every database the application talks to, keyed by a logical name.
 *
 * <pre>
 * app.db.creatoros.url=jdbc:mysql://localhost:3306/creatoros
 * app.db.creatoros.username=root
 * app.db.creatoros.password=root
 * </pre>
 *
 * <p>Only {@code creatoros} is configured today. Because this is a map, adding an analytics or
 * audit database is a matter of adding a new {@code app.db.<name>.*} block plus a persistence unit
 * in {@link DatabaseConfig} - no change to this class.
 */
@ConfigurationProperties("app")
@Getter
@Setter
public class DatabaseProperties {

    /** Logical database name to its connection settings. */
    private Map<String, DataSourceSettings> db = new LinkedHashMap<>();

    /**
     * @throws IllegalStateException when a config class asks for a database that has no
     *                               {@code app.db.<name>.*} block, which is clearer than a
     *                               NullPointerException during context startup
     */
    public DataSourceSettings require(String name) {
        DataSourceSettings settings = db.get(name);
        if (settings == null) {
            throw new IllegalStateException(
                    "No datasource configured for '%s' - add app.db.%s.* to application.properties"
                            .formatted(name, name));
        }
        return settings;
    }

    @Getter
    @Setter
    public static class DataSourceSettings {

        private String url;
        private String username;
        private String password;
        private String driverClassName = "com.mysql.cj.jdbc.Driver";

        /** Hikari sizing, per database, so a reporting store can be pooled differently. */
        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private long connectionTimeoutMs = 30_000L;
    }
}
