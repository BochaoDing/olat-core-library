package ch.uzh.campus.data;

import java.util.Date;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 */
public class LecturerIdCourseIdModifiedDate extends LecturerIdCourseId {

    // Required by Spring
    public LecturerIdCourseIdModifiedDate() {}

    public LecturerIdCourseIdModifiedDate(long lecturerId, long courseId, Date modifiedDate) {
        super(lecturerId, courseId);
        this.modifiedDate = modifiedDate;
    }

    private Date modifiedDate;

    public Date getModifiedDate() {
        return modifiedDate;
    }

    // Required by Spring
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
