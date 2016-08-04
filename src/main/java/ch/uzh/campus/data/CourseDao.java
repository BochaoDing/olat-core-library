package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

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

    @Autowired
    public CourseDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(Course course) {
        dbInstance.saveObject(course);
    }

    public void saveOrUpdate(Course course) {
        dbInstance.getCurrentEntityManager().merge(course);
    }

    public void save(CourseOrgId courseOrgId) {
        Course course = new Course(courseOrgId);
        dbInstance.saveObject(course);
        updateOrgsFromCourseOrgId(course, courseOrgId);
    }

    public void saveOrUpdate(CourseOrgId courseOrgId) {
        Course course = new Course(courseOrgId);
        course = dbInstance.getCurrentEntityManager().merge(course);
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
        for (Org org : course.getOrgs()) {
            if (!org.getId().equals(courseOrgId.getOrg1())
                    && !org.getId().equals(courseOrgId.getOrg2())
                    && !org.getId().equals(courseOrgId.getOrg3())
                    && !org.getId().equals(courseOrgId.getOrg4())
                    && !org.getId().equals(courseOrgId.getOrg5())
                    && !org.getId().equals(courseOrgId.getOrg6())
                    && !org.getId().equals(courseOrgId.getOrg7())
                    && !org.getId().equals(courseOrgId.getOrg8())
                    && !org.getId().equals(courseOrgId.getOrg9()))
                removeOrgFromCourse(org, course);
        }

        // Add new orgs
        if (courseOrgId.getOrg1() != null) {
            addOrgToCourse(courseOrgId.getOrg1(), course);
        }
        if (courseOrgId.getOrg2() != null) {
            addOrgToCourse(courseOrgId.getOrg2(), course);
        }
        if (courseOrgId.getOrg3() != null) {
            addOrgToCourse(courseOrgId.getOrg3(), course);
        }
        if (courseOrgId.getOrg4() != null) {
            addOrgToCourse(courseOrgId.getOrg4(), course);
        }
        if (courseOrgId.getOrg5() != null) {
            addOrgToCourse(courseOrgId.getOrg5(), course);
        }
        if (courseOrgId.getOrg6() != null) {
            addOrgToCourse(courseOrgId.getOrg6(), course);
        }
        if (courseOrgId.getOrg7() != null) {
            addOrgToCourse(courseOrgId.getOrg7(), course);
        }
        if (courseOrgId.getOrg8() != null) {
            addOrgToCourse(courseOrgId.getOrg8(), course);
        }
        if (courseOrgId.getOrg9() != null) {
            addOrgToCourse(courseOrgId.getOrg9(), course);
        }
    }

    private void addOrgToCourse(Long orgId, Course course) {
        Org org = dbInstance.getCurrentEntityManager().find(Org.class, orgId);
        if (org != null) {
            org.getCourses().add(course);
            course.getOrgs().add(org);
        } else {
            String warningMessage = "No org found with id " + orgId + " for entry " + course.getId() + " of table ck_course.";
            LOG.warn(warningMessage);
        }
    }

    private void removeOrgFromCourse(Org org, Course course) {
        org.getCourses().remove(course);
        course.getOrgs().remove(org);
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

    List<Course> getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(Long studentId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
				.setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList(); 
    }

    Course getLatestCourseByResourceable(Long resourceableId) throws Exception {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_LATEST_COURSE_BY_RESOURCEABLE_ID, Course.class)
                .setParameter("resourceableId", resourceableId)
                .getSingleResult();
    }

	private static String getWildcardLikeSearchString(String searchString) {
		return searchString != null ? "%" + searchString + "%" : "%";
	}

    void delete(Course course) {
        deleteCourseBidirectionally(course, dbInstance.getCurrentEntityManager());
    }

    void saveResourceableId(Long courseId, Long resourceableId) {
        Course course = getCourseById(courseId);
        if (course == null) {
            String warningMessage = "No course found with id " + courseId + ". Cannot save resourcable id;";
            LOG.warn(warningMessage);
            return;
        }
        course.setResourceableId(resourceableId);
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

    void resetResourceableIdAndParentCourse(Long resourceableId) {
        List<Course> courses =dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_COURSES_BY_RESOURCEABLE_ID, Course.class)
                .setParameter("resourceableId", resourceableId)
                .getResultList();
        if (courses.isEmpty()) {
            String warningMessage = "No courses found with resourcable id " + resourceableId + ".";
            LOG.warn(warningMessage);
            return;
        }
        for (Course course : courses) {
            course.setResourceableId(null);
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

    List<Long> getResourceableIdsOfAllCreatedCoursesOfCurrentSemester() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_RESOURCEABLE_IDS_OF_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER, Long.class)
                .getResultList();
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

    boolean existResourceableId(Long resourceableId) {
        List<Long> courseIds = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_COURSE_IDS_BY_RESOURCEABLE_ID, Long.class)
                .setParameter("resourceableId", resourceableId)
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
