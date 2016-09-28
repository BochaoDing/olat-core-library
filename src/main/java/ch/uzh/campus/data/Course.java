package ch.uzh.campus.data;

import org.apache.commons.lang.builder.ToStringBuilder;

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
@Entity
@NamedQueries({
        @NamedQuery(name = Course.GET_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER, query = "select c from Course c where c.resourceableId is not null and c.shortSemester = (select max(c2.shortSemester) from Course c2)"),
        @NamedQuery(name = Course.GET_IDS_OF_ALL_CREATED_SYNCHRONIZABLE_COURSES_OF_CURRENT_SEMESTER, query = "select c.id from Course c where c.resourceableId is not null and c.synchronizable = true and c.shortSemester = (select max(c2.shortSemester) from Course c2)"),
        @NamedQuery(name = Course.GET_RESOURCEABLE_IDS_OF_ALL_CREATED_NOT_CONTINUED_COURSES_OF_SPECIFIC_SEMESTERS, query = "select c.resourceableId from Course c where " +
                "c.resourceableId is not null " +
                "and c.shortSemester in :shortSemesters " +
                "and not exists (select c2 from Course c2 where c2.parentCourse.id = c.id)"),
        @NamedQuery(name = Course.GET_IDS_OF_ALL_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER, query = "select c.id from Course c where " +
                "c.resourceableId is null " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.shortSemester = (select max(c2.shortSemester) from Course c2)"),
        @NamedQuery(name = Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, query = "select c from Course c join c.lecturerCourses lc where " +
                "c.resourceableId is not null and lc.lecturer.personalNr = :lecturerId " +
                "and c.shortSemester = (select max(c1.shortSemester) from Course c1) and c.title like :searchString"),
        @NamedQuery(name = Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, query = "select c from Course c join c.lecturerCourses lc where " +
                "lc.lecturer.personalNr = :lecturerId " +
                "and c.resourceableId is null " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.shortSemester = (select max(c2.shortSemester) from Course c2) " +
                "and c.title like :searchString"),
        @NamedQuery(name = Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, query = "select c from Course c join c.studentCourses sc where " +
                "c.resourceableId is not null and sc.student.id = :studentId " +
                "and c.shortSemester = (select max(c1.shortSemester) from Course c1) and c.title like :searchString"),
        @NamedQuery(name = Course.GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE, query = "select c from Course c join c.parentCourse.studentCourses scp where " +
                "c.parentCourse is not null and c.resourceableId is not null and scp.student.id = :studentId " +
                "and not exists (select c1 from Course c1 join c1.studentCourses sc1 where c1.id = c.id and sc1.student.id = :studentId) " +
                "and c.shortSemester = (select max(c2.shortSemester) from Course c2) and c.title like :searchString"),
        @NamedQuery(name = Course.GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, query = "select c from Course c join c.studentCourses sc where " +
                "sc.student.id = :studentId " +
                "and c.resourceableId is null " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.shortSemester = (select max(c2.shortSemester) from Course c2) " +
                "and c.title like :searchString"),
        @NamedQuery(name = Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID, query = "select c from Course c left join c.studentCourses sc where " +
                "sc.student.id = :studentId " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.shortSemester = (select max(c2.shortSemester) from Course c2)"),
        @NamedQuery(name = Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE, query = "select c from Course c join c.parentCourse.studentCourses scp where " +
                "c.parentCourse is not null and scp.student.id = :studentId " +
                "and not exists (select c1 from Course c1 join c1.studentCourses sc1 where c1.id = c.id and sc1.student.id = :studentId) " +
                "and c.exclude = false " +
                "and exists (select c2 from Course c2 join c2.orgs o where c2.id = c.id and o.enabled = true) " +
                "and c.shortSemester = (select max(c3.shortSemester) from Course c3)"),
        @NamedQuery(name = Course.GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID, query = "select c from Course c left join c.lecturerCourses lc where " +
                "lc.lecturer.personalNr = :lecturerId " +
                "and c.exclude = false " +
                "and exists (select c1 from Course c1 join c1.orgs o where c1.id = c.id and o.enabled = true) " +
                "and c.shortSemester = (select max(c2.shortSemester) from Course c2) "),
        @NamedQuery(name = Course.GET_ALL_NOT_CREATED_ORPHANED_COURSES, query = "select c.id from Course c where c.resourceableId is null and c.id not in (select lc.course.id from LecturerCourse lc) and c.id not in (select sc.course.id from StudentCourse sc)"),
        @NamedQuery(name = Course.GET_COURSE_IDS_BY_RESOURCEABLE_ID, query = "select c.id from Course c where c.resourceableId = :resourceableId"),
        @NamedQuery(name = Course.GET_COURSES_BY_RESOURCEABLE_ID, query = "select c from Course c where c.resourceableId = :resourceableId"),
        @NamedQuery(name = Course.GET_LATEST_COURSE_BY_RESOURCEABLE_ID, query = "select c from Course c where c.resourceableId = :resourceableId and c.endDate = (select max(c1.endDate) from Course c1 where c1.resourceableId = :resourceableId)"),
        @NamedQuery(name = Course.GET_ALL_SHORT_SEMESTERS_IN_DESCENDING_ORDER, query = "select c.shortSemester from Course c group by c.shortSemester order by c.shortSemester desc")
})
@Table(name = "ck_course")
public class Course {

    private static final String WHITESPACE = " ";

    @Id
    private Long id;

    @Column(name = "olat_id")
    private Long resourceableId;

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

    @Column(name = "semester", nullable = false)
    private String semester;

    @Column(name = "short_semester", nullable = false)
    private String shortSemester;

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

    @OneToMany(mappedBy = "course")
    private Set<LecturerCourse> lecturerCourses = new HashSet<>();
  
    @OneToMany(mappedBy = "course")
    private Set<StudentCourse> studentCourses = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "ck_course_org",
            joinColumns = {@JoinColumn(name = "course_id")},
            inverseJoinColumns = {@JoinColumn(name = "org_id")})
    private Set<Org> orgs = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Text> texts = new HashSet<>();

    @OneToOne(mappedBy = "parentCourse", cascade = CascadeType.ALL)
    private Course childCourse;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_course_id")
    private Course parentCourse;

    public Course() {}

    static final String GET_IDS_OF_ALL_CREATED_SYNCHRONIZABLE_COURSES_OF_CURRENT_SEMESTER = "getIdsOfAllCreatedSynchronizableCoursesOfCurrentSemester";
    static final String GET_RESOURCEABLE_IDS_OF_ALL_CREATED_NOT_CONTINUED_COURSES_OF_SPECIFIC_SEMESTERS = "getResourceableIdsOfAllCreatedNotContinuedCoursesOfSpecificSemesters";
    static final String GET_IDS_OF_ALL_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER = "getIdsOfAllNotCreatedCreatableCoursesOfCurrentSemester";
    static final String GET_ALL_CREATED_COURSES_OF_CURRENT_SEMESTER = "getAllCreatedCoursesOfCurrentSemester";
    static final String GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID = "getCreatedCoursesOfCurrentSemesterByLecturerId";
    static final String GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID = "getNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId";
    static final String GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID = "getCreatedCoursesOfCurrentSemesterByStudentId";
    static final String GET_CREATED_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE = "getCreatedCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse";
    static final String GET_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID = "getNotCreatedCreatableCoursesOfCurrentSemesterByStudentId";
    static final String GET_ALL_NOT_CREATED_ORPHANED_COURSES = "getAllNotCreatedOrphanedCourses";
    static final String GET_COURSE_IDS_BY_RESOURCEABLE_ID = "getCourseIdsByResourceableId";
    static final String GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_LECTURER_ID = "getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByLecturerId";
    static final String GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID = "getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentId";
    static final String GET_CREATED_AND_NOT_CREATED_CREATABLE_COURSES_OF_CURRENT_SEMESTER_BY_STUDENT_ID_BOOKED_BY_STUDENT_ONLY_AS_PARENT_COURSE = "getCreatedAndNotCreatedCreatableCoursesOfCurrentSemesterByStudentIdBookedByStudentOnlyAsParentCourse";
    static final String GET_COURSES_BY_RESOURCEABLE_ID = "getCoursesByResourceableId";
    static final String GET_LATEST_COURSE_BY_RESOURCEABLE_ID = "getLatestCourseByResourceableId";
    static final String GET_ALL_SHORT_SEMESTERS_IN_DESCENDING_ORDER = "getAllShortSemestersInDescendingOrder";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceableId() {
        return resourceableId;
    }

    public void setResourceableId(Long resourceableId) {
        this.resourceableId = resourceableId;
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

    public boolean isSynchronizable() {
        return synchronizable;
    }

    public void setSynchronizable(boolean synchronizable) {
        this.synchronizable = synchronizable;
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

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getShortSemester() {
        return shortSemester;
    }

    public void setShortSemester(String shortSemester) {
        this.shortSemester = shortSemester;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
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

        String titleToBeDisplayed = "";

        if (shortSemester != null) {
            titleToBeDisplayed = shortSemester.concat(WHITESPACE);
        }

        if (shortTitle != null && shortTitleActivated && "".equals(shortTitle) == false) {
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
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("shortTitle", getShortTitle());
        builder.append("title", getTitle());
        builder.append("vstNr", getVstNr());
        builder.append("isELearning", isELearningSupported());
        builder.append("language", getLanguage());
        builder.append("category", getCategory());
        builder.append("startDate", getStartDate());
        builder.append("endDate", getEndDate());
        builder.append("vvzLink", getVvzLink());
        builder.append("resourceableId", getResourceableId());
        builder.append("exclude", isExclude());

        return builder.toString();
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
