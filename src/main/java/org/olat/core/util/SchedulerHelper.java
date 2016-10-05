package org.olat.core.util;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.Map;

public class SchedulerHelper {

    private static final OLog log = Tracing.createLoggerFor(SchedulerHelper.class);


    public static boolean triggerJob(String jobName, String jobGroup) {
        return triggerJob(jobName, jobGroup, "");
    }

    public static boolean triggerJob(String jobName, String jobGroup, String customErrorMessage) {
        //Scheduler[] schedulers = CoreSpringFactory.getImpl(Scheduler[].class);
        Map<String, Scheduler> schedulers = CoreSpringFactory.getBeansOfType(Scheduler.class);
        if (schedulers != null) {
            for (Scheduler scheduler : schedulers.values()) {
                // find and triggerJob the cron job
                try {
                    JobDetail detail = scheduler.getJobDetail(jobName, jobGroup);
                    if (detail != null) {
                        scheduler.triggerJob(detail.getName(), detail.getGroup());
                        return true;
                    }
                } catch (SchedulerException e) {
                    log.error(customErrorMessage, e);
                }
            }
        }

        return false;
    }

}
