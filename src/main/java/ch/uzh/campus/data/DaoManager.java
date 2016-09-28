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
 * <p>
 */
package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.CampusCourseImportTO;
import ch.uzh.campus.utils.ListUtil;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

import static java.lang.Boolean.TRUE;

@Repository
public class DaoManager {

    private static final OLog LOG = Tracing.createLoggerFor(DaoManager.class);

	private final CourseDao courseDao;
    private final StudentDao studentDao;
    private final LecturerCourseDao lecturerCourseDao;
    private final StudentCourseDao studentCourseDao;
    private final LecturerDao lecturerDao;
    private final DelegationDao delegationDao;
    private final TextDao textDao;
    private final EventDao eventDao;
    private final OrgDao orgDao;
	private final ImportStatisticDao statisticDao;
	private final DataConverter dataConverter;
	private final CampusCourseConfiguration campusCourseConfiguration;
	private final boolean shortTitleActivated;

	@Autowired
	public DaoManager(CourseDao courseDao,
                      StudentDao studentDao,
                      LecturerCourseDao lecturerCourseDao,
                      StudentCourseDao studentCourseDao,
                      LecturerDao lecturerDao,
                      DelegationDao delegationDao,
                      TextDao textDao,
                      EventDao eventDao,
                      OrgDao orgDao,
                      ImportStatisticDao statisticDao,
                      DataConverter dataConverter,
                      CampusCourseConfiguration campusCourseConfiguration,
                      @Value("${campus.lv_kuerzel.activated}") String shortTitleActivated) {
		this.courseDao = courseDao;
		this.studentDao = studentDao;
		this.lecturerCourseDao = lecturerCourseDao;
		this.studentCourseDao = studentCourseDao;
		this.lecturerDao = lecturerDao;
		this.delegationDao = delegationDao;
		this.textDao = textDao;
		this.eventDao = eventDao;
		this.orgDao = orgDao;
		this.statisticDao = statisticDao;
		this.dataConverter = dataConverter;
		this.campusCourseConfiguration = campusCourseConfiguration;
		this.shortTitleActivated = TRUE.toString().equals(shortTitleActivated);
	}

    public void saveCourses(List<Course> courses) {
        for (Course course : courses) {
            courseDao.save(course);
        }
    }

    public void saveOrgs(List<Org> orgs) {
        orgDao.save(orgs);
    }

    public void saveStudents(List<Student> students) {
        studentDao.save(students);
    }

    public void saveLecturers(List<Lecturer> lecturers) {
        lecturerDao.save(lecturers);
    }
    
    public void saveStudentCourses(List<StudentCourse> studentCourses) {
        studentCourseDao.saveOrUpdateList(studentCourses);
    }

    public void saveDelegation(Identity delegator, Identity delegatee) {
        delegationDao.save(delegator.getKey(), delegatee.getKey());
    }

    public boolean existsDelegation(Identity delegator, Identity delegatee) {
        return delegationDao.existsDelegation(delegator.getKey(), delegatee.getKey());
    }

    public boolean existResourceableId(Long resourceableId) {
        return courseDao.existResourceableId(resourceableId);
    }

    public void deleteCourse(Course course) {
        courseDao.delete(course);
    }

    public void deleteCourseById(Long courseId) {
        courseDao.deleteByCourseId(courseId);
    }

    public void deleteCoursesAndBookingsByCourseIds(List<Long> courseIds) {
        List<List<Long>> listSplit = ListUtil.split(courseIds, campusCourseConfiguration.getEntitiesSublistMaxSize());
        for (List<Long> subList : listSplit) {
            if (!subList.isEmpty()) {
                courseDao.deleteByCourseIds(subList);
            }
        }
    }

    public List<Long> getAllCoursesToBeDeleted() {
        return courseDao.getAllNotCreatedOrphanedCourses();
    }

    public Course getCourseById(Long id) {
        return courseDao.getCourseById(id);
    }

    public int deleteAllTexts() {
        return textDao.deleteAllTextsAsBulkDelete();
    }

    public int deleteAllEvents() {
        return eventDao.deleteAllEventsAsBulkDelete();
    }

    public void deleteStudentsAndBookingsByStudentIds(List<Long> studentIds) {
        List<List<Long>> listSplit = ListUtil.split(studentIds, campusCourseConfiguration.getEntitiesSublistMaxSize());
        for (List<Long> subList : listSplit) {
            if (!subList.isEmpty()) {
                studentCourseDao.deleteByStudentIdsAsBulkDelete(subList);
                studentDao.deleteByStudentIdsAsBulkDelete(subList);
            }
        }
    }

    public void deleteStudent(Student student) {
        studentDao.delete(student);
    }

    public List<LecturerIdCourseId> getAllNotUpdatedLCBookingOfCurrentSemester(Date date) {
        return lecturerCourseDao.getAllNotUpdatedLCBookingOfCurrentSemester(date);
    }

    public int deleteLCBookingByLecturerIdCourseIds(List<LecturerIdCourseId> lecturerIdCourseIds) {
        return lecturerCourseDao.deleteByLecturerIdCourseIdsAsBulkDelete(lecturerIdCourseIds);
    }

    public int deleteAllLCBookingTooFarInThePast(Date date) {
        return lecturerCourseDao.deleteAllLCBookingTooFarInThePastAsBulkDelete(date);
    }

    public List<StudentIdCourseId> getAllNotUpdatedSCBookingOfCurrentSemester(Date date) {
        return studentCourseDao.getAllNotUpdatedSCBookingOfCurrentSemester(date);
    }

    public int deleteSCBookingByStudentIdCourseIds(List<StudentIdCourseId> studentIdCourseIds) {
        return studentCourseDao.deleteByStudentIdCourseIdsAsBulkDelete(studentIdCourseIds);
    }

    public int deleteAllSCBookingTooFarInThePast(Date date) {
        return studentCourseDao.deleteAllSCBookingTooFarInThePastAsBulkDelete(date);
    }

    public void deleteLecturersAndBookingsByLecturerIds(List<Long> lecturerIds) {
        List<List<Long>> listSplit = ListUtil.split(lecturerIds, campusCourseConfiguration.getEntitiesSublistMaxSize());
        for (List<Long> subList : listSplit) {
            if (!subList.isEmpty()) {
                lecturerCourseDao.deleteByLecturerIdsAsBulkDelete(subList);
                lecturerDao.deleteByLecturerIdsAsBulkDelete(subList);
            }
        }
    }

    public void delete(Lecturer lecturer) {
        lecturerDao.delete(lecturer);
    }

    public Student getStudentById(Long id) {
        return studentDao.getStudentById(id);
    }

    public List<Student> getAllStudents() {
        return studentDao.getAllStudentsWithCreatedOrNotCreatedCreatableCourses();
    }

    public Student getStudentByRegistrationNrId(String registrationNr) {
        return studentDao.getStudentByRegistrationNr(registrationNr);
    }

    public Student getStudentByEmail(String email) {
        return studentDao.getStudentByEmail(email);
    }

    public List<Long> getAllStudentsToBeDeleted(Date date) {
        return studentDao.getAllNotManuallyMappedOrTooOldOrphanedStudents(date);
    }

    public void deleteEventsByCourseId(Long courseId) {
        eventDao.deleteEventsByCourseId(courseId);
    }

    public void deleteTextsByCourseId(Long courseId) {
        textDao.deleteTextsByCourseId(courseId);
    }

    public Lecturer getLecturerById(Long id) {
        return lecturerDao.getLecturerById(id);
    }

    public List<Lecturer> getAllLecturers() {
        return lecturerDao.getAllLecturersWithCreatedOrNotCreatedCreatableCourses();
    }

    public Lecturer getLecturerByEmail(String email) {
        return lecturerDao.getLecturerByEmail(email);
    }

    public List<Long> getAllLecturersToBeDeleted(Date date) {
        return lecturerDao.getAllNotManuallyMappedOrTooOldOrphanedLecturers(date);
    }

    public List<Long> getAllOrgsToBeDeleted() {
        return orgDao.getAllOrphanedOrgs();
    }

    public void deleteOrgByIds(List<Long> orgIds) {
        orgDao.deleteByOrgIds(orgIds);
    }

    public List<Student> getStudentsByMappedIdentityKey(Long identityKey) {
        return studentDao.getStudentsByMappedIdentityKey(identityKey);
    }

    public List<Lecturer> getLecturersByMappedIdentityKey(Long identityKey) {
        return lecturerDao.getLecturersByMappedIdentityKey(identityKey);
    }

    public List<Text> getTextByCourseId(Long id) {
        return textDao.getTextsByCourseId(id);
    }

    public String getTextContentsByCourseId(Long id) {
        return textDao.getContentsByCourseId(id);
    }

    public String getTextInfosByCourseId(Long id) {
        return textDao.getInfosByCourseId(id);
    }

    public String getTextMaterialsByCourseId(Long id) {
        return textDao.getMaterialsByCourseId(id);
    }

    public List<Course> getCreatedAndNotCreatedCreatableCoursesByStudentId(Long studentId) {

        List<Course> coursesOfStudent = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(studentId);

        // We also have to look for courses the student booked as parent course
        List<Course> coursesBookedOnlyAsParentCourse = courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(studentId);
        for (Course course : coursesBookedOnlyAsParentCourse) {
            // Only add course if student course booking of current semester is not up-to-date
            if (!areStudentCourseBookingsForCurrentSemesterUpToDate(course) && !coursesOfStudent.contains(course)) {
                coursesOfStudent.add(course);
            }
        }

        return coursesBookedOnlyAsParentCourse;
    }

    public List<Course> getCreatedCoursesByStudentId(Long studentId, String searchString) {

        List<Course> coursesOfStudent = courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(studentId, searchString);

        // We also have to look for courses the student booked as parent course
        List<Course> coursesBookedOnlyAsParentCourse = courseDao.getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(studentId, searchString);
        for (Course course : coursesBookedOnlyAsParentCourse) {
            // Only add course if student course booking of current semester is not up-to-date
            if (!areStudentCourseBookingsForCurrentSemesterUpToDate(course) && !coursesOfStudent.contains(course)) {
                coursesOfStudent.add(course);
            }
        }

        return coursesBookedOnlyAsParentCourse;
    }

    public List<Course> getNotCreatedCoursesByStudentId(Long id, String searchString) {
        return courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(id, searchString);
    }

    public List<Course> getCreatedAndNotCreatedCreatableCoursesByLecturerId(Long id) {
        return courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(id);
    }

    public List<Course> getCreatedCoursesByLecturerId(Long id, String searchString) {
        return courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(id, searchString);
    }

    public List<Course> getNotCreatedCoursesByLecturerId(Long id, String searchString) {
        return courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(id, searchString);
    }

    public Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapUserType userType, String searchString) {
        Set<Course> coursesWithoutResourceableId = new HashSet<>();
        coursesWithoutResourceableId.addAll(getCourses(identity, userType, false, searchString));// false --- > notCreatedCourses
        // THE CASE OF LECTURER, ADD THE APPROPRIATE DELEGATES
        if (userType.equals(SapUserType.LECTURER)) {
            List<Delegation> delegations = delegationDao.getDelegationsByDelegatee(identity.getKey());
            for (Delegation delegation : delegations) {
                coursesWithoutResourceableId.addAll(getCourses(delegation.getDelegator(), userType, false, searchString));// false --- > notCreatedCourses
            }
        }
        return coursesWithoutResourceableId;
    }

    public Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapUserType userType, String searchString) {
        Set<Course> coursesWithResourceableId = new HashSet<>();
        coursesWithResourceableId.addAll(getCourses(identity, userType, true, searchString));// true --- > CreatedCourses
        // THE CASE OF LECTURER, ADD THE APPROPRIATE DELEGATES
        if (userType.equals(SapUserType.LECTURER)) {
            List<Delegation> delegations = delegationDao.getDelegationsByDelegatee(identity.getKey());
            for (Delegation delegation : delegations) {
                coursesWithResourceableId.addAll(getCourses(delegation.getDelegator(), userType, true, searchString));// true --- > CreatedCourses
            }
        }
        return coursesWithResourceableId;
    }

    public Set<Course> getCourses(Identity identity, SapUserType userType, boolean created, String searchString) {
        Set <Course> courses = new HashSet<>();
        if (userType.equals(SapUserType.LECTURER)) {
            for (Lecturer lecturer : getLecturersByMappedIdentityKey(identity.getKey())) {
                if (created) {
                    courses.addAll(getCreatedCoursesByLecturerId(lecturer.getPersonalNr(), searchString));
                } else {
                    courses.addAll(getNotCreatedCoursesByLecturerId(lecturer.getPersonalNr(), searchString));
                }
            }
        } else {
            for (Student student : getStudentsByMappedIdentityKey(identity.getKey())) {
                if (created) {
                    courses.addAll(getCreatedCoursesByStudentId(student.getId(), searchString));
                } else {
                    courses.addAll(getNotCreatedCoursesByStudentId(student.getId(), searchString));
                }
            }
        }
        return courses;
    }

    public void saveCampusCourseResoureableId(Long courseId, Long resourceableId) {
        courseDao.saveResourceableId(courseId, resourceableId);
    }

    public Course getLatestCourseByResourceable(Long resourcableId) throws Exception {
        return courseDao.getLatestCourseByResourceable(resourcableId);
    }

    public void resetResourceableIdAndParentCourseReference(Long resourceableId) {
        courseDao.resetResourceableIdAndParentCourse(resourceableId);
    }

    public void saveParentCourseId(Long courseId, Long parentCourseId) {
        courseDao.saveParentCourseId(courseId, parentCourseId);
    }

    public List<Long> getAllCreatedSapCourcesIds() {
        return courseDao.getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemester();
    }

    public List<Long> getResourceableIdsOfAllCreatedNotContinuedCoursesOfPreviousSemesters() {
        return courseDao.getResourceableIdsOfAllCreatedNotContinuedCoursesOfPreviousSemestersNotTooFarInThePast();
    }

    public List<Long> getAllNotCreatedSapCourcesIds() {
        return courseDao.getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester();
    }

    public List<Long> getIdsOfAllEnabledOrgs() {
        return orgDao.getIdsOfAllEnabledOrgs();
    }

    public List<Course> getAllCreatedSapCources() {
        return courseDao.getAllCreatedCoursesOfCurrentSemester();
    }

    public CampusCourseImportTO getSapCampusCourse(long courseId) {
        Course course = getCourseById(courseId);
		if (course == null) {
			return null;
		}

		Set<StudentCourse> studentCourses = course.getStudentCourses();
        if (!areStudentCourseBookingsForCurrentSemesterUpToDate(course)) {
            // Bookings of current semester not up-to-date, so also take bookings of parent course
            studentCourses.addAll(course.getParentCourse().getStudentCourses());
        }

        Set<LecturerCourse> lecturerCourses = course.getLecturerCourses();

        return new CampusCourseImportTO(course.getTitleToBeDisplayed(shortTitleActivated),
				course.getSemester(),
				dataConverter.convertLecturersToIdentities(lecturerCourses),
                dataConverter.convertDelegateesToIdentities(lecturerCourses),
				dataConverter.convertStudentsToIdentities(studentCourses),
                textDao.getContentsByCourseId(course.getId()), course.getResourceableId(),
				course.getId(), course.getLanguage(), course.getVvzLink());
    }

    private boolean areStudentCourseBookingsForCurrentSemesterUpToDate(Course course) {
        // i)  If we have no parent course (i.e. it is not a continued course) we assume that the student course booking
        //     is always up-to-date.
        // ii) If we have a parent course (i.e. it is a continued course) we require that at least 50% of the bookings
        //     of the current semester must be students who have already booked the parent course. Otherwise
        //     the secretariat seems not have (manually) copied all the (permitted) students of the parent course.
        return (course.getParentCourse() == null || studentDao.hasMoreThan50PercentOfStudentsOfSpecificCourseBothABookingOfCourseAndParentCourse(course.getId()));
    }

    public List getDelegatees(Identity delegator) {
        return dataConverter.getDelegatees(delegator);
    }

    public void deleteDelegation(Identity delegator, Identity delegatee) {
        delegationDao.deleteDelegationById(delegator.getKey(), delegatee.getKey());
    }

    public boolean checkImportedData() {
        return (statisticDao.getLastCompletedImportedStatistic().size() == campusCourseConfiguration.getMustCompletedImportedFiles());
    }
}
