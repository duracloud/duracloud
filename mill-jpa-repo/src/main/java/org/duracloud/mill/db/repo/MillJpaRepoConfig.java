/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.db.repo;

import java.text.MessageFormat;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.duracloud.mill.manifest.ManifestStore;
import org.duracloud.mill.manifest.jpa.JpaManifestStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
@Configuration
@EnableJpaRepositories(basePackages = { "org.duracloud.mill" }, 
                       entityManagerFactoryRef = MillJpaRepoConfig.ENTITY_MANAGER_FACTORY_BEAN, 
                       transactionManagerRef = MillJpaRepoConfig.TRANSACTION_MANAGER_BEAN)
@EnableTransactionManagement
public class MillJpaRepoConfig {
    private static final String MILL_REPO_ENTITY_MANAGER_FACTORY_BEAN =
        "millRepoEntityManagerFactory";
    public static final String MILL_REPO_DATA_SOURCE_BEAN =
        "millRepoDataSource";
    public static final String TRANSACTION_MANAGER_BEAN =
        "millJpaRepoTransactionManager";
    public static final String ENTITY_MANAGER_FACTORY_BEAN =
        MILL_REPO_ENTITY_MANAGER_FACTORY_BEAN;

    @Bean(name = MILL_REPO_DATA_SOURCE_BEAN, destroyMethod = "close")
    public BasicDataSource millRepoDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(MessageFormat.format("jdbc:mysql://{0}:{1}/{2}?autoReconnect=true" +
        		                                "&characterEncoding=utf8" +
        		                                "&characterSetResults=utf8",
                                               System.getProperty("mill.db.host","localhost"),
                                               System.getProperty("mill.db.port","3306"),
                                               System.getProperty("mill.db.name", "mill")));
        dataSource.setUsername(System.getProperty("mill.db.user", "mill"));
        dataSource.setPassword(System.getProperty("mill.db.pass", "password"));
        return dataSource;
    }

    @Bean(name=TRANSACTION_MANAGER_BEAN)
    public PlatformTransactionManager
        millRepoTransactionManager(@Qualifier(MILL_REPO_ENTITY_MANAGER_FACTORY_BEAN) EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager tm =
            new JpaTransactionManager(entityManagerFactory);
        tm.setJpaDialect(new HibernateJpaDialect());
        return tm;
    }

    @Bean(name = MILL_REPO_ENTITY_MANAGER_FACTORY_BEAN)
    public LocalContainerEntityManagerFactoryBean
        millRepoEntityManagerFactory(@Qualifier(MILL_REPO_DATA_SOURCE_BEAN) DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf =
            new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPersistenceUnitName("mill-repo-pu");
        emf.setPackagesToScan("org.duracloud.mill");

        String hbm2ddlAuto =
            System.getProperty("hibernate.hbm2ddl.auto");

        HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
        va.setGenerateDdl(hbm2ddlAuto != null);
        va.setDatabase(Database.MYSQL);
        emf.setJpaVendorAdapter(va);
        
        Properties props = new Properties();
        if(hbm2ddlAuto != null){
            props.setProperty("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        }
        props.setProperty("hibernate.dialect",
                          "org.hibernate.dialect.MySQL5Dialect");
        props.setProperty("hibernate.ejb.naming_strategy",
                          "org.hibernate.cfg.ImprovedNamingStrategy");
        props.setProperty("hibernate.cache.provider_class",
                          "org.hibernate.cache.HashtableCacheProvider");
        props.setProperty("jadira.usertype.autoRegisterUserTypes", "true");
        props.setProperty("jadira.usertype.databaseZone", "jvm");
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.format_sql", "false");
        props.setProperty("hibernate.show_comments", "false");
        emf.setJpaProperties(props);
        return emf;
    }

    @Bean
    public ManifestStore manifestStore(JpaManifestItemRepo manifestRepo){
       return  new JpaManifestStore(manifestRepo);
    }
}
