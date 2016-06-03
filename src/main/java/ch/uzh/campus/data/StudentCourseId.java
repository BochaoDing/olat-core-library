package ch.uzh.campus.data;

import java.io.Serializable;

/**
 * @author Martin Schraner
 */
public class StudentCourseId implements Serializable {

    private Long student;
    private Long course;

    public StudentCourseId() {
    }

    public StudentCourseId(Long student, Long course) {
        this.student = student;
        this.course = course;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StudentCourseId that = (StudentCourseId) o;

        if (!student.equals(that.student)) return false;
        return course.equals(that.course);

    }

    @Override
    public int hashCode() {
        int result = student.hashCode();
        result = 31 * result + course.hashCode();
        return result;
    }
}
