package ch.uzh.extension.campuscourse.model;

import java.util.Date;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 */
public class LecturerIdCourseIdDateOfImport extends LecturerIdCourseId {

    // Required by Spring
    public LecturerIdCourseIdDateOfImport() {}

    public LecturerIdCourseIdDateOfImport(long lecturerId, long courseId, Date dateOfImport) {
        super(lecturerId, courseId);
        this.dateOfImport = dateOfImport;
    }

    private Date dateOfImport;

    public Date getDateOfImport() {
        return dateOfImport;
    }

    // Required by Spring
    public void setDateOfImport(Date dateOfImport) {
        this.dateOfImport = dateOfImport;
    }
}
