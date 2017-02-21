package ch.uzh.extension.campuscourse.model;

import ch.uzh.extension.campuscourse.data.entity.LecturerCourse;

import java.util.Date;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 */
public class LecturerIdCourseIdDateOfLatestImport extends LecturerIdCourseId {

	private Date dateOfLatestImport;

    public LecturerIdCourseIdDateOfLatestImport() {}

    public LecturerIdCourseIdDateOfLatestImport(long lecturerId, long courseId, Date dateOfLatestImport) {
        super(lecturerId, courseId);
		this.dateOfLatestImport = dateOfLatestImport;
    }

	public Date getDateOfLatestImport() {
        return dateOfLatestImport;
    }

    public void setDateOfLatestImport(Date dateOfLatestImport) {
        this.dateOfLatestImport = dateOfLatestImport;
    }

	public void mergeImportedAttributesInto(LecturerCourse lecturerCourseToBeUpdated) {
		// all imported attributes, except ids
		lecturerCourseToBeUpdated.setDateOfLatestImport(getDateOfLatestImport());
	}
}
