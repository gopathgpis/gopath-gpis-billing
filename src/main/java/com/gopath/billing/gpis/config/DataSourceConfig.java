package com.gopath.billing.gpis.config;

import com.gopath.domain.security.AesEncryption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${security.db.password}")
    private String passwordEncrypt;

    @Bean
    public AesEncryption getAesEncryption(){
        return new AesEncryption();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties getDatasourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource getDatasource() {

        DataSourceProperties ds = getDatasourceProperties();
        ds.setPassword(getAesEncryption().decrypt(passwordEncrypt));

        return ds.initializeDataSourceBuilder().build();
    }
}
