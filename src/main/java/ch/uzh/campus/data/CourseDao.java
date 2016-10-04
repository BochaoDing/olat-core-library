package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@Repository
public class CourseDao implements CampusDao<CourseOrgId> {

    private static final OLog LOG = Tracing.createLoggerFor(CourseDao.class);

	private final DB dbInstance;
    private final CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    public CourseDao(DB dbInstance, CampusCourseConfiguration campusCourseConfiguration) {
        this.dbInstance = dbInstance;
        this.campusCourseConfiguration = campusCourseConfiguration;
    }

    public void save(Course course) {
        dbInstance.saveObject(course);
    }

    public void saveOrUpdate(Course course) {
        dbInstance.getCurrentEntityManager().merge(course);
    }

    public void save(CourseOrgId courseOrgId) {
        Course course = new Course();
		courseOrgId.merge(course);
        dbInstance.saveObject(course);
        updateOrgsFromCourseOrgId(course, courseOrgId);
    }

    public void saveOrUpdate(CourseOrgId courseOrgId) {
		/*
		 * A database merge with a detached course entity would override the
		 * "olat_id" attribute values with "null".
		 */
		Course course = getCourseById(courseOrgId.getId());
		if (course == null) {
			save(courseOrgId);
            return;
		}
		courseOrgId.merge(course);
		updateOrgsFromCourseOrgId(course, courseOrgId);
    }

    @Override
    public void save(List<CourseOrgId> courseOrgIds) {
        for (CourseOrgId courseOrgId : courseOrgIds) {
            save(courseOrgId);
        }
    }

    @Override
    public void saveOrUpdate(List<CourseOrgId> courseOrgIds) {
        for (CourseOrgId courseOrgId : courseOrgIds) {
            saveOrUpdate(courseOrgId);
        }
    }

    private void updateOrgsFromCourseOrgId(Course course, CourseOrgId courseOrgId) {
        // Remove org from course if it is not present any more
        Iterator<Org> iterator = course.getOrgs().iterator();
        while (iterator.hasNext()) {
            Org org = iterator.next();
            if (!org.getId().equals(courseOrgId.getOrg1())
                    && !org.getId().equals(courseOrgId.getOrg2())
                    && !org.getId().equals(courseOrgId.getOrg3())
                    && !org.getId().equals(courseOrgId.getOrg4())
                    && !org.getId().equals(courseOrgId.getOrg5())
                    && !org.getId().equals(courseOrgId.getOrg6())
                    && !org.getId().equals(courseOrgId.getOrg7())
                    && !org.getId().equals(courseOrgId.getOrg8())
                    && !org.getId().equals(courseOrgId.getOrg9())) {
                org.getCourses().remove(course);
                iterator.remove();
            }
        }

        // Add org to course if it is not present yet
        List<Long> orgIdsOfCourse = course.getOrgs().stream().map(Org::getId).collect(Collectors.toList());
        if (courseOrgId.getOrg1() != null && !orgIdsOfCourse.contains(courseOrgId.getOrg1())) {
            addOrgToCourse(courseOrgId.getOrg1(), course);
        }
        if (courseOrgId.getOrg2() != null && !orgIdsOfCourse.contains(courseOrgId.getOrg2())) {
            addOrgToCourse(courseOrgId.getOrg2(), course);
        }
        if (courseOrgId.getOrg3() != null && !orgIdsOfCourse.contains(courseOrgId.getOrg3())) {
            addOrgToCourse(courseOrgId.getOrg3(), course);
        }
        if (courseOrgId.getOrg4() != null && !orgIdsOfCourse.contains(courseOrgId.getOrg4())) {
            addOrgToCourse(courseOrgId.getOrg4(), course);
        }
        if (courseOrgId.getOrg5() != null && !orgIdsOfCourse.contains(courseOrgId.getOrg5())) {
            addOrgToCourse(courseOrgId.getOrg5(), course);
        }
        if (courseOrgId.getOrg6() != null && !orgIdsOfCourse.contains(courseOrgId.getOrg6())) {
            addOrgToCourse(courseOrgId.getOrg6(), course);
        }
        if (courseOrgId.getOrg7() != null && !orgIdsOfCourse.contains(courseOrgId.getOrg7())) {
            addOrgToCourse(courseOrgId.getOrg7(), course);
        }
        if (courseOrgId.getOrg8() != null && !orgIdsOfCourse.contains(courseOrgId.getOrg8())) {
            addOrgToCourse(courseOrgId.getOrg8(), course);
        }
        if (courseOrgId.getOrg9() != null && !orgIdsOfCourse.contains(courseOrgId.getOrg9())) {
            addOrgToCourse(courseOrgId.getOrg9(), course);
        }
    }

    private void addOrgToCourse(Long orgId, Course course) {
        Org org = dbInstance.findObject(Org.class, orgId);
        if (org != null) {
            org.getCourses().add(course);
            course.getOrgs().add(org);
        } else {
            String warningMessage = "No org found with id " + orgId + " for entry " + course.getId() + " of table ck_course.";
            LOG.warn(warningMessage);
        }
    }

    Course getCourseById(Long id) {
        return dbInstance.findObject(Course.class, id);
    }

    List<Course> getCreatedCoursesOfCurrentSemesterByLecturerId(Long lecturerId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, Course.class)
                .setParameter("lecturerId", lecturerId)
				.setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList();
    }

    List<Course> getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(Long lecturerId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, Course.class)
                .setParameter("lecturerId", lecturerId)
				.setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList();
    }

    List<Course> getCreatedCoursesOfCurrentSemesterByStudentId(Long studentId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
				.setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList(); 
    }

    List<Course> getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(Long studentId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE, Course.class)
                .setParameter("studentId", studentId)
                .setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList();
    }

    List<Course> getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(Long studentId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
				.setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList(); 
    }

    Course getLatestCourseByOlatResource(Long olatResourceKey) throws Exception {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_LATEST_COURSE_BY_OLAT_RESOURCE_KEY, Course.class)
                .setParameter("olatResourceKey", olatResourceKey)
                .getSingleResult();
    }

	private static String getWildcardLikeSearchString(String searchString) {
		return searchString != null ? "%" + searchString + "%" : "%";
	}

    void delete(Course course) {
        deleteCourseBidirectionally(course, dbInstance.getCurrentEntityManager());
    }

    void saveOlatResource(Long courseId, Long olatResourceKey) {
        Course course = getCourseById(courseId);
        if (course == null) {
            String warningMessage = "No course found with id " + courseId + ". Cannot save olat resource with id " + olatResourceKey;
            LOG.warn(warningMessage);
            return;
        }
        EntityManager em = dbInstance.getCurrentEntityManager();
        OLATResource olatResource = em.find(OLATResourceImpl.class, olatResourceKey);
        if (olatResource == null) {
            LOG.warn("No olat resource found with id " + olatResourceKey + ". Cannot save olat resource.");
            return;
        }
        course.setOlatResource(olatResource);
    }

    void disableSynchronization(Long courseId) {
        Course course = getCourseById(courseId);
        if (course == null) {
            String warningMessage = "No course found with id " + courseId + ". Cannot disable synchronization;";
            LOG.warn(warningMessage);
            return;
        }
        course.setSynchronizable(false);
    }

    void resetOlatResourceAndParentCourse(Long olatResourceKey) {
        List<Course> courses = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_COURSES_BY_OLAT_RESOURCE_KEY, Course.class)
                .setParameter("olatResourceKey", olatResourceKey)
                .getResultList();
        if (courses.isEmpty()) {
            String warningMessage = "No courses found with olat resource id " + olatResourceKey + ".";
            LOG.warn(warningMessage);
            return;
        }
        for (Course course : courses) {
            course.setOlatResource(null);
            course.removeParentCourse();
        }
    }

    void saveParentCourseId(Long courseId, Long parentCourseId) {
        Course course = getCourseById(courseId);
        Course parentCourse = getCourseById(parentCourseId);
        if (course == null) {
            String warningMessage = "No course found with id " + courseId + ". Cannot save parent course id;";
            LOG.warn(warningMessage);
            return;
        }
        if (parentCourse == null) {
            String warningMessage = "No parent course found with id " + parentCourseId + ". Cannot save parent course id;";
            LOG.warn(warningMessage);
            return;
        }
        course.setParentCourse(parentCourse);
    }

    /**
     * Deletes also according entries of the join tables ck_lecturer_course, ck_student_course and ck_course_org and of the related tables ck_text and ck_event.
     * We cannot use a bulk delete here, since deleting the join table ck_course_org is not possible.
     */
    void deleteByCourseId(Long courseId) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        deleteCourseBidirectionally(dbInstance.getCurrentEntityManager().getReference(Course.class, courseId), em);
    }

    /**
     * Deletes also according entries of the join tables ck_lecturer_course, ck_student_course and ck_course_org and of the related tables ck_text and ck_event.
     * We cannot use a bulk delete here, since deleting the join table ck_course_org is not possible.
     */
    void deleteByCourseIds(List<Long> courseIds) {
        int count = 0;
        EntityManager em = dbInstance.getCurrentEntityManager();
        for (Long courseId : courseIds) {
            deleteCourseBidirectionally(em.getReference(Course.class, courseId), em);
            // Avoid memory problems caused by loading too many objects into the persistence context
            // (cf. C. Bauer and G. King: Java Persistence mit Hibernate, 2nd edition, p. 477)
            if (++count % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
    }
    
    List<Long> getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemester() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_IDS_OF_ALL_CREATED_SYNCHRONIZABLE_COURSES_OF_CURRENT_SEMESTER, Long.class)
                .getResultList();
    }

    List<Long> getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters(List<String> shortSemesters) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_OLAT_RESOURCE_KEYS_OF_ALL_CREATED_NOT_CONTINUED_COURSES_OF_SPECIFIC_SEMESTERS, Long.class)
                .setParameter("shortSemesters", shortSemesters)
                .getResultList();
    }

    List<Long> getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfPreviousSemestersNotTooFarInThePast() {
        List<String> previousShortSemestersNotTooFarInThePast = getPreviousShortSemestersNotTooFarInThePast();
        if (previousShortSemestersNotTooFarInThePast.isEmpty()) {
            return new ArrayList<>();
        }
        return getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters(previousShortSemestersNotTooFarInThePast);
    }

    List<Long> getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_IDS_OF_ALL_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER, Long.class)
                .getResultList();
    }

    List<Course> getAllCreatedCoursesOfCurrentSemester() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER, Course.class)
                .getResultList();
    }

    List<Long> getAllNotCreatedOrphanedCourses() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_NOT_CREATED_ORPHANED_COURSES, Long.class)
                .getResultList();
    }

    boolean existCoursesForOlatResource(Long olatResourceKey) {
        List<Long> courseIds = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_COURSE_IDS_BY_OLAT_RESOURCE_KEY, Long.class)
                .setParameter("olatResourceKey", olatResourceKey)
                .getResultList();
        return !courseIds.isEmpty();
    }

    List<Course> getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(Long lecturerId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, Course.class)
                .setParameter("lecturerId", lecturerId)
                .getResultList();
    }

    List<Course> getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(Long studentId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    List<Course> getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(Long studentId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE, Course.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    List<String> getPreviousShortSemestersNotTooFarInThePast() {
        List<String> shortSemesters = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_SHORT_SEMESTERS_IN_DESCENDING_ORDER, String.class)
                .getResultList();
        int indexOfOldestSemesterNotTooFarInThePast = campusCourseConfiguration.getMaxYearsToKeepCkData() * 2 + 1;
        if (shortSemesters.size() < 2 || indexOfOldestSemesterNotTooFarInThePast < 1) {
            return new ArrayList<>();
        }
        return shortSemesters.subList(1, Math.min(indexOfOldestSemesterNotTooFarInThePast, shortSemesters.size()));
    }

    String getPreviousShortSemester() {
        List<String> shortSemesters = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_SHORT_SEMESTERS_IN_DESCENDING_ORDER, String.class)
                .getResultList();
        if (shortSemesters.size() < 2) {
            return null;
        }
        return shortSemesters.get(1);
    }

    private void deleteCourseBidirectionally(Course course, EntityManager em) {
        // Delete join table entries
        for (LecturerCourse lecturerCourse : course.getLecturerCourses()) {
            lecturerCourse.getLecturer().getLecturerCourses().remove(lecturerCourse);
            // Use em.remove() instead of dbInstance.deleteObject() since the latter calls dbInstance.getCurrentEntityManager()
            // at every call, which may has an impact on the performance
            em.remove(lecturerCourse);
        }
        for (StudentCourse studentCourse : course.getStudentCourses()) {
            studentCourse.getStudent().getStudentCourses().remove(studentCourse);
            em.remove(studentCourse);
        }
        for (Org org : course.getOrgs()) {
            org.getCourses().remove(course);
        }
        em.remove(course);
    }
}
