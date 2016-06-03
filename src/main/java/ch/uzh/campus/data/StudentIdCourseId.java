package ch.uzh.campus.data;

import java.util.Date;

/**
 * Initial Date: 04.06.2012 <p>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
public class StudentIdCourseId {

	private Long studentId;
	private Long courseId;
	private Date modifiedDate;
    
	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public Long getCourseId() {
		return courseId;
	}

	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}
