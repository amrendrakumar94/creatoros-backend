package com.creatoros.config;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.hibernate.HibernateTransactionManager;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableTransactionManagement
@Slf4j
public class DatabaseConfig {

    /** Entities belonging to the main database. */
    private static final String MAIN_ENTITY_PACKAGE = "com.creatoros.entity";

    @Value("${spring.main.datasource.driver-class-name}")
    private String              mainDriverClassName;

    @Value("${spring.main.datasource.url}")
    private String              mainUrl;

    @Value("${spring.main.datasource.username}")
    private String              mainUsername;

    @Value("${spring.main.datasource.password}")
    private String              mainPassword;

    @Value("${spring.main.datasource.maximum-pool-size:20}")
    private int                 mainMaximumPoolSize;

    @Value("${spring.main.datasource.minimum-idle:5}")
    private int                 mainMinimumIdle;

    @Value("${spring.main.datasource.connection-timeout-ms:60000}")
    private long                mainConnectionTimeoutMs;

    @Value("${spring.jpa.hibernate.ddl-auto:validate}")
    private String              hibernateDdlAuto;

    @Value("${spring.jpa.show-sql:false}")
    private String              hibernateShowSql;

    @Value("${spring.jpa.properties.hibernate.format_sql:true}")
    private String              hibernateFormatSql;

    @Bean(name = "mainDataSource")
    @Primary
    public DataSource getMainDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(mainDriverClassName);
        hikariConfig.setJdbcUrl(mainUrl);
        hikariConfig.setUsername(mainUsername);
        hikariConfig.setPassword(mainPassword);
        hikariConfig.setMaximumPoolSize(mainMaximumPoolSize);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("main-mysql-pool");
        hikariConfig.setConnectionTimeout(mainConnectionTimeoutMs);
        hikariConfig.setMinimumIdle(mainMinimumIdle);

        log.info("Configured main datasource -> {}", mainUrl);
        return new HikariDataSource(hikariConfig);
    }

    @Bean(name = "mainSessionFactory")
    @Primary
    public SessionFactory getMainSessionFactory(@Qualifier("mainDataSource") DataSource dataSource,
            ObjectProvider<FlywayMigrationInitializer> flywayInitializer) {

        flywayInitializer.getIfAvailable();

        LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(dataSource);
        sessionBuilder.scanPackages(MAIN_ENTITY_PACKAGE);
        sessionBuilder.setProperty("hibernate.hbm2ddl.auto", hibernateDdlAuto);
        sessionBuilder.setProperty("hibernate.show_sql", hibernateShowSql);
        sessionBuilder.setProperty("hibernate.format_sql", hibernateFormatSql);
        return sessionBuilder.buildSessionFactory();
    }

    @Bean(name = "mainTransactionManager")
    @Primary
    public HibernateTransactionManager getMainTransactionManager(@Qualifier("mainSessionFactory") SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }
}
