/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.commons.services.taskexecutor.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.LongRunnable;
import org.olat.core.commons.services.taskexecutor.Sequential;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskAwareRunnable;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.model.DBSecureRunnable;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.taskexecutor.model.PersistentTaskRunnable;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.resource.OLATResource;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Description:<br>
 * Generic task executor to run tasks in it's own threads. Use it to decouple stuff that might
 * takes more time than a user may is willing to wait. The task gets executed by a thread pool.
 * If you look for scheduled task see @see {@link org.olat.core.commons.services.scheduler}
 * 
 * <P>
 * Initial Date:  02.05.2007 <br>
 * @author guido
 * @author srosse, stephane.rosse@frentix.com, http://www.frnetix.com
 */
public class TaskExecutorManagerImpl extends BasicManager implements TaskExecutorManager {
	private static final OLog log = Tracing.createLoggerFor(TaskExecutorManagerImpl.class);

	private final ExecutorService taskExecutor;
	private final ExecutorService sequentialTaskExecutor;
	private final DB dbInstance;
	private final PersistentTaskDAO persistentTaskDao;
	private final Scheduler scheduler;

	@Autowired
	private TaskExecutorManagerImpl(@Qualifier("mpTaskExecutorService") ExecutorService mpTaskExecutor,
									@Qualifier("searchExecutor") ExecutorService sequentialTaskExecutor,
									DB dbInstance,
									PersistentTaskDAO persistentTaskDao,
									@Qualifier("schedulerFactoryBean") Scheduler scheduler) {
		this.taskExecutor = mpTaskExecutor;
		this.sequentialTaskExecutor = sequentialTaskExecutor;
		this.dbInstance = dbInstance;
		this.persistentTaskDao = persistentTaskDao;
		this.scheduler = scheduler;
	}

	public void shutdown() {
		taskExecutor.shutdown();
		sequentialTaskExecutor.shutdown();
	}
	
	@Override
	public void execute(Runnable task) {
		//wrap call to the task here to catch all errors that are may not catched yet in the task itself
		//like outOfMemory or other system errors.
		Task persistentTask = null;
		if(task instanceof LongRunnable) {
			persistentTask = persistentTaskDao.createTask(UUID.randomUUID().toString(), (LongRunnable)task);
			dbInstance.commit();
		} else {
			execute(task, persistentTask, (task instanceof Sequential));
		}
	}

	@Override
	public void execute(LongRunnable task, Identity creator, OLATResource resource,
			String resSubPath, Date scheduledDate) {

		persistentTaskDao.createTask(UUID.randomUUID().toString(), task, creator, resource, resSubPath, scheduledDate);
		dbInstance.commit();
	}
	
	private void execute(Runnable task, Task persistentTask, boolean sequential) {
		if (taskExecutor != null) {
			if(task instanceof TaskAwareRunnable) {
				((TaskAwareRunnable)task).setTask(persistentTask);
			}
			DBSecureRunnable safetask = new DBSecureRunnable(task);
			if(sequential) {
				sequentialTaskExecutor.submit(safetask);
			} else {
				taskExecutor.submit(safetask);
			}
			
		} else {
			logError("taskExecutor is not initialized (taskExecutor=null). Do not call 'runTask' before TaskExecutorModule is initialized.", null);
			throw new AssertException("taskExecutor is not initialized");
		}
	}

	@Override
	public void executeTaskToDo() {
		try {
			JobDetail detail = scheduler.getJobDetail("taskExecutorJob", Scheduler.DEFAULT_GROUP);
			scheduler.triggerJob(detail.getName(), detail.getGroup());
		} catch (SchedulerException e) {
			log.error("", e);
		}
	}

	protected void processTaskToDo() {
		try {
			List<Long> todos = persistentTaskDao.tasksToDo();
			for(Long todo:todos) {
				PersistentTask task = persistentTaskDao.loadTaskById(todo);
				Runnable runnable = persistentTaskDao.deserializeTask(task);
				PersistentTaskRunnable command = new PersistentTaskRunnable(todo);
				execute(command, null, (runnable instanceof Sequential));
			}
		} catch (Exception e) {
			// ups, something went completely wrong! We log this but continue next time
			log.error("Error while executing task todo", e);
		}		
	}

	@Override
	public List<Task> getTasks(OLATResource resource) {
		return persistentTaskDao.findTasks(resource);
	}

	@Override
	public List<Identity> getModifiers(Task task) {
		return persistentTaskDao.getModifiers(task);
	}

	@Override
	public Task pickTaskForEdition(Task task) {
		return persistentTaskDao.pickTaskForEdition(task.getKey());
	}

	@Override
	public Task returnTaskAfterEdition(Task task, TaskStatus wishedStatus) {
		return persistentTaskDao.returnTaskAfterEdition(task.getKey(), wishedStatus);
	}

	@Override
	public <T extends Runnable> T getPersistedRunnableTask(Task task, Class<T> type) {
		if(task instanceof PersistentTask) {
			PersistentTask ptask = (PersistentTask)task;
			@SuppressWarnings("unchecked")
			T runnable = (T)persistentTaskDao.deserializeTask(ptask);
			return runnable;
		}
		return null;
	}
	
	@Override
	public void updateAndReturn(Task task, LongRunnable runnableTask, Identity modifier, Date scheduledDate) {
		persistentTaskDao.updateTask(task, runnableTask, modifier, scheduledDate);
	}

	@Override
	public void delete(Task task) {
		persistentTaskDao.delete(task);
	}

	@Override
	public void delete(OLATResource resource) {
		persistentTaskDao.delete(resource);
	}

	@Override
	public void delete(OLATResource resource, String resSubPath) {
		persistentTaskDao.delete(resource, resSubPath);
	}
}
