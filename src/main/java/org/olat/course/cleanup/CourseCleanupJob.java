package org.olat.course.cleanup;

import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.repository.handlers.CourseHandler;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.File;
import java.io.IOException;

public class CourseCleanupJob extends QuartzJobBean {

    public static String CLEANUP_ROOT_DIR_NAME = "cleanup";
    private static final OLog log = Tracing.createLoggerFor(CourseHandler.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String relPath = File.separator + CLEANUP_ROOT_DIR_NAME;
        OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(relPath, null);
        File cleanupBaseDirectory = rootFolder.getBasefile();
        if (cleanupBaseDirectory.exists()) {
            try {
                FileUtils.deleteDirsAndFiles(cleanupBaseDirectory, true, false);
            } catch (Exception e) {
                log.error("Failed to empty olatdata/cleanup folder");
            }
        }
    }
}
