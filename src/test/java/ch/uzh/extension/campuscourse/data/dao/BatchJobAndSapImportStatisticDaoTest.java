package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.CampusCourseTestDataGenerator;
import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.data.entity.BatchJobAndSapImportStatistic;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Martin Schraner
 */
@Component
public class BatchJobAndSapImportStatisticDaoTest extends CampusCourseTestCase {

    @Autowired
    private BatchJobAndSapImportStatisticDao batchJobAndSapImportStatisticDao;

    @Autowired
    private CampusCourseTestDataGenerator campusCourseTestDataGenerator;

	@Before
    public void setup() throws CampusCourseException {
		insertTestData();
    }

    @Test
    public void testGetNumberOfCompletedBatchStepsOfLastSapImport() {
       	assertEquals(2, batchJobAndSapImportStatisticDao.getNumberOfCompletedBatchStepsOfLastSapImport());
    }

    @Test
	public void testGetNumberOfCompletedBatchStepsOfSapImportOfToday() {

		int sizeBeforeInsert = batchJobAndSapImportStatisticDao.getNumberOfCompletedBatchStepsOfSapImportOfToday();

		// Create and insert import statistic of today
		List<BatchJobAndSapImportStatistic> batchJobAndSapImportStatisticsOfToday = new ArrayList<>();

		Calendar startTime1 = new GregorianCalendar();
		startTime1.set(Calendar.HOUR_OF_DAY, 4);
		startTime1.set(Calendar.MINUTE, 10);
		Calendar endTime1 = new GregorianCalendar();
		endTime1.set(Calendar.HOUR_OF_DAY, 4);
		endTime1.set(Calendar.MINUTE, 12);
		Calendar dateOfSync1 = new GregorianCalendar();
		dateOfSync1.set(Calendar.HOUR_OF_DAY, 4);
		dateOfSync1.set(Calendar.MINUTE, 30);
		batchJobAndSapImportStatisticsOfToday.add(new BatchJobAndSapImportStatistic(CampusBatchStepName.IMPORT_ORGS, BatchStatus.COMPLETED, startTime1.getTime(), endTime1.getTime(), 8, 8, 0, 0, 0, 0, 0, dateOfSync1, false));

		Calendar startTime2 = new GregorianCalendar();
		startTime2.set(Calendar.HOUR_OF_DAY, 4);
		startTime2.set(Calendar.MINUTE, 13);
		Calendar endTime2 = new GregorianCalendar();
		endTime2.set(Calendar.HOUR_OF_DAY, 4);
		endTime2.set(Calendar.MINUTE, 15);
		Calendar dateOfSync2 = new GregorianCalendar();
		dateOfSync2.set(Calendar.HOUR_OF_DAY, 4);
		dateOfSync2.set(Calendar.MINUTE, 30);
		batchJobAndSapImportStatisticsOfToday.add(new BatchJobAndSapImportStatistic(CampusBatchStepName.IMPORT_STUDENTS, BatchStatus.COMPLETED, startTime2.getTime(), endTime2.getTime(), 8, 7, 0, 1, 0, 0, 0, dateOfSync2, false));

		Calendar startTime3 = new GregorianCalendar();
		startTime2.set(Calendar.HOUR_OF_DAY, 4);
		startTime2.set(Calendar.MINUTE, 15);
		Calendar endTime3 = new GregorianCalendar();
		endTime2.set(Calendar.HOUR_OF_DAY, 4);
		endTime2.set(Calendar.MINUTE, 18);
		Calendar dateOfSync3 = new GregorianCalendar();
		dateOfSync3.set(Calendar.HOUR_OF_DAY, 4);
		dateOfSync3.set(Calendar.MINUTE, 30);
		batchJobAndSapImportStatisticsOfToday.add(new BatchJobAndSapImportStatistic(CampusBatchStepName.IMPORT_STUDENT_COURSES, BatchStatus.COMPLETED, startTime3.getTime(), endTime3.getTime(), 8, 7, 0, 1, 0, 0, 0, dateOfSync3, false));

		batchJobAndSapImportStatisticDao.save(batchJobAndSapImportStatisticsOfToday);
		dbInstance.flush();

       	assertEquals(sizeBeforeInsert + 3, batchJobAndSapImportStatisticDao.getNumberOfCompletedBatchStepsOfSapImportOfToday());
	}

    @Test
    public void testGetStartTimeOfMostRecentCompletedCourseImport() {
        Calendar startTimeOfMostRecentCourseImportAsCalendar = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.HOUR_OF_DAY, 10);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.MINUTE, 13);
        startTimeOfMostRecentCourseImportAsCalendar.set(Calendar.SECOND, 0);
        assertEquals(startTimeOfMostRecentCourseImportAsCalendar.getTime(), batchJobAndSapImportStatisticDao.getStartTimeOfMostRecentCompletedCourseImport());
    }

    private void insertTestData() throws CampusCourseException {
        List<BatchJobAndSapImportStatistic> importStatistics = campusCourseTestDataGenerator.createBatchJobAndSapImportStatistics();
        batchJobAndSapImportStatisticDao.save(importStatistics);
        dbInstance.flush();
    }
}
