package ch.uzh.extension.campuscourse.service.dao;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.model.LecturerIdCourseId;
import ch.uzh.extension.campuscourse.model.StudentIdCourseId;
import ch.uzh.extension.campuscourse.model.SapUserType;
import ch.uzh.extension.campuscourse.model.CampusCourseTO;
import ch.uzh.extension.campuscourse.model.CampusCourseTOForUI;
import ch.uzh.extension.campuscourse.model.CampusGroups;
import ch.uzh.extension.campuscourse.util.ListUtil;
import ch.uzh.extension.campuscourse.data.dao.*;
import ch.uzh.extension.campuscourse.data.entity.*;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

@Repository
public class DaoManager {

	private final CourseDao courseDao;
    private final SemesterDao semesterDao;
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

    @Autowired
	public DaoManager(CourseDao courseDao,
                      SemesterDao semesterDao,
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
                      CampusCourseConfiguration campusCourseConfiguration) {
		this.courseDao = courseDao;
        this.semesterDao = semesterDao;
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

    public void saveDelegation(Identity delegator, Identity delegatee) {
        delegationDao.save(delegator.getKey(), delegatee.getKey());
    }

    public boolean existsDelegation(Identity delegator, Identity delegatee) {
        return delegationDao.existsDelegation(delegator.getKey(), delegatee.getKey());
    }

    public boolean existCoursesForRepositoryEntry(Long repositoryEntryKey) {
        return courseDao.existCoursesForRepositoryEntry(repositoryEntryKey);
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

    public List<Semester> getSemestersInAscendingOrder() {
        return semesterDao.getSemestersInAscendingOrder();
    }

    public Semester getCurrentSemester() {
        return semesterDao.getCurrentSemester();
    }

    public void setCurrentSemester(Long semesterId) {
        semesterDao.setCurrentSemester(semesterId);
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

    public List<LecturerIdCourseId> getAllNotUpdatedLCBookingOfCurrentImportProcess(Date date, Semester semesterOfCurrentImportProcess) {
        return lecturerCourseDao.getAllNotUpdatedLCBookingOfCurrentImportProcess(date, semesterOfCurrentImportProcess);
    }

    public int deleteLCBookingByLecturerIdCourseIds(List<LecturerIdCourseId> lecturerIdCourseIds) {
        return lecturerCourseDao.deleteByLecturerIdCourseIdsAsBulkDelete(lecturerIdCourseIds);
    }

    public int deleteAllLCBookingTooFarInThePast(Date date) {
        return lecturerCourseDao.deleteAllLCBookingTooFarInThePastAsBulkDelete(date);
    }

    public List<StudentIdCourseId> getAllNotUpdatedSCBookingOfCurrentImportProcess(Date date, Semester semesterOfCurrentImportProcess) {
        return studentCourseDao.getAllNotUpdatedSCBookingOfCurrentImportProcess(date, semesterOfCurrentImportProcess);
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

    public Set<Course> getNotCreatedCourses(Identity identity, SapUserType userType, String searchString) {
        Set<Course> courses = new HashSet<>();
        courses.addAll(getCourses(identity, userType, false, searchString));// false --- > notCreatedCourses
        // THE CASE OF LECTURER, ADD THE APPROPRIATE DELEGATES
        if (userType.equals(SapUserType.LECTURER)) {
            List<Delegation> delegations = delegationDao.getDelegationsByDelegatee(identity.getKey());
            for (Delegation delegation : delegations) {
                courses.addAll(getCourses(delegation.getDelegator(), userType, false, searchString));// false --- > notCreatedCourses
            }
        }
        return courses;
    }

    public Set<Course> getCreatedCourses(Identity identity, SapUserType userType, String searchString) {
        Set<Course> courses = new HashSet<>();
        courses.addAll(getCourses(identity, userType, true, searchString));// true --- > CreatedCourses
        // THE CASE OF LECTURER, ADD THE APPROPRIATE DELEGATES
        if (userType.equals(SapUserType.LECTURER)) {
            List<Delegation> delegations = delegationDao.getDelegationsByDelegatee(identity.getKey());
            for (Delegation delegation : delegations) {
                courses.addAll(getCourses(delegation.getDelegator(), userType, true, searchString));// true --- > CreatedCourses
            }
        }
        return courses;
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

    public void saveCampusCourseRepositoryEntry(Long courseId, Long repositoryEntryKey) {
        courseDao.saveRepositoryEntry(courseId, repositoryEntryKey);
    }

    public void saveCampusGroupA(Long courseId, Long campusGroupAKey) {
        courseDao.saveCampusGroupA(courseId, campusGroupAKey);
    }

    public void saveCampusGroupB(Long courseId, Long campusGroupBKey) {
        courseDao.saveCampusGroupB(courseId, campusGroupBKey);
    }

    public Course getLatestCourseByRepositoryEntry(Long repositoryEntryKey) throws Exception {
        return courseDao.getLatestCourseByRepositoryEntry(repositoryEntryKey);
    }

    public Set<CampusGroups> getCampusGroupsByRepositoryEntry(Long repositoryEntryKey) {
        return courseDao.getCampusGroupsByRepositoryEntry(repositoryEntryKey);
    }

    public void resetRepositoryEntryAndParentCourse(Long repositoryEntryKey) {
        courseDao.resetRepositoryEntryAndParentCourse(repositoryEntryKey);
    }

    public void resetCampusGroup(Long campusGroupKey) {
        courseDao.resetCampusGroup(campusGroupKey);
    }

    public void saveParentCourseId(Long courseId, Long parentCourseId) {
        courseDao.saveParentCourseId(courseId, parentCourseId);
    }

    public List<Long> getSapIdsOfAllCreatedOlatCampusCourses() {
        return courseDao.getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemester();
    }

    public List<Long> getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemesters() {
        return courseDao.getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemestersNotTooFarInThePast();
    }

    public List<Long> getSapIdsOfAllNotCreatedOlatCampusCourses() {
        return courseDao.getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester();
    }

    public List<Long> getIdsOfAllEnabledOrgs() {
        return orgDao.getIdsOfAllEnabledOrgs();
    }

    public List<Course> getAllCreatedSapCources() {
        return courseDao.getAllCreatedCoursesOfCurrentSemester();
    }

    public CampusCourseTO loadCampusCourseTO(long courseId) {
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

        return new CampusCourseTO(
                course.getTitleToBeDisplayed(),
				course.getSemester(),
				dataConverter.convertLecturersToIdentities(lecturerCourses),
                dataConverter.convertDelegateesToIdentities(lecturerCourses),
				dataConverter.convertStudentsToIdentities(studentCourses),
                course.isContinuedCourse(),
                course.getTitlesOfCourseAndParentCoursesInAscendingOrder(),
                textDao.getContentsByCourseId(course.getId()),
                course.getRepositoryEntry(),
                new CampusGroups(course.getCampusGroupA(), course.getCampusGroupB()),
                course.getId(),
                course.getLanguage(),
                course.getVvzLink());
    }

    public CampusCourseTOForUI loadCampusCourseTOForUI(long courseId) {
        Course course = getCourseById(courseId);
        return new CampusCourseTOForUI(course.getTitleToBeDisplayed(), courseId);
    }

    private boolean areStudentCourseBookingsForCurrentSemesterUpToDate(Course course) {
        // i)  If we have no parent course (i.e. it is not a continued course) we assume that the student course booking
        //     is always up-to-date.
        // ii) If we have a parent course (i.e. it is a continued course) we require that at least 50% of the bookings
        //     of the previous semester must be students who also booked the course in the current semester. Otherwise
        //     the secretariat seems not have (manually) copied all the (permitted) students of the parent course yet.
        return (course.getParentCourse() == null || studentDao.hasMoreThan50PercentOfStudentsOfSpecificCourseBothABookingOfCourseAndParentCourse(course));
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

    public Semester getSemesterOfMostRecentCourseImport() {
        return courseDao.getSemesterOfMostRecentCourseImport();
    }
}
