package ch.uzh.campus.data;

import java.io.Serializable;

/**
 * @author Martin Schraner
 */
public class LecturerCourseId implements Serializable {

    private Long lecturer;
    private Long course;

    public LecturerCourseId() {
    }

    public LecturerCourseId(Long lecturer, Long course) {
        this.lecturer = lecturer;
        this.course = course;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LecturerCourseId that = (LecturerCourseId) o;

        if (!lecturer.equals(that.lecturer)) return false;
        return course.equals(that.course);

    }

    @Override
    public int hashCode() {
        int result = lecturer.hashCode();
        result = 31 * result + course.hashCode();
        return result;
    }
}
