package ch.uzh.campus.data;

import org.springframework.test.context.ContextConfiguration;

import java.util.List;

/**
 * @author aabouc
 * @author Martin Schraner
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class MockDataGenerator {

    private List<Org> orgs;
    private List<Delegation> delegations;
    private List<Student> students;
    private List<Lecturer> lecturers;
    private List<CourseOrgId> courseOrgIds;
    private List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports;
    private List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports;
    private List<TextCourseId> textCourseIds;
    private List<EventCourseId> eventCourseIds;
    private List<ImportStatistic> importStatistics;

    List<Delegation> getDelegations() {
        return delegations;
    }

    public void setDelegations(List<Delegation> delegations) {
        this.delegations = delegations;
    }

    List<Org> getOrgs() {
        return orgs;
    }

    public void setOrgs(List<Org> orgs) {
        this.orgs = orgs;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<Lecturer> getLecturers() {
        return lecturers;
    }

    public void setLecturers(List<Lecturer> lecturers) {
        this.lecturers = lecturers;
    }

    public List<CourseOrgId> getCourseOrgIds() {
        return courseOrgIds;
    }

    public void setCourseOrgIds(List<CourseOrgId> courseOrgIds) {
        this.courseOrgIds = courseOrgIds;
    }

    List<EventCourseId> getEventCourseIds() {
        return eventCourseIds;
    }

    public void setEventCourseIds(List<EventCourseId> eventCourseIds) {
        this.eventCourseIds = eventCourseIds;
    }

    List<ImportStatistic> getImportStatistics() {
        return importStatistics;
    }

    public void setImportStatistics(List<ImportStatistic> importStatistics) {
        this.importStatistics = importStatistics;
    }

    List<StudentIdCourseIdDateOfImport> getStudentIdCourseIdDateOfImports() {
        return studentIdCourseIdDateOfImports;
    }

    public void setStudentIdCourseIdDateOfImports(List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports) {
        this.studentIdCourseIdDateOfImports = studentIdCourseIdDateOfImports;
    }

    List<TextCourseId> getTextCourseIds() {
        return textCourseIds;
    }

    public void setTextCourseIds(List<TextCourseId> textCourseIds) {
        this.textCourseIds = textCourseIds;
    }

    List<LecturerIdCourseIdDateOfImport> getLecturerIdCourseIdDateOfImports() {
        return lecturerIdCourseIdDateOfImports;
    }

    public void setLecturerIdCourseIdDateOfImports(List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports) {
        this.lecturerIdCourseIdDateOfImports = lecturerIdCourseIdDateOfImports;
    }
}
