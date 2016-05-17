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
    private List<LecturerIdCourseId> lecturerIdCourseIds;
    private List<StudentIdCourseId> studentIdCourseIds;
    private List<TextCourseId> textCourseIds;
    private List<Event> events;
    private List<ImportStatistic> importStatistics;

    public List<Delegation> getDelegations() {
        return delegations;
    }

    public void setDelegations(List<Delegation> delegations) {
        this.delegations = delegations;
    }

    public List<Org> getOrgs() {
        return orgs;
    }

    public void setOrgs(List<Org> orgs) {
        this.orgs = orgs;
    }

    public List<SapOlatUser> getSapOlatUsers() {
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

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<ImportStatistic> getImportStatistics() {
        return importStatistics;
    }

    public void setImportStatistics(List<ImportStatistic> importStatistics) {
        this.importStatistics = importStatistics;
    }

    public List<StudentIdCourseId> getStudentIdCourseIds() {
        return studentIdCourseIds;
    }

    public void setStudentIdCourseIds(List<StudentIdCourseId> studentIdCourseIds) {
        this.studentIdCourseIds = studentIdCourseIds;
    }

    public List<TextCourseId> getTextCourseIds() {
        return textCourseIds;
    }

    public void setTextCourseIds(List<TextCourseId> textCourseIds) {
        this.textCourseIds = textCourseIds;
    }

    public List<LecturerIdCourseId> getLecturerIdCourseIds() {
        return lecturerIdCourseIds;
    }

    public void setLecturerIdCourseIds(List<LecturerIdCourseId> lecturerIdCourseIds) {
        this.lecturerIdCourseIds = lecturerIdCourseIds;
    }
}
