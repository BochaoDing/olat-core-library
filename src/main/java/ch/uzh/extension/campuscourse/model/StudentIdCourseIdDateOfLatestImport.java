package ch.uzh.extension.campuscourse.model;

import ch.uzh.extension.campuscourse.data.entity.StudentCourse;

import java.util.Date;

/**
 * Initial Date: 04.06.2012 <p>
 *
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
public class StudentIdCourseIdDateOfLatestImport extends StudentIdCourseId {

	private Date dateOfLatestImport;

	public StudentIdCourseIdDateOfLatestImport() {}

	public StudentIdCourseIdDateOfLatestImport(long studentId, long courseId, Date dateOfLatestImport) {
		super(studentId, courseId);
		this.dateOfLatestImport = dateOfLatestImport;
	}

	public Date getDateOfLatestImport() {
		return dateOfLatestImport;
	}

	public void setDateOfLatestImport(Date dateOfLatestImport) {
		this.dateOfLatestImport = dateOfLatestImport;
	}

	public void mergeImportedAttributesInto(StudentCourse studentCourseToBeUpdated) {
		// all imported attributes, except ids
		studentCourseToBeUpdated.setDateOfLatestImport(getDateOfLatestImport());
	}
}
