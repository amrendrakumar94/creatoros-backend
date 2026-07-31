package com.creatoros.config;

import com.creatoros.config.DatabaseProperties.DataSourceSettings;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateSettings;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Explicit persistence wiring, one unit per database.
 *
 * <p>
 * Defining a {@link DataSource} and a
 * {@link LocalContainerEntityManagerFactoryBean} here makes Spring Boot's
 * datasource and Hibernate JPA auto-configuration back off, which is the point:
 * with several databases there is no single "the" datasource for Boot to guess
 * at. {@code creatoros} is marked {@link Primary} so unqualified injection and
 * Flyway keep resolving to it.
 *
 * <h2>Adding another database (analytics, audit, ...)</h2>
 * <ol>
 * <li>Add an {@code app.db.<name>.*} block to application.properties.</li>
 * <li>Put its entities in their own package, e.g.
 * {@code com.creatoros.entity.audit}, and keep them out of
 * {@code com.creatoros.entity} so the units do not overlap.</li>
 * <li>Copy the three beans below, dropping {@code @Primary} and giving each a
 * distinct name:
 * 
 * <pre>
 * &#64;Bean
 * DataSource auditDataSource() {
 *     return buildDataSource("audit");
 * }
 *
 * &#64;Bean
 * LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(&#64;Qualifier("auditDataSource") DataSource ds) {
 *     return buildEntityManagerFactory(ds, "audit", "com.creatoros.entity.audit");
 * }
 *
 * &#64;Bean
 * PlatformTransactionManager auditTransactionManager(&#64;Qualifier("auditEntityManagerFactory") EntityManagerFactory emf) {
 *     return new JpaTransactionManager(emf);
 * }
 * </pre>
 * 
 * </li>
 * <li>Give its DAOs {@code @PersistenceContext(unitName = "audit")} and
 * annotate their services {@code @Transactional("auditTransactionManager")} -
 * the unqualified {@code @Transactional} always means the primary unit, so a
 * non-primary unit that omits the qualifier will silently run outside the
 * intended transaction.</li>
 * <li>Flyway auto-configuration only migrates the primary datasource. A second
 * database needs its own migration runner pointed at its own
 * {@code db/migration/<name>} location.</li>
 * </ol>
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties({ DatabaseProperties.class, JpaProperties.class, HibernateProperties.class })
@RequiredArgsConstructor
@Slf4j
public class DatabaseConfig {

    /** Persistence unit name for the primary application database. */
    public static final String        CREATOROS_UNIT = "creatoros";

    private final DatabaseProperties  databaseProperties;
    private final JpaProperties       jpaProperties;
    private final HibernateProperties hibernateProperties;

    // ---------- creatoros: the primary unit ----------

    @Primary
    @Bean
    public DataSource creatorOsDataSource() {
        return buildDataSource(CREATOROS_UNIT);
    }

    /**
     * @param flywayInitializer resolved purely for ordering:
     *            {@code ddl-auto=validate} would fail against an unmigrated
     *            database, and hand-wiring this factory loses the dependency
     *            Boot would otherwise add between the two
     */
    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("creatorOsDataSource") DataSource dataSource,
            ObjectProvider<FlywayMigrationInitializer> flywayInitializer) {

        flywayInitializer.getIfAvailable();
        return buildEntityManagerFactory(dataSource, CREATOROS_UNIT, "com.creatoros.entity");
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    // ---------- reusable builders, shared by every unit ----------

    /** Builds a pooled DataSource from the {@code app.db.<name>.*} block. */
    protected DataSource buildDataSource(String name) {
        DataSourceSettings settings = databaseProperties.require(name);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName(name + "-pool");
        dataSource.setJdbcUrl(settings.getUrl());
        dataSource.setUsername(settings.getUsername());
        dataSource.setPassword(settings.getPassword());
        dataSource.setDriverClassName(settings.getDriverClassName());
        dataSource.setMaximumPoolSize(settings.getMaximumPoolSize());
        dataSource.setMinimumIdle(settings.getMinimumIdle());
        dataSource.setConnectionTimeout(settings.getConnectionTimeoutMs());

        log.info("Configured datasource '{}' -> {}", name, settings.getUrl());
        return dataSource;
    }

    /**
     * Builds a Hibernate-backed persistence unit over one datasource.
     *
     * <p>
     * Hibernate settings still come from the standard {@code spring.jpa.*}
     * keys, so behaviour (ddl-auto, show-sql, formatting) is unchanged from
     * before this wiring became explicit.
     */
    protected LocalContainerEntityManagerFactoryBean buildEntityManagerFactory(DataSource dataSource, String unitName, String... packagesToScan) {

        Map<String, Object> properties = hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName(unitName);
        factory.setPackagesToScan(packagesToScan);
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(properties);
        return factory;
    }
}
