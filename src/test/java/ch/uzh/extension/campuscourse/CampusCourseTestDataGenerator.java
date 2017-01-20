package ch.uzh.extension.campuscourse;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import ch.uzh.extension.campuscourse.data.entity.*;
import ch.uzh.extension.campuscourse.model.*;
import org.springframework.batch.core.BatchStatus;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Martin Schraner
 */
@Component
public class CampusCourseTestDataGenerator {

	private static final Date DATE_OF_IMPORT;

	static {
		Calendar dateOfImportAsCalendar = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
		dateOfImportAsCalendar.set(Calendar.HOUR_OF_DAY, 10);
		dateOfImportAsCalendar.set(Calendar.MINUTE, 13);
		DATE_OF_IMPORT = dateOfImportAsCalendar.getTime();
	}

	public List<Student> createStudents() {
		List<Student> students = new ArrayList<>();
		students.add(new Student(2100L, "1000", "firstName1", "lastName1", "email1", DATE_OF_IMPORT));
		students.add(new Student(2200L, "2000", "firstName2", "lastName2", "email2", DATE_OF_IMPORT));
		students.add(new Student(2300L, "3000", "firstName3", "lastName3", "email3", DATE_OF_IMPORT));
		students.add(new Student(2400L, "4000", "firstName4", "lastName4", "email4", DATE_OF_IMPORT));
		students.add(new Student(2500L, "5000", "firstName5", "lastName5", "email5", DATE_OF_IMPORT));
		students.add(new Student(2600L, "6000", "firstName6", "lastName6", "email6", DATE_OF_IMPORT));
		students.add(new Student(2700L, "7000", "firstName7", "lastName7", "email7", DATE_OF_IMPORT));
		students.add(new Student(2800L, "8000", "firstName8", "lastName8", "email8", DATE_OF_IMPORT));
		return students;
	}

	public List<Lecturer> createLecturers() {
		List<Lecturer> lecturers = new ArrayList<>();
		lecturers.add(new Lecturer(1100L, "firstName1", "lastName1", "email1", DATE_OF_IMPORT));
		lecturers.add(new Lecturer(1200L, "firstName2", "lastName2", "email2", DATE_OF_IMPORT));
		lecturers.add(new Lecturer(1300L, "firstName3", "lastName3", "email3", DATE_OF_IMPORT));
		lecturers.add(new Lecturer(1400L, "firstName4", "lastName4", "email4", DATE_OF_IMPORT));
		lecturers.add(new Lecturer(1500L, "firstName5", "lastName5", "email5", DATE_OF_IMPORT));
		lecturers.add(new Lecturer(1600L, "firstName6", "lastName6", "email6", DATE_OF_IMPORT));
		lecturers.add(new Lecturer(1700L, "firstName7", "lastName7", "email7", DATE_OF_IMPORT));
		lecturers.add(new Lecturer(1800L, "firstName8", "lastName8", "email8", DATE_OF_IMPORT));
		lecturers.add(new Lecturer(1900L, "firstName9", "lastName9", "email9", DATE_OF_IMPORT));
		return lecturers;
	}

	public List<CourseSemesterOrgId> createCourseSemesterOrgIds() {
		List<CourseSemesterOrgId> courseSemesterOrgIds = new ArrayList<>();
		courseSemesterOrgIds.add(new CourseSemesterOrgId(
				100L,
				"ABCD100",
				"Created1",
				"lvNr1",
				"Seminar",
				"EN",
				"Seminar",
				new GregorianCalendar(2099, Calendar.OCTOBER, 10).getTime(),
				new GregorianCalendar(2100, Calendar.FEBRUARY, 10).getTime(),
				"link",
				"Herbstsemester 2099",
				"",
				9100L,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				DATE_OF_IMPORT));

		courseSemesterOrgIds.add(new CourseSemesterOrgId(
				200L,
				"ABCD200",
				"Created2",
				"lvNr2",
				"Seminar",
				"EN",
				"Seminar",
				new GregorianCalendar(2099, Calendar.OCTOBER, 10).getTime(),
				new GregorianCalendar(2100, Calendar.FEBRUARY, 10).getTime(),
				"link",
				"Herbstsemester 2099",
				"",
				9300L,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				DATE_OF_IMPORT));

		courseSemesterOrgIds.add(new CourseSemesterOrgId(
				300L,
				"ABCD300",
				"Creatable",
				"lvNr3",
				"Seminar",
				"EN",
				"Seminar",
				new GregorianCalendar(2099, Calendar.OCTOBER, 10).getTime(),
				new GregorianCalendar(2100, Calendar.FEBRUARY, 10).getTime(),
				"link",
				"Herbstsemester 2099",
				"",
				9100L,
				9200L,
				9300L,
				9400L,
				9500L,
				9600L,
				9700L,
				9800L,
				9900L,
				DATE_OF_IMPORT));

		courseSemesterOrgIds.add(new CourseSemesterOrgId(
				400L,
				"ABCD400",
				"PreviousSemester",
				"lvNr4",
				"Seminar",
				"EN",
				"Seminar",
				new GregorianCalendar(2099, Calendar.APRIL, 10).getTime(),
				new GregorianCalendar(2099, Calendar.JULY, 10).getTime(),
				"link",
				"Frühjahrssemester 2099",
				"",
				9700L,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				new GregorianCalendar(2099, Calendar.APRIL, 11).getTime()));

		courseSemesterOrgIds.add(new CourseSemesterOrgId(
				500L,
				"ABCD500",
				"OldSemester",
				"lvNr5",
				"Seminar",
				"EN",
				"Seminar",
				new GregorianCalendar(2098, Calendar.OCTOBER, 10).getTime(),
				new GregorianCalendar(2099, Calendar.FEBRUARY, 10).getTime(),
				"link",
				"Herbstsemester 2098",
				"",
				9700L,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				new GregorianCalendar(2098, Calendar.OCTOBER, 11).getTime()));

		courseSemesterOrgIds.add(new CourseSemesterOrgId(
				600L,
				"ABCD600",
				"OldSemester",
				"lvNr6",
				"Seminar",
				"EN",
				"Seminar",
				new GregorianCalendar(2098, Calendar.APRIL, 10).getTime(),
				new GregorianCalendar(2098, Calendar.JULY, 10).getTime(),
				"link",
				"Frühjahrssemester 2098",
				"",
				9800L,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				new GregorianCalendar(2098, Calendar.APRIL, 11).getTime()));

		courseSemesterOrgIds.add(new CourseSemesterOrgId(
				700L,
				"ABCD700",
				"CourseOfDisabledOrg",
				"lvNr7",
				"Seminar",
				"EN",
				"Seminar",
				new GregorianCalendar(2099, Calendar.OCTOBER, 10).getTime(),
				new GregorianCalendar(2100, Calendar.FEBRUARY, 10).getTime(),
				"link",
				"Herbstsemester 2099",
				"",
				9200L,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				DATE_OF_IMPORT));

		courseSemesterOrgIds.add(new CourseSemesterOrgId(
				800L,
				"ABCD800",
				"CourseToBeExcluded",
				"lvNr8",
				"Seminar",
				"EN",
				"Seminar",
				new GregorianCalendar(2099, Calendar.OCTOBER, 10).getTime(),
				new GregorianCalendar(2100, Calendar.FEBRUARY, 10).getTime(),
				"link",
				"Herbstsemester 2099",
				"X",
				9800L,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				DATE_OF_IMPORT));

		return courseSemesterOrgIds;
	}

	public List<StudentIdCourseIdDateOfImport> createStudentIdCourseIdDateOfImports() {
		List<StudentIdCourseIdDateOfImport> studentIdCourseIds = new ArrayList<>();
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2100L, 100L, DATE_OF_IMPORT));
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2200L, 100L, DATE_OF_IMPORT));
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2100L, 200L, DATE_OF_IMPORT));
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2200L, 200L, DATE_OF_IMPORT));
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2100L, 300L, DATE_OF_IMPORT));
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2400L, 400L, DATE_OF_IMPORT));
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2500L, 500L, DATE_OF_IMPORT));
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2600L, 600L, DATE_OF_IMPORT));
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2700L, 700L, DATE_OF_IMPORT));
		studentIdCourseIds.add(new StudentIdCourseIdDateOfImport(2800L, 800L, DATE_OF_IMPORT));
		return studentIdCourseIds;
	}

	public List<LecturerIdCourseIdDateOfImport> createLecturerIdCourseIdDateOfImports() {
		List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIds = new ArrayList<>();
		lecturerIdCourseIds.add(new LecturerIdCourseIdDateOfImport(1100L, 100L, DATE_OF_IMPORT));
		lecturerIdCourseIds.add(new LecturerIdCourseIdDateOfImport(1100L, 200L, DATE_OF_IMPORT));
		lecturerIdCourseIds.add(new LecturerIdCourseIdDateOfImport(1200L, 200L, DATE_OF_IMPORT));
		lecturerIdCourseIds.add(new LecturerIdCourseIdDateOfImport(1300L, 300L, DATE_OF_IMPORT));
		lecturerIdCourseIds.add(new LecturerIdCourseIdDateOfImport(1400L, 400L, DATE_OF_IMPORT));
		lecturerIdCourseIds.add(new LecturerIdCourseIdDateOfImport(1500L, 500L, DATE_OF_IMPORT));
		lecturerIdCourseIds.add(new LecturerIdCourseIdDateOfImport(1600L, 600L, DATE_OF_IMPORT));
		lecturerIdCourseIds.add(new LecturerIdCourseIdDateOfImport(1800L, 700L, DATE_OF_IMPORT));
		lecturerIdCourseIds.add(new LecturerIdCourseIdDateOfImport(1900L, 800L, DATE_OF_IMPORT));
		return lecturerIdCourseIds;
	}

	public List<TextCourseId> createTextCourseIds() {
		List<TextCourseId> textCourseIds = new ArrayList<>();
		textCourseIds.add(new TextCourseId("Veranstaltungsinhalt", 1, "- praktische Tätigkeiten im chemischen Labor", DATE_OF_IMPORT, 100L));
		textCourseIds.add(new TextCourseId("Veranstaltungsinhalt", 2, "- Herstellung von Lösungen unterschiedlicher Konzentration", DATE_OF_IMPORT, 100L));
		textCourseIds.add(new TextCourseId("Unterrichtsmaterialien", 1, "Versuchsanleitungen,", DATE_OF_IMPORT, 100L));
		textCourseIds.add(new TextCourseId("Unterrichtsmaterialien", 2, "download von homepage (s. link)", DATE_OF_IMPORT, 100L));
		textCourseIds.add(new TextCourseId("Hinweise", 1, "Selbsttestfragen:", DATE_OF_IMPORT, 100L));
		textCourseIds.add(new TextCourseId("Hinweise", 2, "Zugriff über www.vetpharm.uzh.ch/cyberpharm", DATE_OF_IMPORT, 100L));
		textCourseIds.add(new TextCourseId("Veranstaltungsinhalt", 1, "- praktische Tätigkeiten im chemischen Labor2", DATE_OF_IMPORT, 200L));
		textCourseIds.add(new TextCourseId("Veranstaltungsinhalt", 2, "- Herstellung von Lösungen unterschiedlicher Konzentration2", DATE_OF_IMPORT, 200L));
		return textCourseIds;
	}

	public List<EventCourseId> createEventCourseIds() {
		List<EventCourseId> eventCourseIds = new ArrayList<>();
		eventCourseIds.add(new EventCourseId(new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), "10:00:00",  "11:30:00", DATE_OF_IMPORT, 100L));
		eventCourseIds.add(new EventCourseId(new GregorianCalendar(2014, Calendar.OCTOBER, 13).getTime(), "16:00:00",  "17:30:00", DATE_OF_IMPORT, 100L));
		eventCourseIds.add(new EventCourseId(new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), "09:00:00",  "10:30:00", DATE_OF_IMPORT, 200L));
		eventCourseIds.add(new EventCourseId(new GregorianCalendar(2014, Calendar.OCTOBER, 16).getTime(), "14:00:00",  "15:30:00", DATE_OF_IMPORT, 200L));
		return eventCourseIds;
	}

	public List<Org> createOrgs() {
		List<Org> orgs = new ArrayList<>();
		orgs.add(new Org(9100L, "1", "Theologische Fakultät", true, DATE_OF_IMPORT));
		orgs.add(new Org(9200L, "2", "Rechtswissenschaftliche Fakultät", false, DATE_OF_IMPORT));
		orgs.add(new Org(9300L, "3", "Wirtschaftswissenschaftliche Fakultät", true, DATE_OF_IMPORT));
		orgs.add(new Org(9400L, "4_Dek", "Humanmedizin", false, DATE_OF_IMPORT));
		orgs.add(new Org(9500L, "4_FB 1", "Zentrum für Zahnmedizin", false, DATE_OF_IMPORT));
		orgs.add(new Org(9600L, "5", "Vetsuisse-Fakultät", false, DATE_OF_IMPORT));
		orgs.add(new Org(9700L, "6", "Philosophische Fakultät", true, DATE_OF_IMPORT));
		orgs.add(new Org(9800L, "7", "Mathematisch-naturwissenschaftliche Fakultät", true, DATE_OF_IMPORT));
		orgs.add(new Org(9900L, "1_IFHR", "Institut für Hermeneutik und Religionsphilosophie", false, DATE_OF_IMPORT));
		orgs.add(new Org(9901L, "1_IFSR", "Institut für Schweizerische Reformationsgeschichte", false, DATE_OF_IMPORT));
		return orgs;
	}

	public List<BatchJobAndSapImportStatistic> createBatchJobAndSapImportStatistics() {
		List<BatchJobAndSapImportStatistic> batchJobAndSapImportStatistics = new ArrayList<>();

		Calendar startTime1 = new GregorianCalendar(2099, Calendar.OCTOBER, 10);
		startTime1.set(Calendar.HOUR_OF_DAY, 10);
		startTime1.set(Calendar.MINUTE, 10);
		Calendar endTime1 = new GregorianCalendar(2099, Calendar.OCTOBER, 10);
		endTime1.set(Calendar.HOUR_OF_DAY, 10);
		endTime1.set(Calendar.MINUTE, 12);
		Calendar dateOfSync1 = new GregorianCalendar(2099, Calendar.OCTOBER, 10);
		dateOfSync1.set(Calendar.HOUR_OF_DAY, 4);
		dateOfSync1.set(Calendar.MINUTE, 30);
		batchJobAndSapImportStatistics.add(new BatchJobAndSapImportStatistic(CampusBatchStepName.IMPORT_ORGS, BatchStatus.COMPLETED, startTime1.getTime(), endTime1.getTime(), 8, 8, 0, 0, 0, 0, 0, dateOfSync1, false));

		Calendar startTime2 = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
		startTime2.set(Calendar.HOUR_OF_DAY, 10);
		startTime2.set(Calendar.MINUTE, 10);
		Calendar endTime2 = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
		endTime2.set(Calendar.HOUR_OF_DAY, 10);
		endTime2.set(Calendar.MINUTE, 12);
		Calendar dateOfSync2 = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
		dateOfSync2.set(Calendar.HOUR_OF_DAY, 4);
		dateOfSync2.set(Calendar.MINUTE, 30);
		batchJobAndSapImportStatistics.add(new BatchJobAndSapImportStatistic(CampusBatchStepName.IMPORT_ORGS, BatchStatus.COMPLETED, startTime2.getTime(), endTime2.getTime(), 8, 8, 0, 0, 0, 0, 0, dateOfSync2, false));

		Calendar startTime3 = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
		startTime3.set(Calendar.HOUR_OF_DAY, 10);
		startTime3.set(Calendar.MINUTE, 13);
		Calendar endTime3 = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
		endTime3.set(Calendar.HOUR_OF_DAY, 10);
		endTime3.set(Calendar.MINUTE, 15);
		Calendar dateOfSync3 = new GregorianCalendar(2099, Calendar.OCTOBER, 11);
		dateOfSync3.set(Calendar.HOUR_OF_DAY, 4);
		dateOfSync3.set(Calendar.MINUTE, 30);
		batchJobAndSapImportStatistics.add(new BatchJobAndSapImportStatistic(CampusBatchStepName.IMPORT_COURSES, BatchStatus.COMPLETED, startTime3.getTime(), endTime3.getTime(), 8, 7, 0, 1, 0, 0, 0, dateOfSync3, false));

		Calendar startTime4 = new GregorianCalendar(2099, Calendar.OCTOBER, 12);
		startTime4.set(Calendar.HOUR_OF_DAY, 10);
		startTime4.set(Calendar.MINUTE, 10);
		Calendar endTime4 = new GregorianCalendar(2099, Calendar.OCTOBER, 12);
		endTime4.set(Calendar.HOUR_OF_DAY, 10);
		endTime4.set(Calendar.MINUTE, 12);
		Calendar dateOfSync4 = new GregorianCalendar(2099, Calendar.OCTOBER, 12);
		dateOfSync4.set(Calendar.HOUR_OF_DAY, 4);
		dateOfSync4.set(Calendar.MINUTE, 30);
		batchJobAndSapImportStatistics.add(new BatchJobAndSapImportStatistic(CampusBatchStepName.IMPORT_ORGS, BatchStatus.FAILED, startTime4.getTime(), endTime4.getTime(), 8, 7, 0, 1, 0, 0, 0, dateOfSync4, false));

		return batchJobAndSapImportStatistics;
	}

	public List<BatchJobAndUserMappingStatistic> createBatchJobAndUserMappingStatistics() {
		List<BatchJobAndUserMappingStatistic> batchJobAndUserMappingStatistics = new ArrayList<>();

		Calendar startTime1 = new GregorianCalendar(2099, Calendar.NOVEMBER, 10);
		startTime1.set(Calendar.HOUR_OF_DAY, 10);
		startTime1.set(Calendar.MINUTE, 10);
		Calendar endTime1 = new GregorianCalendar(2099, Calendar.NOVEMBER, 10);
		endTime1.set(Calendar.HOUR_OF_DAY, 10);
		endTime1.set(Calendar.MINUTE, 12);
		batchJobAndUserMappingStatistics.add(new BatchJobAndUserMappingStatistic(CampusBatchStepName.LECTURER_MAPPING, BatchStatus.COMPLETED, startTime1.getTime(), endTime1.getTime(), 30, 30, 0, 0, 0, 0, 0, 15, 2, 0, 3, 1, 3, 6));

		Calendar startTime2 = new GregorianCalendar(2099, Calendar.NOVEMBER, 10);
		startTime2.set(Calendar.HOUR_OF_DAY, 10);
		startTime2.set(Calendar.MINUTE, 11);
		Calendar endTime2 = new GregorianCalendar(2099, Calendar.NOVEMBER, 10);
		endTime2.set(Calendar.HOUR_OF_DAY, 10);
		endTime2.set(Calendar.MINUTE, 13);
		batchJobAndUserMappingStatistics.add(new BatchJobAndUserMappingStatistic(CampusBatchStepName.STUDENT_MAPPING, BatchStatus.COMPLETED, startTime2.getTime(), endTime2.getTime(), 30, 30, 0, 0, 0, 0, 0, 15, 2, 0, 3, 1, 3, 6));

		Calendar startTime3 = new GregorianCalendar(2099, Calendar.NOVEMBER, 11);
		startTime3.set(Calendar.HOUR_OF_DAY, 10);
		startTime3.set(Calendar.MINUTE, 10);
		Calendar endTime3 = new GregorianCalendar(2099, Calendar.NOVEMBER, 11);
		endTime3.set(Calendar.HOUR_OF_DAY, 10);
		endTime3.set(Calendar.MINUTE, 12);
		batchJobAndUserMappingStatistics.add(new BatchJobAndUserMappingStatistic(CampusBatchStepName.LECTURER_MAPPING, BatchStatus.UNKNOWN, startTime3.getTime(), endTime3.getTime(), 30, 30, 0, 0, 0, 0, 0, 15, 2, 0, 3, 1, 3, 6));

		Calendar startTime4 = new GregorianCalendar(2099, Calendar.NOVEMBER, 11);
		startTime4.set(Calendar.HOUR_OF_DAY, 10);
		startTime4.set(Calendar.MINUTE, 11);
		Calendar endTime4 = new GregorianCalendar(2099, Calendar.NOVEMBER, 11);
		endTime4.set(Calendar.HOUR_OF_DAY, 10);
		endTime4.set(Calendar.MINUTE, 13);
		batchJobAndUserMappingStatistics.add(new BatchJobAndUserMappingStatistic(CampusBatchStepName.STUDENT_MAPPING, BatchStatus.UNKNOWN, startTime4.getTime(), endTime4.getTime(), 30, 30, 0, 0, 0, 0, 0, 15, 2, 0, 3, 1, 3, 6));

		return batchJobAndUserMappingStatistics;
	}

	public List<BatchJobAndCampusCourseSynchronizationStatistic> createBatchJobAndCampusCourseSynchronizationStatistics() {
		List<BatchJobAndCampusCourseSynchronizationStatistic> batchJobAndCampusCourseSynchronizationStatistics = new ArrayList<>();

		Calendar startTime1 = new GregorianCalendar(2099, Calendar.DECEMBER, 10);
		startTime1.set(Calendar.HOUR_OF_DAY, 10);
		startTime1.set(Calendar.MINUTE, 10);
		Calendar endTime1 = new GregorianCalendar(2099, Calendar.DECEMBER, 10);
		endTime1.set(Calendar.HOUR_OF_DAY, 10);
		endTime1.set(Calendar.MINUTE, 12);
		batchJobAndCampusCourseSynchronizationStatistics.add(new BatchJobAndCampusCourseSynchronizationStatistic(CampusBatchStepName.CAMPUS_COURSE_SYNCHRONIZATION, BatchStatus.COMPLETED, startTime1.getTime(), endTime1.getTime(), 30, 30, 0, 0, 0, 0, 0, 5, 0, 53, 23));

		Calendar startTime2 = new GregorianCalendar(2099, Calendar.DECEMBER, 11);
		startTime2.set(Calendar.HOUR_OF_DAY, 10);
		startTime2.set(Calendar.MINUTE, 10);
		Calendar endTime2 = new GregorianCalendar(2099, Calendar.DECEMBER, 11);
		endTime2.set(Calendar.HOUR_OF_DAY, 10);
		endTime2.set(Calendar.MINUTE, 12);
		batchJobAndCampusCourseSynchronizationStatistics.add(new BatchJobAndCampusCourseSynchronizationStatistic(CampusBatchStepName.CAMPUS_COURSE_SYNCHRONIZATION, BatchStatus.UNKNOWN, startTime2.getTime(), endTime2.getTime(), 30, 30, 0, 0, 0, 0, 0, 5, 0, 53, 23));

		return batchJobAndCampusCourseSynchronizationStatistics;
	}

}
