package ch.uzh.extension.campuscourse.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * @author Martin Schraner
 */
@Repository
@Scope("prototype")
public class StudentIdCourseId extends JoinTableIds {

	// Required by Spring
	public StudentIdCourseId() {}

	public StudentIdCourseId(long studentId, long courseId) {
		firstReference = studentId;
		secondReference = courseId;
	}

	public long getStudentId() {
		return firstReference;
	}

	// Required by Spring
	public void setStudentId(long studentId) {
		firstReference = studentId;
	}

	public long getCourseId() {
		return secondReference;
	}

	// Required by Spring
	public void setCourseId(long courseId) {
		this.secondReference = courseId;
	}
}
