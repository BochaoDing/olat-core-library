package ch.uzh.campus.data;

/**
 * @author Martin Schraner
 */
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
