package ch.uzh.extension.campuscourse.model;

/**
 * @author Martin Schraner
 */
public class CourseIdTextTypeIdLineNumber {

	private final long courseId;
	private final int textTypeId;
	private final int lineNumber;

	public CourseIdTextTypeIdLineNumber(long courseId, int textTypeId, int lineNumber) {
		this.courseId = courseId;
		this.textTypeId = textTypeId;
		this.lineNumber = lineNumber;
	}

	public long getCourseId() {
		return courseId;
	}

	public int getTextTypeId() {
		return textTypeId;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CourseIdTextTypeIdLineNumber that = (CourseIdTextTypeIdLineNumber) o;

		if (courseId != that.courseId) return false;
		if (textTypeId != that.textTypeId) return false;
		return lineNumber == that.lineNumber;
	}

	@Override
	public int hashCode() {
		int result = (int) (courseId ^ (courseId >>> 32));
		result = 31 * result + textTypeId;
		result = 31 * result + lineNumber;
		return result;
	}
}
