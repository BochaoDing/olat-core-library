package ch.uzh.extension.campuscourse.data;

import ch.uzh.extension.campuscourse.data.entity.ImportStatistic;
import ch.uzh.extension.campuscourse.data.entity.Lecturer;
import ch.uzh.extension.campuscourse.data.entity.Org;
import ch.uzh.extension.campuscourse.data.entity.Student;
import ch.uzh.extension.campuscourse.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author aabouc
 * @author Martin Schraner
 */
@Component
public class MockDataGenerator {

    private List<Org> orgs;
    private List<Student> students;
    private List<Lecturer> lecturers;
    private List<CourseSemesterOrgId> courseSemesterOrgIds;
    private List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports;
    private List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports;
    private List<TextCourseId> textCourseIds;
    private List<EventCourseId> eventCourseIds;
    private List<ImportStatistic> importStatistics;

    public List<Org> getOrgs() {
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

    public List<CourseSemesterOrgId> getCourseSemesterOrgIds() {
        return courseSemesterOrgIds;
    }

    public void setCourseSemesterOrgIds(List<CourseSemesterOrgId> courseSemesterOrgIds) {
        this.courseSemesterOrgIds = courseSemesterOrgIds;
    }

    public List<EventCourseId> getEventCourseIds() {
        return eventCourseIds;
    }

    public void setEventCourseIds(List<EventCourseId> eventCourseIds) {
        this.eventCourseIds = eventCourseIds;
    }

    public List<ImportStatistic> getImportStatistics() {
        return importStatistics;
    }

    public void setImportStatistics(List<ImportStatistic> importStatistics) {
        this.importStatistics = importStatistics;
    }

    public List<StudentIdCourseIdDateOfImport> getStudentIdCourseIdDateOfImports() {
        return studentIdCourseIdDateOfImports;
    }

    public void setStudentIdCourseIdDateOfImports(List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports) {
        this.studentIdCourseIdDateOfImports = studentIdCourseIdDateOfImports;
    }

    public List<TextCourseId> getTextCourseIds() {
        return textCourseIds;
    }

    public void setTextCourseIds(List<TextCourseId> textCourseIds) {
        this.textCourseIds = textCourseIds;
    }

    public List<LecturerIdCourseIdDateOfImport> getLecturerIdCourseIdDateOfImports() {
        return lecturerIdCourseIdDateOfImports;
    }

    public void setLecturerIdCourseIdDateOfImports(List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports) {
        this.lecturerIdCourseIdDateOfImports = lecturerIdCourseIdDateOfImports;
    }
}
