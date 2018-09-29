package com.mtl.hulk.configuration;

import com.mtl.hulk.HulkContainer;
import com.mtl.hulk.HulkDataSource;
import com.mtl.hulk.aop.BeanFactoryHulkAdvisor;
import com.mtl.hulk.aop.interceptor.BrokerInterceptor;
import com.mtl.hulk.aop.interceptor.TransactionInterceptor;
import com.mtl.hulk.aop.pointcut.BrokerPointcut;
import com.mtl.hulk.aop.pointcut.TransactionPointcut;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.logger.data.sql.SQLDataSource;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(HulkProperties.class)
public class HulkConfiguration {

    private final HulkProperties properties;
    private final ApplicationContext applicationContext;

    public HulkConfiguration(HulkProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    @Bean
    public BusinessActivityManagerImpl bam() {
        return new BusinessActivityManagerImpl(properties, applicationContext);
    }

    @Bean
    public BeanFactoryHulkAdvisor hulkTransactionAdvisor() {
        BeanFactoryHulkAdvisor advisor = new BeanFactoryHulkAdvisor(new TransactionPointcut());
        advisor.setAdvice(hulkTransactionInterceptor());
        return advisor;
    }

    @Bean
    public BeanFactoryHulkAdvisor hulkBrokerAdvisor() {
        BeanFactoryHulkAdvisor advisor = new BeanFactoryHulkAdvisor(new BrokerPointcut());
        advisor.setAdvice(hulkBrokerInterceptor());
        return advisor;
    }

    @Bean
    public TransactionInterceptor hulkTransactionInterceptor() {
        return new TransactionInterceptor(bam(), applicationContext);
    }

    @Bean
    public BrokerInterceptor hulkBrokerInterceptor() {
        return new BrokerInterceptor(bam());
    }

    @Bean
    public HulkDataSource hulkDataSource() {
        List<DataSource> writeDataSources = new ArrayList<>();
        List<DataSource> readDataSources = new ArrayList<>();

        for (PoolProperties properties : properties.getLogMasters()) {
            DataSource writeDataSource = new DataSource();
            writeDataSource.setPoolProperties(properties);
            writeDataSources.add(writeDataSource);
        }
        for (PoolProperties properties : properties.getLogSlaves()) {
            DataSource readDataSource = new DataSource();
            readDataSource.setPoolProperties(properties);
            readDataSources.add(readDataSource);
        }

        return new SQLDataSource(writeDataSources, readDataSources);
    }

    @Bean
    public HulkApplicationListener hulkApplicationListener() {
        return new HulkApplicationListener();
    }

    private static class HulkApplicationListener implements ApplicationListener<ApplicationEvent> {

        @Autowired
        private BusinessActivityManagerImpl bam;
        @Autowired
        private TransactionInterceptor hulkTransactionInterceptor;
        @Autowired
        private BrokerInterceptor hulkBrokerInterceptor;
        @Autowired
        private HulkDataSource hulkDataSource;

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ContextRefreshedEvent) {
                HulkContainer.setDatasource(hulkDataSource);
                HulkContainer.getInterceptors().add(hulkBrokerInterceptor);
                HulkContainer.getInterceptors().add(hulkTransactionInterceptor);
            } else if (event instanceof ContextClosedEvent) {
                HulkContainer.destroy();
                bam.destroy();
            }
        }

    }

}
