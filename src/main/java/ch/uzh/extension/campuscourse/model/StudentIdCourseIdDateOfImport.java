package ch.uzh.extension.campuscourse.model;

import java.util.Date;

/**
 * Initial Date: 04.06.2012 <p>
 *
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
public class StudentIdCourseIdDateOfImport extends StudentIdCourseId {

	public StudentIdCourseIdDateOfImport() {}

	public StudentIdCourseIdDateOfImport(long studentId, long courseId, Date dateOfImport) {
		super(studentId, courseId);
		this.dateOfImport = dateOfImport;
	}

	private Date dateOfImport;

	public Date getDateOfImport() {
		return dateOfImport;
	}

	public void setDateOfImport(Date dateOfImport) {
		this.dateOfImport = dateOfImport;
	}
}
