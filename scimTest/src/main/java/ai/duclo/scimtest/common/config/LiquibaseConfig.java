package ai.duclo.scimtest.common.config;

import ai.duclo.scimtest.common.model.DeviceDBConstants;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class LiquibaseConfig {
    @Bean
    public SpringLiquibase scimDBScript(@Qualifier(DeviceDBConstants.DEVICE_DATA_SOURCE) DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/db-scim.xml");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
