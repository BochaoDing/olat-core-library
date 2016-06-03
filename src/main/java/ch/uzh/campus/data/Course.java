package ch.uzh.campus.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
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
        @NamedQuery(name = Course.GET_ALL_CREATED_COURSES, query = "select c from Course c  where c.resourceableId is not null and c.shortSemester= (select max(c2.shortSemester) from Course c2)"),
        @NamedQuery(name = Course.GET_IDS_OF_ALL_CREATED_COURSES, query = "select c.id from Course c where c.resourceableId is not null and c.synchronizable = true and c.shortSemester= (select max(c2.shortSemester) from Course c2)"),
        @NamedQuery(name = Course.GET_RESOURCEABLEIDS_OF_ALL_CREATED_COURSES, query = "select c.resourceableId from Course c where c.resourceableId is not null"),
        @NamedQuery(name = Course.GET_IDS_OF_ALL_NOT_CREATED_COURSES, query = "select c.id from Course c where c.resourceableId is null and c.enabled = '1' and c.shortSemester= (select max(c2.shortSemester) from Course c2)"),
        @NamedQuery(name = Course.DELETE_RESOURCEABLE_ID, query = "update Course c set c.resourceableId = null where c.resourceableId = :resId"),
        @NamedQuery(name = Course.SAVE_RESOURCEABLE_ID, query = "update Course c set c.resourceableId = :resId where c.id = :courseId"),

        @NamedQuery(name = Course.DISABLE_SYNCHRONIZATION, query = "update Course c set c.synchronizable = false where c.id = :courseId"),

        @NamedQuery(name = Course.GET_CREATED_COURSES_BY_LECTURER_IDS, query = "select distinct c from Course c join c.lecturers l where l.personalNr in :lecturerIds and c.resourceableId is not null and c.enabled = '1' and c.shortSemester= (select max(c2.shortSemester) from Course c2) "),
        @NamedQuery(name = Course.GET_NOT_CREATED_COURSES_BY_LECTURER_IDS, query = "select distinct c from Course c join c.lecturers l where l.personalNr in :lecturerIds and c.resourceableId is null and c.enabled = '1' and c.shortSemester= (select max(c2.shortSemester) from Course c2) "),
        @NamedQuery(name = Course.GET_CREATED_COURSES_BY_STUDENT_ID, query = "select distinct c from Course c join c.studentCourses sc where sc.student.id = :studentId and c.resourceableId is not null and  c.enabled = '1' and c.shortSemester= (select max(c2.shortSemester) from Course c2)"),
        @NamedQuery(name = Course.GET_NOT_CREATED_COURSES_BY_STUDENT_ID, query = "select distinct c from Course c join c.studentCourses sc where sc.student.id = :studentId and c.resourceableId is null and c.enabled = '1' and c.shortSemester= (select max(c2.shortSemester) from Course c2)"),

        @NamedQuery(name = Course.GET_PILOT_COURSES_BY_STUDENT_ID, query = "select c from Course c left join c.studentCourses sc where sc.course.id = :studentId and c.enabled = '1' and c.shortSemester= (select max(c2.shortSemester) from Course c2)"),
        @NamedQuery(name = Course.GET_PILOT_COURSES_BY_LECTURER_ID, query = "select c from Course c left join c.lecturers cl where cl.personalNr = :lecturerId and c.enabled = '1' and c.shortSemester= (select max(c2.shortSemester) from Course c2) "),

        @NamedQuery(name = Course.GET_ALL_NOT_UPDATED_COURSES, query = "select c.id from Course c where c.resourceableId is null and c.modifiedDate < :lastImportDate"),
        @NamedQuery(name = Course.GET_COURSE_IDS_BY_RESOURCEABLE_ID, query = "select c.id from Course c where c.resourceableId = :resourceableId")
})
@Table(name = "ck_course")
public class Course {

    private static final String WHITESPACE = " ";

    @Id
    private Long id;

    @Column(name = "olat_id", updatable = false)
    private Long resourceableId;
    @Column(name = "short_title")
    private String shortTitle;
    @Column(name = "title")
    private String title;
    @Column(name = "lv_nr")
    private String vstNr;
    @Column(name = "e_learning_supported")
    private String isELearning;
    @Column(name = "language")
    private String language;
    @Column(name = "category")
    private String category;
    @Column(name = "start_date")
    private Date startDate;
    @Column(name = "end_date")
    private Date endDate;
    @Column(name = "vvz_link")
    private String vvzLink;
    @Column(name = "semester")
    private String semester;
    @Column(name = "short_semester")
    private String shortSemester;
    @Column(name = "ipz")
    private String ipz;

    @Column(name = "enabled")
    private String enabled = "0";

    @Column(name = "synchronizable", updatable = false)
    private boolean synchronizable = true;

    @Transient
    private Long org1;
    @Transient
    private Long org2;
    @Transient
    private Long org3;
    @Transient
    private Long org4;
    @Transient
    private Long org5;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "ck_lecturer_course",
            joinColumns = {@JoinColumn(name = "course_id")},
            inverseJoinColumns = {@JoinColumn(name = "lecturer_id")})
    private Set<Lecturer> lecturers = new HashSet<>();
  
    @OneToMany(mappedBy = "course")
    private Set<StudentCourse> studentCourses = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Text> texts = new HashSet<>();

    public static final String GET_IDS_OF_ALL_CREATED_COURSES = "getIdsOfAllCreatedCourses";
    public static final String GET_RESOURCEABLEIDS_OF_ALL_CREATED_COURSES = "getResourceableIdsOfAllCreatedCourses";
    public static final String GET_IDS_OF_ALL_NOT_CREATED_COURSES = "getIdsOfAllNotCreatedCourses";
    public static final String GET_ALL_CREATED_COURSES = "getAllCreatedCourses";
    public static final String DELETE_RESOURCEABLE_ID = "deleteResourceableId";
    public static final String SAVE_RESOURCEABLE_ID = "saveResourceableId";
    public static final String DISABLE_SYNCHRONIZATION = "disableSynchronization";

    public static final String GET_CREATED_COURSES_BY_LECTURER_IDS = "getCreatedCoursesByLecturerIds";
    public static final String GET_NOT_CREATED_COURSES_BY_LECTURER_IDS = "getNotCreatedCoursesByLecturerIds";
    public static final String GET_CREATED_COURSES_BY_STUDENT_ID = "getCreatedCoursesByStudentId";
    public static final String GET_NOT_CREATED_COURSES_BY_STUDENT_ID = "getNotCreatedCoursesByStudentId";

    public static final String GET_ALL_NOT_UPDATED_COURSES = "getAllNotUpdatedCourses";
    public static final String GET_COURSE_IDS_BY_RESOURCEABLE_ID = "getCourseIdsByResourceableId";

    public static final String GET_PILOT_COURSES_BY_LECTURER_ID = "getPilotCoursesByLecturerId";
    public static final String GET_PILOT_COURSES_BY_STUDENT_ID = "getPilotCoursesByStudentId";

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

    public String getIsELearning() {
        return isELearning;
    }

    public void setIsELearning(String isELearning) {
        this.isELearning = isELearning;
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

    public String getIpz() {
        return ipz;
    }

    public void setIpz(String ipz) {
        this.ipz = ipz;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public Long getOrg1() {
        return org1;
    }

    public void setOrg1(Long org1) {
        this.org1 = org1;
    }

    public Long getOrg2() {
        return org2;
    }

    public void setOrg2(Long org2) {
        this.org2 = org2;
    }

    public Long getOrg3() {
        return org3;
    }

    public void setOrg3(Long org3) {
        this.org3 = org3;
    }

    public Long getOrg4() {
        return org4;
    }

    public void setOrg4(Long org4) {
        this.org4 = org4;
    }

    public Long getOrg5() {
        return org5;
    }

    public void setOrg5(Long org5) {
        this.org5 = org5;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Set<Lecturer> getLecturers() {
        return lecturers;
    }

    public Set<StudentCourse> getStudentCourses() {
       return studentCourses;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public Set<Text> getTexts() {
        return texts;
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
    public String getTitleToBeDisplayed(String shortTitleActivated) {

        String titleToBeDisplayed = "";

        if (shortSemester != null) {
            titleToBeDisplayed = shortSemester.concat(WHITESPACE);
        }

        if (shortTitle != null && shortTitleActivated != null && Boolean.TRUE.toString().equals(shortTitleActivated)) {
            titleToBeDisplayed = titleToBeDisplayed.concat(shortTitle.substring(4)).concat(WHITESPACE);
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
        builder.append("isELearning", getIsELearning());
        builder.append("language", getLanguage());
        builder.append("category", getCategory());
        builder.append("startDate", getStartDate());
        builder.append("endDate", getEndDate());
        builder.append("vvzLink", getVvzLink());
        builder.append("resourceableId", getResourceableId());
        builder.append("ipz", getIpz());

        return builder.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Course))
            return false;
        Course theOther = (Course) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getShortTitle(), theOther.getShortTitle());
        builder.append(this.getTitle(), theOther.getTitle());
        builder.append(this.getVstNr(), theOther.getVstNr());

        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(this.getShortTitle());
        builder.append(this.getTitle());
        builder.append(this.getVstNr());

        return builder.toHashCode();
    }

}

