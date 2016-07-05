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
    private List<SapOlatUser> sapOlatUsers;
    private List<Delegation> delegations;
    private List<Student> students;
    private List<Lecturer> lecturers;
    private List<Course> courses;
    private List<LecturerIdCourseIdModifiedDate> lecturerIdCourseIdModifiedDates;
    private List<StudentIdCourseIdModifiedDate> studentIdCourseIdModifiedDates;
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

    List<SapOlatUser> getSapOlatUsers() {
        return sapOlatUsers;
    }

    public void setSapOlatUsers(List<SapOlatUser> sapOlatUsers) {
        this.sapOlatUsers = sapOlatUsers;
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

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
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

    List<StudentIdCourseIdModifiedDate> getStudentIdCourseIdModifiedDates() {
        return studentIdCourseIdModifiedDates;
    }

    public void setStudentIdCourseIdModifiedDates(List<StudentIdCourseIdModifiedDate> studentIdCourseIdModifiedDates) {
        this.studentIdCourseIdModifiedDates = studentIdCourseIdModifiedDates;
    }

    List<TextCourseId> getTextCourseIds() {
        return textCourseIds;
    }

    public void setTextCourseIds(List<TextCourseId> textCourseIds) {
        this.textCourseIds = textCourseIds;
    }

    List<LecturerIdCourseIdModifiedDate> getLecturerIdCourseIdModifiedDates() {
        return lecturerIdCourseIdModifiedDates;
    }

    public void setLecturerIdCourseIdModifiedDates(List<LecturerIdCourseIdModifiedDate> lecturerIdCourseIdModifiedDates) {
        this.lecturerIdCourseIdModifiedDates = lecturerIdCourseIdModifiedDates;
    }
}
