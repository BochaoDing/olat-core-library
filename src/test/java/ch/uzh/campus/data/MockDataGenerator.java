package ch.uzh.campus.data;

import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Set;

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
    private List<StudentCourse> studentCourses;

    private List<LecturerCourse> lecturerCourses;
    private List<Text> texts;
    private List<Event> events;
    private List<ImportStatistic> importStatistics;

    
    private List<LecturerIdCourseId> lecturerIdCourseIds;
//    private Set<CourseLecturer> courseLecturers;

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

    public List<Text> getTexts() {
        return texts;
    }

    public void setTexts(List<Text> texts) {
        this.texts = texts;
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

    public List<StudentCourse> getStudentCourses() {
        return studentCourses;
    }

    public void setStudentCourses(List<StudentCourse> studentCourses) {
        this.studentCourses = studentCourses;
    }

    public List<LecturerCourse> getLecturerCourses() {
        return lecturerCourses;
    }

    public void setLecturerCourses(List<LecturerCourse> lecturerCourses) {
        this.lecturerCourses = lecturerCourses;
    }

    
    public List<LecturerIdCourseId> getLecturerIdCourseIds() {
        return lecturerIdCourseIds;
    }

    public void setLecturerIdCourseIds(List<LecturerIdCourseId> lecturerIdCourseIds) {
        this.lecturerIdCourseIds = lecturerIdCourseIds;
    }
}
