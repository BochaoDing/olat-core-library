package ch.uzh.extension.campuscourse.batchprocessing;

import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Martin Schraner
 */
@Configuration
public class CampusBatchJobBeanFactory {

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

	private static ThreadPoolTaskExecutor createThreadPoolTaskExecutor(int corePoolSize, int maxPoolSize) {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
		threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
		return threadPoolTaskExecutor;
	}
}