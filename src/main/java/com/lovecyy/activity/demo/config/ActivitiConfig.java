package com.lovecyy.activity.demo.config;

import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 工作流相关配置
 *
 * @author ruoyi
 */
@Configuration
public class ActivitiConfig
{

    // 流程配置，与spring整合采用SpringProcessEngineConfiguration这个实现
    @Bean
    public ProcessEngineConfiguration processEngineConfiguration(DataSource dataSource,
            PlatformTransactionManager transactionManager)
    {
        SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(dataSource);
        /**
         * public static final String DB_SCHEMA_UPDATE_FALSE = "false";操作activiti23张表的时候，如果表不存在，就抛出异常，不能自动创建23张表
         *
         * public static final String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";每次操作，都会先删除表，再创建表
         *
         * public static final String DB_SCHEMA_UPDATE_TRUE = "true";如果表不存在，就创建表，如果表存在，就直接操作
         */
        processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        processEngineConfiguration.setDatabaseType("mysql");

        processEngineConfiguration.setTransactionManager(transactionManager);

        // 流程图字体
        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");

        // 用户验证表改成使用视图
        processEngineConfiguration.setDbIdentityUsed(false);

        return processEngineConfiguration;
    }

    // 流程引擎，与spring整合使用factoryBean
    @Bean
    public ProcessEngineFactoryBean processEngine(ProcessEngineConfiguration processEngineConfiguration)
    {
        ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
        processEngineFactoryBean
                .setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
        return processEngineFactoryBean;
    }

    // 八大接口
    //管理流程定义(仓库服务类)
    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine)
    {
        return processEngine.getRepositoryService();
    }

    //执行管理，包括启动、推进、删除流程实例等
    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine)
    {
        return processEngine.getRuntimeService();
    }

    //任务管理
    @Bean
    public TaskService taskService(ProcessEngine processEngine)
    {
        return processEngine.getTaskService();
    }
    //历史管理（执行完的数据的管理
    @Bean
    public HistoryService historyService(ProcessEngine processEngine)
    {
        return processEngine.getHistoryService();
    }

    //可选服务，任务表单管理
    @Bean
    public FormService formService(ProcessEngine processEngine)
    {
        return processEngine.getFormService();
    }

    //组织机构管理
    @Bean
    public IdentityService identityService(ProcessEngine processEngine)
    {
        return processEngine.getIdentityService();
    }

    @Bean
    public ManagementService managementService(ProcessEngine processEngine)
    {
        return processEngine.getManagementService();
    }

    @Bean
    public DynamicBpmnService dynamicBpmnService(ProcessEngine processEngine)
    {
        return processEngine.getDynamicBpmnService();
    }
}
