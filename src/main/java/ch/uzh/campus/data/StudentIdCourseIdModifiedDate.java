package ch.uzh.campus.data;

import java.util.Date;

/**
 * Initial Date: 04.06.2012 <p>
 *
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
public class StudentIdCourseIdModifiedDate extends StudentIdCourseId {

	public StudentIdCourseIdModifiedDate() {}

	public StudentIdCourseIdModifiedDate(long studentId, long courseId, Date modifiedDate) {
		super(studentId, courseId);
		this.modifiedDate = modifiedDate;
	}

	private Date modifiedDate;

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}
