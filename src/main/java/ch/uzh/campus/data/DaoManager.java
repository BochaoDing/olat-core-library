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
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DaoManager {
    @Autowired
    DB dbInstance;

    @Autowired
    private CourseDao courseDao;
    @Autowired
    private StudentDao studentDao;
    @Autowired
    private LecturerCourseDao lecturerCourseDao;
    @Autowired
    private StudentCourseDao studentCourseDao;
    @Autowired
    private LecturerDao lecturerDao;
    @Autowired
    private SapOlatUserDao sapOlatUserDao;
    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private TextDao textDao;
    @Autowired
    private EventDao eventDao;
    @Autowired
    private OrgDao orgDao;
    @Autowired
    protected ImportStatisticDao statisticDao;
    @Autowired
    DataConverter dataConverter;

    @Autowired
    private CampusCourseConfiguration campusCourseConfiguration;
    
    @Value("${campus.lv_kuerzel.activated}")
    private String shortTitleActivated;
    

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

    public void saveSapOlatUsers(List<SapOlatUser> sapOlatUsers) {
        sapOlatUserDao.save(sapOlatUsers);
    }

    public void saveDelegation(Identity delegator, Identity delegatee) {
        Delegation delegation = new Delegation();
        delegation.setDelegator(delegator.getName());
        delegation.setDelegatee(delegatee.getName());
        delegation.setModifiedDate(new Date());
        delegationDao.save(delegation);
    }

    public boolean existDelegation(Identity delegator, Identity delegatee) {
        return delegationDao.existDelegation(delegator.getName(), delegatee.getName());
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
                sapOlatUserDao.deleteMappingBySapStudentIdsAsBulkDelete(subList);
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
                sapOlatUserDao.deleteMappingBySapLecturerIdsAsBulkDelete(subList);
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

    public List<Long> getAllStudentsToBeDeleted() {
        return studentDao.getAllOrphanedStudents();
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

    public List<Long> getAllLecturersToBeDeleted() {
        return lecturerDao.getAllOrphanedLecturers();
    }

    public List<Long> getAllOrgsToBeDeleted() {
        return orgDao.getAllOrphanedOrgs();
    }

    public void deleteOrgByIds(List<Long> orgIds) {
        orgDao.deleteByOrgIds(orgIds);
    }

    public SapOlatUser getStudentSapOlatUserByOlatUserName(String olatUserName) {
        return sapOlatUserDao.getSapOlatUserByOlatUserNameAndSapUserType(olatUserName, SapOlatUser.SapUserType.STUDENT);
    }

    public SapOlatUser getLecturerSapOlatUserByOlatUserName(String olatUserName) {
        return sapOlatUserDao.getSapOlatUsersByOlatUserNameAndSapUserType(olatUserName, SapOlatUser.SapUserType.LECTURER);
    }

    public List<SapOlatUser> getSapOlatUserListByOlatUserName(String olatUserName) {
        return sapOlatUserDao.getSapOlatUserListByOlatUserName(olatUserName);
    }

    public SapOlatUser getSapOlatUserBySapUserId(Long sapUserId) {
        return sapOlatUserDao.getSapOlatUserBySapUserId(sapUserId);
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

    public List<Course> getCreatedAndNotCreatedCreatableCoursesByStudentId(Long id) {
        return courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(id);
    }

    public List<Course> getCreatedCoursesByStudentId(Long id, String searchString) {
        return courseDao.getCreatedCoursesOfCurrentSemesterByStudentId(id, searchString);
    }

    public List<Course> getNotCreatedCoursesByStudentId(Long id, String searchString) {
        return courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(id, searchString);
    }

    public List<Course> getCreatedAndNotCreatedCreatableCoursesByLecturerId(Long id) {
        return courseDao.getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(id);
    }

    public List<Course> getCreatedCoursesByLecturerIds(Long id, String searchString) {
        return courseDao.getCreatedCoursesOfCurrentSemesterByLecturerId(id, searchString);
    }

    public List<Course> getNotCreatedCoursesByLecturerId(Long id, String searchString) {
        return courseDao.getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(id, searchString);
    }

    public Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapOlatUser.SapUserType userType, String searchString) {
        Set<Course> coursesWithoutResourceableId = new HashSet<>();
        coursesWithoutResourceableId.addAll(getCourses(identity.getName(), userType, false, searchString));// false --- > notCreatedCourses
        // THE CASE OF LECTURER, ADD THE APPROPRIATE DELEGATES
        if (userType.equals(SapOlatUser.SapUserType.LECTURER)) {
            List<Delegation> delegations = delegationDao.getDelegationsByDelegatee(identity.getName());
            for (Delegation delegation : delegations) {
                coursesWithoutResourceableId.addAll(getCourses(delegation.getDelegator(), userType, false, searchString));// false --- > notCreatedCourses
            }
        }
        return coursesWithoutResourceableId;
    }

    public Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapOlatUser.SapUserType userType, String searchString) {
        Set<Course> coursesWithResourceableId = new HashSet<>();
        coursesWithResourceableId.addAll(getCourses(identity.getName(), userType, true, searchString));// true --- > CreatedCourses
        // THE CASE OF LECTURER, ADD THE APPROPRIATE DELEGATES
        if (userType.equals(SapOlatUser.SapUserType.LECTURER)) {
            List<Delegation> delegations = delegationDao.getDelegationsByDelegatee(identity.getName());
            for (Delegation delegation : delegations) {
                coursesWithResourceableId.addAll(getCourses(delegation.getDelegator(), userType, true, searchString));// true --- > CreatedCourses
            }
        }
        return coursesWithResourceableId;
    }

    public List<Course> getCourses(String olatUserName, SapOlatUser.SapUserType userType, boolean created, String searchString) {
        if (userType.equals(SapOlatUser.SapUserType.LECTURER)) {
            SapOlatUser sapOlatUser = getLecturerSapOlatUserByOlatUserName(olatUserName);
            if (sapOlatUser == null) {
                return Collections.emptyList();
            } else {
                return (created) ? getCreatedCoursesByLecturerIds(sapOlatUser.getSapUserId(), searchString) : getNotCreatedCoursesByLecturerId(sapOlatUser.getSapUserId(), searchString);
            }
        } else {
            SapOlatUser sapOlatUser = getStudentSapOlatUserByOlatUserName(olatUserName);
            if (sapOlatUser == null) {
                return Collections.emptyList();
            } else {
                return (created) ? getCreatedCoursesByStudentId(sapOlatUser.getSapUserId(), searchString) : getNotCreatedCoursesByStudentId(sapOlatUser.getSapUserId(), searchString);
            }
        }
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

    public List<Long> getResourceableIdsOfAllCreatedCoursesOfPreviousSemesters() {
        return courseDao.getResourceableIdsOfAllCreatedCoursesOfPreviousSemestersNotTooFarInThePast();
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

        // Determine lecturerCourses and studentCourses of course and parent courses
        Set<LecturerCourse> lecturerCoursesOfCourseAndParentCourses;
        Set<StudentCourse> studentCoursesOfCourseAndParentCourses;
        Course courseIt = course;
        do {
            lecturerCoursesOfCourseAndParentCourses = courseIt.getLecturerCourses();
            studentCoursesOfCourseAndParentCourses = courseIt.getStudentCourses();
            courseIt = course.getParentCourse();
        } while (course.getParentCourse() != null);

        return new CampusCourseImportTO(course.getTitleToBeDisplayed(shortTitleActivated),
				course.getSemester(),
				dataConverter.convertLecturersToIdentities(lecturerCoursesOfCourseAndParentCourses),
                dataConverter.convertDelegateesToIdentities(lecturerCoursesOfCourseAndParentCourses),
				dataConverter.convertStudentsToIdentities(studentCoursesOfCourseAndParentCourses),
                textDao.getContentsByCourseId(course.getId()), course.getResourceableId(),
				course.getId(), course.getLanguage(), course.getVvzLink());
    }

    public List getDelegatees(Identity delegator) {
        return dataConverter.getDelegatees(delegator);
    }

    public void deleteDelegation(Identity delegator, Identity delegatee) {
        delegationDao.deleteByDelegatorAndDelegatee(delegator.getName(), delegatee.getName());
    }

    public boolean checkImportedData() {
        return (statisticDao.getLastCompletedImportedStatistic().size() == campusCourseConfiguration.getMustCompletedImportedFiles());
    }

    public void deleteOldLecturerMapping() {
        sapOlatUserDao.deleteOldLecturerMappingAsBulkDelete();
    }

    public void deleteOldStudentMapping() {
        sapOlatUserDao.deleteOldStudentMappingAsBulkDelete();
    }
}
