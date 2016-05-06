package ch.uzh.campus.data;

import java.io.Serializable;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 */
public class LecturerIdCourseId implements Serializable {

    private Long lecturer;
    private Long course;

    public LecturerIdCourseId() {
    }

    public LecturerIdCourseId(Long lecturer, Long course) {
        this.lecturer = lecturer;
        this.course = course;
    }

    public Long getLecturer() {
        return lecturer;
    }

    public void setLecturer(Long lecturer) {
        this.lecturer = lecturer;
    }

    public Long getCourse() {
        return course;
    }

    public void setCourse(Long course) {
        this.course = course;
    }
}
