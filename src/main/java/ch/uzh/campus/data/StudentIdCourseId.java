package ch.uzh.campus.data;

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
}
