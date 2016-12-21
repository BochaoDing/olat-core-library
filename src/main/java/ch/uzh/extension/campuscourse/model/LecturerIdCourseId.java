package ch.uzh.extension.campuscourse.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * @author Martin Schraner
 */
@Repository
@Scope("prototype")
public class LecturerIdCourseId extends JoinTableIds {

    // Required by Spring
    public LecturerIdCourseId() {}

    public LecturerIdCourseId(long lecturerId, long courseId) {
        firstReference = lecturerId;
        secondReference = courseId;
    }

    public long getLecturerId() {
        return firstReference;
    }

    // Required by Spring
    public void setLecturerId(long lecturerId) {
        firstReference = lecturerId;
    }

    public long getCourseId() {
        return secondReference;
    }

    // Required by Spring
    public void setCourseId(long courseId) {
        secondReference = courseId;
    }

}
