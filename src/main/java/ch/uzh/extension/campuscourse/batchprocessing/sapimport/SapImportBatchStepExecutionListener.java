package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.batchprocessing.AbstractCampusBatchStepExecutionListener;
import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.dao.BatchJobAndSapImportStatisticDao;
import ch.uzh.extension.campuscourse.data.dao.BatchJobSkippedItemDao;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic;
import ch.uzh.extension.campuscourse.data.entity.BatchJobStatistic;
import ch.uzh.extension.campuscourse.service.dao.DaoManager;
import org.olat.core.commons.persistence.DB;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * @author Martin Schraner
 */
@Component
@Scope("step")
public class SapImportBatchStepExecutionListener extends AbstractCampusBatchStepExecutionListener {

	private final SapImportControlFileReader sapImportControlFileReader;
	private final BatchJobAndSapImportStatisticDao batchJobAndSapImportStatisticDao;

	@Autowired
	public SapImportBatchStepExecutionListener(
			DB dbInstance,
			BatchJobSkippedItemDao batchJobSkippedItemDao,
			DaoManager daoManager,
			CampusCourseConfiguration campusCourseConfiguration,
			SapImportControlFileReader sapImportControlFileReader,
			BatchJobAndSapImportStatisticDao batchJobAndSapImportStatisticDao) {
		super(dbInstance, batchJobSkippedItemDao, daoManager, campusCourseConfiguration);
		this.sapImportControlFileReader = sapImportControlFileReader;
		this.batchJobAndSapImportStatisticDao = batchJobAndSapImportStatisticDao;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		super.beforeStep(stepExecution);
		if (campusBatchStepName == CampusBatchStepName.IMPORT_TEXTS) {
			try {
				daoManager.deleteAllTexts();
				dbInstance.commitAndCloseSession();
			} catch (Throwable t) {
				dbInstance.commitAndCloseSession();
				throw t;
			}
		}
			// DISABLED FOR NOW
//		else if (campusBatchStepName == CampusBatchStepName.IMPORT_EVENTS) {
//			try {
//				daoManager.deleteAllEvents();
//				dbInstance.commitAndCloseSession();
//			} catch (Throwable t) {
//				dbInstance.commitAndCloseSession();
//				throw t;
//			}
//		}
	}

	@Override
	protected BatchJobStatistic createAndPersistBatchJobStatistic(StepExecution stepExecution) {
		// Determine sap import filename
		String sapImportFilenameWithoutSuffix = "";
		if (campusBatchStepName == CampusBatchStepName.IMPORT_ORGS) {
			sapImportFilenameWithoutSuffix = campusCourseConfiguration.getSapImportOrgsFilenameWithoutSuffix();
		} else if (campusBatchStepName == CampusBatchStepName.IMPORT_COURSES) {
			sapImportFilenameWithoutSuffix = campusCourseConfiguration.getSapImportCoursesFilenameWithoutSuffix();
		} else if (campusBatchStepName == CampusBatchStepName.IMPORT_STUDENTS) {
			sapImportFilenameWithoutSuffix = campusCourseConfiguration.getSapImportStudentsFilenameWithoutSuffix();
		} else if (campusBatchStepName == CampusBatchStepName.IMPORT_LECTURERS) {
			sapImportFilenameWithoutSuffix = campusCourseConfiguration.getSapImportLecturersFilenameWithoutSuffix();
		} else if (campusBatchStepName == CampusBatchStepName.IMPORT_STUDENT_COURSES) {
			sapImportFilenameWithoutSuffix = campusCourseConfiguration.getSapImportStudentCoursesFilenameWithoutSuffix();
		} else if (campusBatchStepName == CampusBatchStepName.IMPORT_LECTURER_COURSES) {
			sapImportFilenameWithoutSuffix = campusCourseConfiguration.getSapImportLecturerCoursesFilenameWithoutSuffix();
		} else if (campusBatchStepName == CampusBatchStepName.IMPORT_TEXTS) {
			sapImportFilenameWithoutSuffix = campusCourseConfiguration.getSapImportTextsFilenameWithoutSuffix();
		} else if (campusBatchStepName == CampusBatchStepName.IMPORT_EVENTS) {
			sapImportFilenameWithoutSuffix = campusCourseConfiguration.getSapImportEventsFilenameWithoutSuffix();
		}
		String sapImportFilename = sapImportFilenameWithoutSuffix + campusCourseConfiguration.getSapImportFilesSuffix();

		// Get date of sync
		Calendar dateOfSync = sapImportControlFileReader.getDateOfSyncOfSapImportFile(sapImportFilename);

		// Create and persist statistic
		boolean next = campusCourseConfiguration.getSapImportFilesSuffix().toUpperCase().contains("NEXT");
		BatchJobAndSapImportStatistic batchJobAndSapImportStatistic = new BatchJobAndSapImportStatistic(campusBatchStepName, stepExecution, dateOfSync, next);
		batchJobAndSapImportStatisticDao.save(batchJobAndSapImportStatistic);
		return batchJobAndSapImportStatistic;
	}
}
