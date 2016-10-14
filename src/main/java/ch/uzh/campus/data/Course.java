package ch.uzh.campus.data;

import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@SuppressWarnings("JpaQlInspection")  // Required to suppress warnings caused by c.olatResource.key
@Entity
@NamedQueries({
        @NamedQuery(name = Course.GET_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER, query = "select c from Course c where c.olatResource is not null and c.semester.currentSemester = true"),
        @NamedQuery(name = Course.GET_IDS_OF_ALL_CREATED_SYNCHRONIZABLE_COURSES_OF_CURRENT_SEMESTER, query = "select c.id from Course c where c.olatResource is not null and c.synchronizable = true and c.semester.currentSemester = true"),
        @NamedQuery(name = Course.GET_OLAT_RESOURCE_KEYS_OF_ALL_CREATED_NOT_CONTINUED_COURSES_OF_SPECIFIC_SEMESTERS, query = "select c.olatResource.key from Course c where " +
                "c.olatResource is not null " +
                "and c.semester.id in :semesterIds " +
                "and not exists (select c2 from Course c2 where c2.parentCourse.id = c.id)"),
        @NamedQuery(name = Course.GET_IDS_OF_ALL_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER, query = "select c.id from Course c where " +
                "c.olatResource is null " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.semester.currentSemester = true"),
        @NamedQuery(name = Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, query = "select c from Course c join c.lecturerCourses lc where " +
                "c.olatResource is not null and lc.lecturer.personalNr = :lecturerId " +
                "and c.semester.currentSemester = true and c.title like :searchString"),
        @NamedQuery(name = Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, query = "select c from Course c join c.lecturerCourses lc where " +
                "lc.lecturer.personalNr = :lecturerId " +
                "and c.olatResource is null " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.semester.currentSemester = true " +
                "and c.title like :searchString"),
        @NamedQuery(name = Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, query = "select c from Course c join c.studentCourses sc where " +
                "c.olatResource is not null and sc.student.id = :studentId " +
                "and c.semester.currentSemester = true and c.title like :searchString"),
        @NamedQuery(name = Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE, query = "select c from Course c join c.parentCourse.studentCourses scp where " +
                "c.parentCourse is not null and c.olatResource is not null and scp.student.id = :studentId " +
                "and not exists (select c1 from Course c1 join c1.studentCourses sc1 where c1.id = c.id and sc1.student.id = :studentId) " +
                "and c.semester.currentSemester = true and c.title like :searchString"),
        @NamedQuery(name = Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, query = "select c from Course c join c.studentCourses sc where " +
                "sc.student.id = :studentId " +
                "and c.olatResource is null " +
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
        @NamedQuery(name = Course.GET_ALL_NOT_CREATED_ORPHANED_COURSES, query = "select c.id from Course c where c.olatResource is null and c.id not in (select lc.course.id from LecturerCourse lc) and c.id not in (select sc.course.id from StudentCourse sc)"),
        @NamedQuery(name = Course.GET_COURSE_IDS_BY_OLAT_RESOURCE_KEY, query = "select c.id from Course c where c.olatResource.key = :olatResourceKey"),
        @NamedQuery(name = Course.GET_COURSES_BY_OLAT_RESOURCE_KEY, query = "select c from Course c where c.olatResource.key = :olatResourceKey"),
        @NamedQuery(name = Course.GET_LATEST_COURSE_BY_OLAT_RESOURCE_KEY, query = "select c from Course c where c.olatResource.key = :olatResourceKey and c.endDate = (select max(c1.endDate) from Course c1 where c1.olatResource.key = :olatResourceKey)")
})
@Table(name = "ck_course")
public class Course {

    private static final String WHITESPACE = " ";

    @Id
    private Long id;

    @Column(name = "short_title", nullable = false)
    private String shortTitle;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "lv_nr", nullable = false)
    private String vstNr;

    @Column(name = "e_learning_supported")
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

    // Disable import and synchronization
    // Used in OLAT 7.x for a continued campus course (for the new course). May still be used in OLAT 10.x if the
    // lecturer/student tables of the campus course from the former semester is not available any more
    @Column(name = "synchronizable", nullable = false)
    private boolean synchronizable = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_import")
    private Date dateOfImport;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_semester")
    private Semester semester;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @ManyToOne(targetEntity=OLATResourceImpl.class)
    @JoinColumn(name = "fk_resource")
    private OLATResource olatResource;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_parent_course")
    private Course parentCourse;

    public Course() {}

    static final String GET_IDS_OF_ALL_CREATED_SYNCHRONIZABLE_COURSES_OF_CURRENT_SEMESTER = "getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemester";
    static final String GET_OLAT_RESOURCE_KEYS_OF_ALL_CREATED_NOT_CONTINUED_COURSES_OF_SPECIFIC_SEMESTERS = "getOlatResourceKeysOfAllCreatedNotContinuedCoursesOfSpecificSemesters";
    static final String GET_IDS_OF_ALL_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER = "getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester";
    static final String GET_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER = "getAllCreatedCoursesOfCurrentSemester";
    static final String GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID = "getCreatedCoursesOfCurrentSemesterByLecturerId";
    static final String GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID = "getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId";
    static final String GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID = "getCreatedCoursesOfCurrentSemesterByStudentId";
    static final String GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE = "getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse";
    static final String GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID = "getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId";
    static final String GET_ALL_NOT_CREATED_ORPHANED_COURSES = "getAllNotCreatedOrphanedCourses";
    static final String GET_COURSE_IDS_BY_OLAT_RESOURCE_KEY = "getCourseIdsByOlatResourceKey";
    static final String GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID = "getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId";
    static final String GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID = "getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId";
    static final String GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE = "getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse";
    static final String GET_COURSES_BY_OLAT_RESOURCE_KEY = "getCoursesByOlatResourceKey";
    static final String GET_LATEST_COURSE_BY_OLAT_RESOURCE_KEY = "getLatestCourseByOlatResourceKey";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVstNr() {
        return vstNr;
    }

    public void setVstNr(String vstNr) {
        this.vstNr = vstNr;
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

    public boolean isSynchronizable() {
        return synchronizable;
    }

    public void setSynchronizable(boolean synchronizable) {
        this.synchronizable = synchronizable;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public OLATResource getOlatResource() {
        return olatResource;
    }

    public void setOlatResource(OLATResource olatResource) {
        this.olatResource = olatResource;
    }

    public Date getDateOfImport() {
        return dateOfImport;
    }

    public void setDateOfImport(Date dateOfImport) {
        this.dateOfImport = dateOfImport;
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

    public void setChildCourse(Course childCourse) {
        this.childCourse = childCourse;
        childCourse.parentCourse = this;
    }

    public void removeChildCourse() {
        if (childCourse == null) {
            return;
        }
        childCourse.parentCourse = null;
        childCourse = null;
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
    public String getContents() {
        return buildText(Text.CONTENTS);
    }

    @Transient
    public String getInfos() {
        return buildText(Text.INFOS);
    }

    @Transient
    public String getMaterials() {
        return buildText(Text.MATERIALS);
    }

    @Transient
    private String buildText(String type) {
        StringBuilder content = new StringBuilder();
        for (Text text : this.getTexts()) {
            if (type.equalsIgnoreCase(text.getType())) {
                content.append(text.getLine());
                content.append(Text.BREAK_TAG);
            }
        }
        return content.toString();
    }

    @Transient
    public String getTitleToBeDisplayed(boolean shortTitleActivated) {

        if (semester == null) {
            return title;
        }

        String titleToBeDisplayed = semester.getShortYearShortSemesterName().concat(WHITESPACE);

        if (shortTitle != null && shortTitleActivated && !"".equals(shortTitle)) {
            if (shortTitle.length() > 4) {
            	titleToBeDisplayed += shortTitle.substring(4);
            } else {
            	titleToBeDisplayed += shortTitle;
            }
            titleToBeDisplayed += WHITESPACE;
        }

        return titleToBeDisplayed.concat(title);
    }

    @Override
    public String toString() {
        return "id=" + getId()
                + ",shortTitle=" + getShortTitle()
                + ",title=" + getTitle()
                + ",vstNr=" + getVstNr()
                + ",isELearningSupported=" + isELearningSupported()
                + ",language=" + getLanguage()
                + ",category=" + getCategory()
                + ",startDate=" + getStartDate()
                + ",endDate=" + getEndDate()
                + ",vvzLink=" + getVvzLink()
                + ",exclude=" + isExclude()
                + ",semester=" + getSemester()
                + ",olat resource id=" + (getOlatResource() == null ? "null" : getOlatResource().getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Course course = (Course) o;

        if (!shortTitle.equals(course.shortTitle)) return false;
        if (!title.equals(course.title)) return false;
        return vstNr.equals(course.vstNr);

    }

    @Override
    public int hashCode() {
        int result = shortTitle.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + vstNr.hashCode();
        return result;
    }
}
