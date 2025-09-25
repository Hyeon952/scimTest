package ai.duclo.scimtest.common.config;

import ai.duclo.scimtest.common.model.DeviceDBConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { DeviceDBConstants.JPA_DEVICE_BASE_PACKAGE }
        , entityManagerFactoryRef = DeviceDBConstants.DEVICE_ENTITY_MANAGER_FACTORY
        , transactionManagerRef = DeviceDBConstants.DEVICE_TRANSACTION_MANAGER)
public class JpaConfig {
    @Bean
    @ConfigurationProperties(DeviceDBConstants.SPRING_DATASOURCE_DEVICE)
    public DataSourceProperties deviceDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(DeviceDBConstants.DEVICE_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean deviceEntityManagerFactory(
            @Qualifier(DeviceDBConstants.DEVICE_DATA_SOURCE) DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.dialect",
                "org.hibernate.spatial.dialect.mysql.MySQL8SpatialDialect");
        return builder.dataSource(dataSource)
                .packages(DeviceDBConstants.JPA_DEVICE_ENTITY_BASE_PACKAGE)
                .properties(jpaProperties).build();
    }

    @Primary
    @Bean(DeviceDBConstants.DEVICE_DATA_SOURCE)
    @ConfigurationProperties(DeviceDBConstants.SPRING_DATASOURCE_DEVICE_HIKARI)
    public DataSource deviceDataSource() {
        DataSourceProperties dataSourceProperties = deviceDataSourceProperties();
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = DeviceDBConstants.DEVICE_TRANSACTION_MANAGER)
    public PlatformTransactionManager deviceTransactionManager(
            @Qualifier(DeviceDBConstants.DEVICE_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean todosEntityManagerFactory) {
        return new JpaTransactionManager(
                Objects.requireNonNull(todosEntityManagerFactory.getObject()));
    }
}
