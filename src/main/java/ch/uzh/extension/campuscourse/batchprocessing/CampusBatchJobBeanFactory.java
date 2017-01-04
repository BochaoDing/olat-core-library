package ch.uzh.extension.campuscourse.batchprocessing;

import org.olat.core.commons.persistence.InnoDBAwareDriverManagerDataSource;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Martin Schraner
 */
@Configuration
@ImportResource("classpath:org/olat/core/commons/persistence/_spring/databaseCorecontext.xml")
public class CampusBatchJobBeanFactory {

	@Autowired
	@Qualifier("mysql_local_DataSource")
	private InnoDBAwareDriverManagerDataSource mysqlLocalDataSource;

	@Autowired
	@Qualifier("postgresql_local_DataSource")
	private DriverManagerDataSource postgresLocalDataSource;

	@Bean
	public SimpleJobLauncher jobLauncher() throws Exception {
		SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
		simpleJobLauncher.setJobRepository(jobRepository().getJobRepository());
		return simpleJobLauncher;
	}

	@Bean
	public MapJobRepositoryFactoryBean jobRepository() {
		return new MapJobRepositoryFactoryBean();
	}

	@Bean
	public ThreadPoolTaskExecutor splitTaskExecutor() {
		return createThreadPoolTaskExecutor(3, 5);
	}

	@Bean
	public ThreadPoolTaskExecutor taskletTaskExecutor() {
		return createThreadPoolTaskExecutor(5, 10);
	}

	@Bean
	public ThreadPoolTaskExecutor synchronizationTaskletTaskExecutor() {
		return createThreadPoolTaskExecutor(3, 3);
	}

	@Bean
	public JpaTransactionManager transactionManager(@Value("${db.vendor}") String dbVendor, @Value("${db.source}") String dbSource) {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
		jpaTransactionManager.setEntityManagerFactory(localContainerEntityManagerFactoryBean(dbVendor, dbSource).getObject());
		return jpaTransactionManager;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(@Value("${db.vendor}") String dbVendor, @Value("${db.source}") String dbSource) {
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		localContainerEntityManagerFactoryBean.setPersistenceUnitName("default");
		switch (dbVendor + "_" + dbSource) {
			case "mysql_local": localContainerEntityManagerFactoryBean.setDataSource(mysqlLocalDataSource);
				break;
			case "postgres_local": localContainerEntityManagerFactoryBean.setDataSource(postgresLocalDataSource);
				break;
		}
		return localContainerEntityManagerFactoryBean;
	}

	private ThreadPoolTaskExecutor createThreadPoolTaskExecutor(int corePoolSize, int maxPoolSize) {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
		threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
		return threadPoolTaskExecutor;
	}
}