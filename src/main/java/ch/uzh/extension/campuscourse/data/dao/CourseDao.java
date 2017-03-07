package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.common.CampusCourseException;
import ch.uzh.extension.campuscourse.data.entity.*;
import ch.uzh.extension.campuscourse.model.CampusGroups;
import ch.uzh.extension.campuscourse.model.CourseSemesterOrgId;
import ch.uzh.extension.campuscourse.util.DateUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@Repository
public class CourseDao {

    private static final OLog LOG = Tracing.createLoggerFor(CourseDao.class);

	private final CampusCourseConfiguration campusCourseConfiguration;
	private final DB dbInstance;
    private final SemesterDao semesterDao;
    private final BatchJobAndSapImportStatisticDao batchJobAndSapImportStatisticDao;

    @Autowired
    public CourseDao(CampusCourseConfiguration campusCourseConfiguration, DB dbInstance, SemesterDao semesterDao, BatchJobAndSapImportStatisticDao batchJobAndSapImportStatisticDao) {
		this.campusCourseConfiguration = campusCourseConfiguration;
		this.dbInstance = dbInstance;
        this.semesterDao = semesterDao;
		this.batchJobAndSapImportStatisticDao = batchJobAndSapImportStatisticDao;
	}

    public void save(Course course) {
		course.setDateOfFirstImport(course.getDateOfLatestImport());
    	dbInstance.saveObject(course);
    }

    public void save(CourseSemesterOrgId courseSemesterOrgId) throws CampusCourseException {
        Course course = new Course();
		courseSemesterOrgId.mergeImportedAttributesInto(course);
		course.setId(courseSemesterOrgId.getId());
        Semester semester = findOrCreateSemester(courseSemesterOrgId);
        course.setSemester(semester);
        save(course);
        updateOrgsFromCourseOrgId(course, courseSemesterOrgId);
    }

	public void save(List<CourseSemesterOrgId> courseSemesterOrgIds) throws CampusCourseException {
		for (CourseSemesterOrgId courseSemesterOrgId : courseSemesterOrgIds) {
			save(courseSemesterOrgId);
		}
	}

    public void saveOrUpdate(CourseSemesterOrgId courseSemesterOrgId) throws CampusCourseException {
		/*
		 * A database merge with a detached course entity would override the
		 * "olat_id" attribute values with "null".
		 */
		Course course = getCourseById(courseSemesterOrgId.getId());
		if (course != null) {
			courseSemesterOrgId.mergeImportedAttributesInto(course);
			updateOrgsFromCourseOrgId(course, courseSemesterOrgId);
		} else {
			save(courseSemesterOrgId);
		}
    }

    private Semester findOrCreateSemester(CourseSemesterOrgId courseSemesterOrgId) throws CampusCourseException {
        Semester semester = semesterDao.getSemesterBySemesterNameAndYear(courseSemesterOrgId.getSemesterName(), courseSemesterOrgId.getSemesterYear());
        if (semester == null) {
            // Create new semester
            if (courseSemesterOrgId.getSemesterName() == null || courseSemesterOrgId.getSemesterYear() == null || courseSemesterOrgId.getSemesterYear() < 1000 || courseSemesterOrgId.getSemesterYear() > 9999) {
                String errorMessage = "Course has invalid semester: " + courseSemesterOrgId.getSemester() + ". Cannot save course with id " + courseSemesterOrgId.getId() + ".";
                // Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by CampusWriter)
                LOG.debug(errorMessage);
                throw new CampusCourseException(errorMessage);
            }
            semester = new Semester(courseSemesterOrgId.getSemesterName(), courseSemesterOrgId.getSemesterYear(), false);
            semesterDao.save(semester);
        }
        return semester;
    }

    private void updateOrgsFromCourseOrgId(Course course, CourseSemesterOrgId courseSemesterOrgId) {
        // Remove org from course if it is not present any more
        Iterator<Org> iterator = course.getOrgs().iterator();
        while (iterator.hasNext()) {
            Org org = iterator.next();
            if (!org.getId().equals(courseSemesterOrgId.getOrg1())
                    && !org.getId().equals(courseSemesterOrgId.getOrg2())
                    && !org.getId().equals(courseSemesterOrgId.getOrg3())
                    && !org.getId().equals(courseSemesterOrgId.getOrg4())
                    && !org.getId().equals(courseSemesterOrgId.getOrg5())
                    && !org.getId().equals(courseSemesterOrgId.getOrg6())
                    && !org.getId().equals(courseSemesterOrgId.getOrg7())
                    && !org.getId().equals(courseSemesterOrgId.getOrg8())
                    && !org.getId().equals(courseSemesterOrgId.getOrg9())) {
                org.getCourses().remove(course);
                iterator.remove();
            }
        }

        // Add org to course if it is not present yet
        List<Long> orgIdsOfCourse = course.getOrgs().stream().map(Org::getId).collect(Collectors.toList());
        if (courseSemesterOrgId.getOrg1() != null && !orgIdsOfCourse.contains(courseSemesterOrgId.getOrg1())) {
            addOrgToCourse(courseSemesterOrgId.getOrg1(), course);
        }
        if (courseSemesterOrgId.getOrg2() != null && !orgIdsOfCourse.contains(courseSemesterOrgId.getOrg2())) {
            addOrgToCourse(courseSemesterOrgId.getOrg2(), course);
        }
        if (courseSemesterOrgId.getOrg3() != null && !orgIdsOfCourse.contains(courseSemesterOrgId.getOrg3())) {
            addOrgToCourse(courseSemesterOrgId.getOrg3(), course);
        }
        if (courseSemesterOrgId.getOrg4() != null && !orgIdsOfCourse.contains(courseSemesterOrgId.getOrg4())) {
            addOrgToCourse(courseSemesterOrgId.getOrg4(), course);
        }
        if (courseSemesterOrgId.getOrg5() != null && !orgIdsOfCourse.contains(courseSemesterOrgId.getOrg5())) {
            addOrgToCourse(courseSemesterOrgId.getOrg5(), course);
        }
        if (courseSemesterOrgId.getOrg6() != null && !orgIdsOfCourse.contains(courseSemesterOrgId.getOrg6())) {
            addOrgToCourse(courseSemesterOrgId.getOrg6(), course);
        }
        if (courseSemesterOrgId.getOrg7() != null && !orgIdsOfCourse.contains(courseSemesterOrgId.getOrg7())) {
            addOrgToCourse(courseSemesterOrgId.getOrg7(), course);
        }
        if (courseSemesterOrgId.getOrg8() != null && !orgIdsOfCourse.contains(courseSemesterOrgId.getOrg8())) {
            addOrgToCourse(courseSemesterOrgId.getOrg8(), course);
        }
        if (courseSemesterOrgId.getOrg9() != null && !orgIdsOfCourse.contains(courseSemesterOrgId.getOrg9())) {
            addOrgToCourse(courseSemesterOrgId.getOrg9(), course);
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

    public Course getCourseById(Long id) {
        return dbInstance.findObject(Course.class, id);
    }

    public List<Course> getCreatedCoursesOfCurrentSemesterByLecturerId(Long lecturerId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, Course.class)
                .setParameter("lecturerId", lecturerId)
				.setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList();
    }

    public List<Course> getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(Long lecturerId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, Course.class)
                .setParameter("lecturerId", lecturerId)
				.setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList();
    }

    public List<Course> getCreatedCoursesOfCurrentSemesterByStudentId(Long studentId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
				.setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList(); 
    }

    public List<Course> getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(Long studentId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE, Course.class)
                .setParameter("studentId", studentId)
                .setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList();
    }

    public List<Course> getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(Long studentId, String searchString) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
				.setParameter("searchString", getWildcardLikeSearchString(searchString))
                .getResultList(); 
    }

	private static String getWildcardLikeSearchString(String searchString) {
		return searchString != null ? "%" + searchString + "%" : "%";
	}

	public Course getCourseOrLastChildOfContinuedCourseByRepositoryEntryKey(Long repositoryEntryKey) {
		Course course;
		try {
			course = dbInstance.getCurrentEntityManager()
					.createNamedQuery(Course.GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_REPOSITORY_ENTRY_KEY, Course.class)
					.setParameter("repositoryEntryKey", repositoryEntryKey)
					.getSingleResult();
		} catch (NoResultException e) {
			LOG.warn("No course found with repository entry id " + repositoryEntryKey + ".");
			return null;
		}

		while (course.getChildCourse() != null) {
			course = course.getChildCourse();
		}

		return course;
	}

    public Set<CampusGroups> getCampusGroupsByRepositoryEntry(Long repositoryEntryKey) {
        Course course;
        try {
			course = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_REPOSITORY_ENTRY_KEY, Course.class)
                .setParameter("repositoryEntryKey", repositoryEntryKey)
                .getSingleResult();
		} catch (NoResultException e) {
			LOG.warn("No course found with repository entry id " + repositoryEntryKey + ".");
			return new HashSet<>();
		}

		Set<CampusGroups> setOfCampusGroups = new HashSet<>();
		do {
			if (course.getCampusGroupA() != null || course.getCampusGroupB() != null) {
				setOfCampusGroups.add(new CampusGroups(course.getCampusGroupA(), course.getCampusGroupB()));
			}
			course = course.getChildCourse();
		} while (course != null);

        return setOfCampusGroups;
    }

    public void delete(Course course) {
        deleteCourseBidirectionally(course, dbInstance.getCurrentEntityManager());
    }

    public void saveRepositoryEntryAndDateOfOlatCourseCreation(Long courseId, Long repositoryEntryKey) {
        Course course = getCourseById(courseId);
        if (course == null) {
            String warningMessage = "No course found with id " + courseId + ". Cannot save repository entry with id " + repositoryEntryKey;
            LOG.warn(warningMessage);
            return;
        }
        EntityManager em = dbInstance.getCurrentEntityManager();
        RepositoryEntry repositoryEntry = em.find(RepositoryEntry.class, repositoryEntryKey);
        if (repositoryEntry == null) {
            LOG.warn("No repository entry found with id " + repositoryEntryKey + ". Cannot save repository entry.");
            return;
        }
        course.setRepositoryEntry(repositoryEntry);
        course.setDateOfOlatCourseCreation(new Date());
    }

    public void saveCampusGroupA(Long courseId, Long campusGroupAKey) {
        Course course = getCourseById(courseId);
        if (course == null) {
            String warningMessage = "No course found with id " + courseId + ". Cannot save campus group A with id " + campusGroupAKey;
            LOG.warn(warningMessage);
            return;
        }

        BusinessGroup campusGroupA = dbInstance.getCurrentEntityManager().find(BusinessGroupImpl.class, campusGroupAKey);
        if (campusGroupA == null) {
            LOG.warn("No business group found with id " + campusGroupAKey + ". Cannot save campus group A.");
            return;
        }
        course.setCampusGroupA(campusGroupA);
    }

    public void saveCampusGroupB(Long courseId, Long campusGroupBKey) {
        Course course = getCourseById(courseId);
        if (course == null) {
            String warningMessage = "No course found with id " + courseId + ". Cannot save campus group B with id " + campusGroupBKey;
            LOG.warn(warningMessage);
            return;
        }

        BusinessGroup campusGroupB = dbInstance.getCurrentEntityManager().find(BusinessGroupImpl.class, campusGroupBKey);
        if (campusGroupB == null) {
            LOG.warn("No business group found with id " + campusGroupBKey + ". Cannot save campus group B.");
            return;
        }
        course.setCampusGroupB(campusGroupB);
    }

    public void resetRepositoryEntryAndParentCoursesAndDateOfOlatCourseCreation(Long repositoryEntryKey) {
		Course course;
    	try {
			course = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_REPOSITORY_ENTRY_KEY, Course.class)
                .setParameter("repositoryEntryKey", repositoryEntryKey)
                .getSingleResult();
		} catch (NoResultException e) {
			// No such course, so we do not have to do anything
			return;
		}

		do {
    		course.setRepositoryEntry(null);
			course.removeParentCourse();
			course.setDateOfOlatCourseCreation(null);
			course = course.getChildCourse();
		} while (course != null);
    }

    public void resetCampusGroup(Long campusGroupKey) {
        // Look for courses with id of campus group A equals to campusGroupKey
		try {
			Course course = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_CAMPUS_GROUP_A_KEY, Course.class)
                .setParameter("campusGroupKey", campusGroupKey)
                .getSingleResult();
			do {
				course.setCampusGroupA(null);
				course = course.getChildCourse();
			} while (course != null);
		} catch (NoResultException e) {
			// No such course, so we do not have to do anything
		}

		// Look for courses with id of campus group B equals to campusGroupKey
		try {
			Course course = dbInstance.getCurrentEntityManager()
					.createNamedQuery(Course.GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_CAMPUS_GROUP_B_KEY, Course.class)
					.setParameter("campusGroupKey", campusGroupKey)
					.getSingleResult();
			do {
				course.setCampusGroupB(null);
				course = course.getChildCourse();
			} while (course != null);
		} catch (NoResultException e) {
			// No such course, so we do not have to do anything
		}
    }

    public void removeParentCourseAndResetDateOfOlatCourseCreation(Long childCourseId) {
    	Course childCourse = getCourseById(childCourseId);
    	if (childCourse == null) {
    		LOG.warn("No course found with id " + childCourseId + ". Cannot remove parent course and reset date of olat course creation.");
    		return;
		}
    	childCourse.removeParentCourse();
    	childCourse.setDateOfOlatCourseCreation(null);
	}

    public void saveParentCourseIdAndDateOfOlatCourseCreation(Long courseId, Long parentCourseId) {
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
        course.setDateOfOlatCourseCreation(new Date());
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
    public void deleteByCourseIds(List<Long> courseIds) {
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

    public List<Long> getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemester() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_IDS_OF_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER, Long.class)
                .getResultList();
    }

    List<Long> getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters(List<Long> semesterIds) {
    	if (semesterIds.isEmpty()) {
    		return new ArrayList<>();
		}
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_REPOSITORY_ENTRY_KEYS_OF_ALL_CREATED_NOT_CONTINUED_COURSES_OF_SPECIFIC_SEMESTERS, Long.class)
                .setParameter("semesterIds", semesterIds)
                .getResultList();
    }

    Set<Long> getRepositoryEntryKeysOfAllCreatedContinuedCoursesWithoutChildOfSpecificSemesters(List<Long> semesterIds) {
		if (semesterIds.isEmpty()) {
			return new HashSet<>();
		}
		List<Course> courses = dbInstance.getCurrentEntityManager()
				.createNamedQuery(Course.GET_ALL_CREATED_CONTINUED_COURSES_WITHOUT_CHILD_OF_SPECIFIC_SEMESTERS, Course.class)
				.setParameter("semesterIds", semesterIds)
				.getResultList();
		Set<Long> repositoryEntryKeys = new HashSet<>();
		for (Course course : courses) {
			// Determine first parent (containing the repository entry)
			while (course.getParentCourse() != null) {
				course = course.getParentCourse();
			}
			if (course.getRepositoryEntry() != null) {
				repositoryEntryKeys.add(course.getRepositoryEntry().getKey());
			}
		}
		return repositoryEntryKeys;
	}

    public Set<Long> getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfPreviousSemestersNotTooFarInThePast() {
        List<Long> previousSemestersNotTooFarInThePast = semesterDao.getPreviousSemestersNotTooFarInThePastInDescendingOrder();
        if (previousSemestersNotTooFarInThePast.isEmpty()) {
            return new HashSet<>();
        }
		Set<Long> repositoryEntryKeys = new HashSet<>();
        repositoryEntryKeys.addAll(getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters(previousSemestersNotTooFarInThePast));
        repositoryEntryKeys.addAll(getRepositoryEntryKeysOfAllCreatedContinuedCoursesWithoutChildOfSpecificSemesters(previousSemestersNotTooFarInThePast));
        return repositoryEntryKeys;
    }

    public List<Long> getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_IDS_OF_ALL_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER, Long.class)
                .getResultList();
    }

    public List<Course> getAllCreatedCoursesOfCurrentSemester() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER, Course.class)
                .getResultList();
    }

    public List<Long> getAllNotCreatedOrphanedCourses() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_ALL_NOT_CREATED_ORPHANED_COURSES, Long.class)
                .getResultList();
    }

    public boolean existCoursesForRepositoryEntry(Long repositoryEntryKey) {
		try {
			dbInstance.getCurrentEntityManager()
					.createNamedQuery(Course.GET_COURSE_ID_OR_ID_OF_FIRST_PARENT_OF_CONTINUED_COURSE_BY_REPOSITORY_ENTRY_KEY, Long.class)
					.setParameter("repositoryEntryKey", repositoryEntryKey)
					.getSingleResult();
			return true;
		} catch (NoResultException e) {
			return false;
		}
	}

	public boolean existsContinuedCourseForRepositoryEntry(Long repositoryEntryKey) {
		try {
			Course course = dbInstance.getCurrentEntityManager()
					.createNamedQuery(Course.GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_REPOSITORY_ENTRY_KEY, Course.class)
					.setParameter("repositoryEntryKey", repositoryEntryKey)
					.getSingleResult();
			return course.getChildCourse() != null;
		} catch (NoResultException e) {
			// Neither continued nor not continued course exists
			return false;
		}
	}

    public List<Course> getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId(Long lecturerId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, Course.class)
                .setParameter("lecturerId", lecturerId)
                .getResultList();
    }

    public List<Course> getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId(Long studentId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, Course.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    public List<Course> getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse(Long studentId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE, Course.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    List<Long> getIdsOfContinuedCoursesTooFarInThePast(Date date) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Course.GET_IDS_OF_CONTINUED_COURSES_TOO_FAR_IN_THE_PAST, Long.class)
                .setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
                .getResultList();
    }

    public Semester getSemesterOfMostRecentCourseImport() {

    	Date startTimeOfMostRecentCourseImport = batchJobAndSapImportStatisticDao.getStartTimeOfMostRecentCompletedCourseImport();
    	if (startTimeOfMostRecentCourseImport == null) {
    	    return null;
        }

		// Subtract one second to avoid rounding problems
		Calendar startTimeOfMostRecentCourseImportMinusOneSecond = Calendar.getInstance();
		startTimeOfMostRecentCourseImportMinusOneSecond.setTime(startTimeOfMostRecentCourseImport);
		startTimeOfMostRecentCourseImportMinusOneSecond.add(Calendar.SECOND, -1);

		List<Long> semesterIdsAsList = dbInstance.getCurrentEntityManager()
				.createNamedQuery(Course.GET_SEMESTER_IDS_OF_MOST_RECENT_COURSE_IMPORT, Long.class)
				.setParameter("startTimeOfMostRecentCourseImport", startTimeOfMostRecentCourseImportMinusOneSecond.getTime())
				.getResultList();

		// Check if we have duplicates
		Set<Long> semesterIdsAsSet = new HashSet<>(semesterIdsAsList);
		if (semesterIdsAsSet.size() == 1) {
			// No duplicates
			return semesterDao.getSemesterById(semesterIdsAsList.get(0));
		} else {
			// Duplicates (should not occur)
			if (semesterIdsAsSet.size() > 1) {
				StringBuilder msg = new StringBuilder();
				msg.append("Current import process contains courses from multiple semesters (");
				for (Long semesterId : semesterIdsAsSet) {
					Semester semester = semesterDao.getSemesterById(semesterId);
					msg.append(semester.getSemesterNameYear()).append(", ");
				}
				msg.setLength(msg.length() - 2);
				msg.append(")!");
				LOG.warn(msg.toString());
			}
			return null;
		}
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