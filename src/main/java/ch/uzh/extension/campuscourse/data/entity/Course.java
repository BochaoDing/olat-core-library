package ch.uzh.extension.campuscourse.data.entity;

import ch.uzh.extension.campuscourse.model.CampusGroups;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.repository.RepositoryEntry;

import javax.persistence.*;
import java.util.*;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@Entity
@Table(name = "ck_course")
@NamedQueries({
        @NamedQuery(name = Course.GET_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER, query = "select c from Course c where " +
				"(c.repositoryEntry is not null or c.parentCourse is not null) and c.semester.currentSemester = true"),
        @NamedQuery(name = Course.GET_IDS_OF_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER, query = "select c.id from Course c where " +
				"(c.repositoryEntry is not null or c.parentCourse is not null) " +
				"and c.semester.currentSemester = true"),
        @NamedQuery(name = Course.GET_REPOSITORY_ENTRY_KEYS_OF_ALL_CREATED_NOT_CONTINUED_COURSES_OF_SPECIFIC_SEMESTERS, query = "select c.repositoryEntry.key from Course c where " +
                "c.repositoryEntry is not null and c.parentCourse is null " +
                "and c.semester.id in :semesterIds " +
                "and not exists (select c2 from Course c2 where c2.parentCourse.id = c.id)"),
		@NamedQuery(name = Course.GET_ALL_CREATED_CONTINUED_COURSES_WITHOUT_CHILD_OF_SPECIFIC_SEMESTERS, query = "select c from Course c left join c.parentCourse where " +
				"c.parentCourse is not null and c.repositoryEntry is null " +
				"and c.semester.id in :semesterIds " +
				"and not exists (select c2 from Course c2 where c2.parentCourse.id = c.id)"),
        @NamedQuery(name = Course.GET_IDS_OF_ALL_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER, query = "select c.id from Course c where " +
                "c.repositoryEntry is null and c.parentCourse is null " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.semester.currentSemester = true"),
        @NamedQuery(name = Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, query = "select c from Course c join c.lecturerCourses lc where " +
                "(c.repositoryEntry is not null or c.parentCourse is not null) " +
                "and lc.lecturer.personalNr = :lecturerId " +
                "and c.semester.currentSemester = true " +
				"and c.title like :searchString"),
        @NamedQuery(name = Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, query = "select c from Course c join c.lecturerCourses lc where " +
                "lc.lecturer.personalNr = :lecturerId " +
                "and c.repositoryEntry is null and c.parentCourse is null " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.semester.currentSemester = true " +
                "and c.title like :searchString"),
        @NamedQuery(name = Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, query = "select c from Course c join c.studentCourses sc where " +
                "(c.repositoryEntry is not null or c.parentCourse is not null) " +
				"and sc.student.id = :studentId " +
                "and c.semester.currentSemester = true and c.title like :searchString"),
        @NamedQuery(name = Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE, query = "select c from Course c join c.parentCourse.studentCourses scp where " +
                "c.parentCourse is not null " +
				"and (c.repositoryEntry is not null or c.parentCourse is not null) " +
				"and scp.student.id = :studentId " +
                "and not exists (select c1 from Course c1 join c1.studentCourses sc1 where c1.id = c.id and sc1.student.id = :studentId) " +
                "and c.semester.currentSemester = true " +
				"and c.title like :searchString"),
        @NamedQuery(name = Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, query = "select c from Course c join c.studentCourses sc where " +
                "sc.student.id = :studentId " +
                "and c.repositoryEntry is null and c.parentCourse is null " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.semester.currentSemester = true " +
                "and c.title like :searchString"),
        @NamedQuery(name = Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, query = "select c from Course c left join c.studentCourses sc where " +
                "sc.student.id = :studentId " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.semester.currentSemester = true"),
        @NamedQuery(name = Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE, query = "select c from Course c join c.parentCourse.studentCourses scp where " +
                "c.parentCourse is not null and scp.student.id = :studentId " +
                "and not exists (select c1 from Course c1 join c1.studentCourses sc1 where c1.id = c.id and sc1.student.id = :studentId) " +
                "and c.exclude = false " +
                "and exists (select c2 from Course c2 join c2.orgs o where c2.id = c.id and o.enabled = true) " +
                "and c.semester.currentSemester = true"),
        @NamedQuery(name = Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, query = "select c from Course c left join c.lecturerCourses lc where " +
                "lc.lecturer.personalNr = :lecturerId " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.semester.currentSemester = true"),
        @NamedQuery(name = Course.GET_ALL_NOT_CREATED_ORPHANED_COURSES, query = "select c.id from Course c where " +
				"c.repositoryEntry is null and c.parentCourse is null " +
				"and c.id not in (select lc.course.id from LecturerCourse lc) " +
				"and c.id not in (select sc.course.id from StudentCourse sc)"),
        @NamedQuery(name = Course.GET_COURSE_ID_OR_ID_OF_FIRST_PARENT_OF_CONTINUED_COURSE_BY_REPOSITORY_ENTRY_KEY, query = "select c.id from Course c where " +
				"c.repositoryEntry.key = :repositoryEntryKey and c.parentCourse is null"),
        @NamedQuery(name = Course.GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_REPOSITORY_ENTRY_KEY, query = "select c from Course c left join c.childCourse where " +
				"c.repositoryEntry.key = :repositoryEntryKey and c.parentCourse is null"),
        @NamedQuery(name = Course.GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_CAMPUS_GROUP_A_KEY, query = "select c from Course c left join c.childCourse where " +
				"c.campusGroupA.key = :campusGroupKey and c.parentCourse is null"),
        @NamedQuery(name = Course.GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_CAMPUS_GROUP_B_KEY, query = "select c from Course c left join c.childCourse where " +
				"c.campusGroupB.key = :campusGroupKey and c.parentCourse is null"),
		@NamedQuery(name = Course.GET_IDS_OF_CONTINUED_COURSES_TOO_FAR_IN_THE_PAST, query = "select c.parentCourse.id from Course c where " +
				"c.parentCourse is not null " +
				"and c.parentCourse.dateOfLatestImport < :nYearsInThePast"),
		@NamedQuery(name = Course.GET_SEMESTER_IDS_OF_MOST_RECENT_COURSE_IMPORT, query = "select c.semester.id from Course c where " +
				"c.dateOfLatestImport >= :startTimeOfMostRecentCourseImport")
})
public class Course {

    public static final String GET_IDS_OF_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER = "getIdsOfAllCreatedCoursesOfCurrentSemester";
    public static final String GET_REPOSITORY_ENTRY_KEYS_OF_ALL_CREATED_NOT_CONTINUED_COURSES_OF_SPECIFIC_SEMESTERS = "getRepositoryEntryKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters";
	public static final String GET_ALL_CREATED_CONTINUED_COURSES_WITHOUT_CHILD_OF_SPECIFIC_SEMESTERS = "getAllCreatedContinuedCoursesWithoutChildOfSpecificSemesters";
    public static final String GET_IDS_OF_ALL_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER = "getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester";
    public static final String GET_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER = "getAllCreatedCoursesOfCurrentSemester";
    public static final String GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID = "getCreatedCoursesOfCurrentSemesterByLecturerId";
    public static final String GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID = "getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId";
    public static final String GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID = "getCreatedCoursesOfCurrentSemesterByStudentId";
    public static final String GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE = "getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse";
    public static final String GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID = "getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId";
    public static final String GET_ALL_NOT_CREATED_ORPHANED_COURSES = "getAllNotCreatedOrphanedCourses";
    public static final String GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID = "getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId";
    public static final String GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID = "getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId";
    public static final String GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE = "getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse";
	public static final String GET_COURSE_ID_OR_ID_OF_FIRST_PARENT_OF_CONTINUED_COURSE_BY_REPOSITORY_ENTRY_KEY = "getCourseIdOrIdOrFirstParentOfContinuedCourseByRepositoryEntryKey";
    public static final String GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_REPOSITORY_ENTRY_KEY = "getCourseOrFirstParentOfContinuedCourseByRepositoryEntryKey";
    public static final String GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_CAMPUS_GROUP_A_KEY = "getCourseOrFirstParentOfContinuedCourseByCampusGroupAKey";
    public static final String GET_COURSE_OR_FIRST_PARENT_OF_CONTINUED_COURSE_BY_CAMPUS_GROUP_B_KEY = "getCourseOrFirstParentOfContinuedCourseByCampusGroupBKey";
	public static final String GET_IDS_OF_CONTINUED_COURSES_TOO_FAR_IN_THE_PAST = "getIdsOfContinuedCoursesTooFarInThePast";
    public static final String GET_SEMESTER_IDS_OF_MOST_RECENT_COURSE_IMPORT = "getSemesterIdsOfMostRecentCourseImport";
    private static final String WHITESPACE = " ";

    @Id
    private Long id;

    @Column(name = "lv_kuerzel", nullable = false)
    private String lvKuerzel;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "lv_nr", nullable = false)
    private String lvNr;

    @Column(name = "e_learning_supported", nullable = false)
    private boolean eLearningSupported;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "vvz_link", nullable = false)
    private String vvzLink;

    @Column(name = "exclude", nullable = false)
    private boolean exclude = false;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_of_olat_course_creation")
	private Date dateOfOlatCourseCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_of_first_import", nullable = false)
	private Date dateOfFirstImport;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_latest_import", nullable = false)
    private Date dateOfLatestImport;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_semester")
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "fk_repositoryentry")
    private RepositoryEntry repositoryEntry;

    @ManyToOne(targetEntity = BusinessGroupImpl.class)
    @JoinColumn(name = "fk_campusgroup_a")
    private BusinessGroup campusGroupA;

    @ManyToOne(targetEntity = BusinessGroupImpl.class)
    @JoinColumn(name = "fk_campusgroup_b")
    private BusinessGroup campusGroupB;

    @OneToMany(mappedBy = "course")
    private Set<LecturerCourse> lecturerCourses = new HashSet<>();
  
    @OneToMany(mappedBy = "course")
    private Set<StudentCourse> studentCourses = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "ck_course_org",
            joinColumns = {@JoinColumn(name = "fk_course")},
            inverseJoinColumns = {@JoinColumn(name = "fk_org")})
    private Set<Org> orgs = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Text> texts = new HashSet<>();

    @OneToOne(mappedBy = "parentCourse", cascade = CascadeType.ALL)
    private Course childCourse;

    // Must be loaded lazy to avoid parallelization problems at batch import / synchronization
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_parent_course")
    private Course parentCourse;

    public Course() {}

    public Course(Long id,
				  String lvKuerzel,
				  String title,
				  String lvNr,
				  boolean eLearningSupported,
				  String language,
				  String category,
				  Date startDate,
				  Date endDate,
				  String vvzLink,
				  boolean exclude,
				  Date dateOfLatestImport,
				  Semester semester) {
        this.id = id;
        this.lvKuerzel = lvKuerzel;
        this.title = title;
        this.lvNr = lvNr;
        this.eLearningSupported = eLearningSupported;
        this.language = language;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.vvzLink = vvzLink;
        this.exclude = exclude;
		this.dateOfLatestImport = dateOfLatestImport;
        this.semester = semester;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLvKuerzel() {
        return lvKuerzel;
    }

    public void setLvKuerzel(String lvKuerzel) {
        this.lvKuerzel = lvKuerzel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLvNr() {
        return lvNr;
    }

    public void setLvNr(String vstNr) {
        this.lvNr = vstNr;
    }

    public boolean isELearningSupported() {
        return eLearningSupported;
    }

    public void setELearningSupported(boolean eLearningSupported) {
        this.eLearningSupported = eLearningSupported;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getVvzLink() {
        return vvzLink;
    }

    public void setVvzLink(String vvzLink) {
        this.vvzLink = vvzLink;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

    public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
        this.repositoryEntry = repositoryEntry;
    }

    public BusinessGroup getCampusGroupA() {
        return campusGroupA;
    }

    public void setCampusGroupA(BusinessGroup campusgroupA) {
        this.campusGroupA = campusgroupA;
    }

    public BusinessGroup getCampusGroupB() {
        return campusGroupB;
    }

    public void setCampusGroupB(BusinessGroup campusgroupB) {
        this.campusGroupB = campusgroupB;
    }

	public Date getDateOfOlatCourseCreation() {
		return dateOfOlatCourseCreation;
	}

	public void setDateOfOlatCourseCreation(Date dateOfOlatCourseCreation) {
		this.dateOfOlatCourseCreation = dateOfOlatCourseCreation;
	}

	public Date getDateOfFirstImport() {
		return dateOfFirstImport;
	}

	public void setDateOfFirstImport(Date dateOfFirstImport) {
		this.dateOfFirstImport = dateOfFirstImport;
	}

	public Date getDateOfLatestImport() {
        return dateOfLatestImport;
    }

    public void setDateOfLatestImport(Date dateOfImport) {
        this.dateOfLatestImport = dateOfImport;
    }

    public Set<LecturerCourse> getLecturerCourses() {
        return lecturerCourses;
    }

    public Set<StudentCourse> getStudentCourses() {
       return studentCourses;
    }

    public Set<Org> getOrgs() {
        return orgs;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public Set<Text> getTexts() {
        return texts;
    }

    public Course getChildCourse() {
        return childCourse;
    }

    public Course getParentCourse() {
        return parentCourse;
    }

    public void setParentCourse(Course parentCourse) {
        this.parentCourse = parentCourse;
        parentCourse.childCourse = this;
    }

    public void removeParentCourse() {
        if (parentCourse == null) {
            return;
        }
        parentCourse.childCourse = null;
        parentCourse = null;
    }

    @Transient
    public boolean isContinuedCourse() {
        return getParentCourse() != null;
    }

	/*
	 * Return the repository entry of the course (if it is not continued) or the repository entry of the first parent
	 * (if it is continued, since a child course does have the repository entry information itself)
	 *
	 * @return repository entry
	 */
	@Transient
	public RepositoryEntry getRepositoryEntryOfCourseOrOfFirstParentOfContinuedCourse() {
    	Course courseIt = this;
    	while (courseIt.getParentCourse() != null) {
    		courseIt = courseIt.getParentCourse();
		}
		return courseIt.getRepositoryEntry();
	}

	/*
	 * Return the campus groups of the course (if it is not continued) or the repository entry of the first parent
	 * (if it is continued, since a child course does have the repository entry information itself)
	 *
	 * @return repository entry
	 */
	@Transient
	public CampusGroups getCampusGroupsOfCourseOrOfFirstParentOfContinuedCourse() {
		Course courseIt = this;
		while (courseIt.getParentCourse() != null) {
			courseIt = courseIt.getParentCourse();
		}
		return new CampusGroups(courseIt.getCampusGroupA(), courseIt.getCampusGroupB());
	}

    /*
     * Create title to be displayed in OLAT (used as repository entry displayname and in course editor model).
     *
     * E.g. 16FS <LV-Kuerzel starting at position 4> Course Title
     *      16FS/15HS <LV-Kuerzel starting at position 4> Course Title
     *      16FS/15HS/15FS <LV-Kuerzel starting at position 4> Course Title
     *
     * @return title to be displayed
     */
    @Transient
    public String getTitleToBeDisplayed() {

        // Add short semester(s)
        StringBuilder titleToBeDisplayed = new StringBuilder();
        Course courseIt = this;
        int count = 0;
        do {
            titleToBeDisplayed.append(courseIt.getSemester().getShortYearShortSemesterName()).append("/");
            courseIt = courseIt.getParentCourse();
            count++;
        } while (courseIt != null && count <= 10);
        // Remove last "/"
        titleToBeDisplayed.setLength(titleToBeDisplayed.length() - 1);
        titleToBeDisplayed.append(WHITESPACE);

        // Add lvKuerzel starting at position 4
        titleToBeDisplayed.append(getTruncatedLvKuerzelWithAppendedWhitespace(this));

        // Add course title
        titleToBeDisplayed.append(getTitle());
        return titleToBeDisplayed.toString();
    }

    /**
     * Create list with title of course and parent courses in ascending order.
     *
     * E.g. (15FS <LV-Kuerzel starting at position 4> Test Course I,
     *       15HS <LV-Kuerzel starting at position 4> Test Course II,
     *       16FS <LV-Kuerzel starting at position 4> Test Course III)
     *
     * @return list with titles
     */
    @Transient
    public List<String> getTitlesOfCourseAndParentCoursesInAscendingOrder() {
        List<String> titlesOfParentCourseAndParentCourses = new ArrayList<>();
        Course courseIt = this;
        int count = 0;
        do {
            String shortSemesterLvKuerzelTitle = courseIt.getSemester().getShortYearShortSemesterName() + WHITESPACE
                    + getTruncatedLvKuerzelWithAppendedWhitespace(courseIt) + courseIt.getTitle();
            titlesOfParentCourseAndParentCourses.add(0, shortSemesterLvKuerzelTitle);
            courseIt = courseIt.getParentCourse();
            count++;
        } while (courseIt != null && count <= 10);
        return titlesOfParentCourseAndParentCourses;
    }

    private String getTruncatedLvKuerzelWithAppendedWhitespace(Course course) {
        if (course.getLvKuerzel() != null && !course.getLvKuerzel().isEmpty()) {
            if (course.getLvKuerzel().length() > 4) {
            	return course.getLvKuerzel().substring(4) + WHITESPACE;
            } else {
            	return course.getLvKuerzel() + WHITESPACE;
            }
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return "id=" + getId()
                + ",lvKuerzel=" + getLvKuerzel()
                + ",title=" + getTitle()
                + ",lvNr=" + getLvNr()
                + ",isELearningSupported=" + isELearningSupported()
                + ",language=" + getLanguage()
                + ",category=" + getCategory()
                + ",startDate=" + getStartDate()
                + ",endDate=" + getEndDate()
                + ",vvzLink=" + getVvzLink()
                + ",exclude=" + isExclude()
                + ",semester=" + getSemester()
                + ",olat resource id=" + (getRepositoryEntry() == null ? "null" : getRepositoryEntry().getKey())
                + ",campus group A id=" + (getCampusGroupA() == null ? "null" : getCampusGroupA().getKey())
                + ",campus group B id=" + (getCampusGroupB() == null ? "null" : getCampusGroupB().getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Course course = (Course) o;

        if (!lvKuerzel.equals(course.lvKuerzel)) return false;
        if (!title.equals(course.title)) return false;
        return lvNr.equals(course.lvNr);

    }

    @Override
    public int hashCode() {
        int result = lvKuerzel.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + lvNr.hashCode();
        return result;
    }
}
