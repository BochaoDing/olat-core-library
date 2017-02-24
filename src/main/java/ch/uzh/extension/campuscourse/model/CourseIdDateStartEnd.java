package ch.uzh.extension.campuscourse.model;


import java.util.Date;

/**
 * @author Martin Schraner
 */
public class CourseIdDateStartEnd {

	private final long courseId;
	private final Date date;
	private final Date start;   // Hibernate requires java.util.Date, not java.sql.Time!
	private final Date end;     // Hibernate requires java.util.Date, not java.sql.Time!

	public CourseIdDateStartEnd(long courseId, Date date, Date start, Date end) {
		this.courseId = courseId;
		this.date = date;
		this.start = start;
		this.end = end;
	}

	public long getCourseId() {
		return courseId;
	}

	public Date getDate() {
		return date;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CourseIdDateStartEnd that = (CourseIdDateStartEnd) o;

		if (courseId != that.courseId) return false;
		if (!date.equals(that.date)) return false;
		if (!start.equals(that.start)) return false;
		return end.equals(that.end);
	}

	@Override
	public int hashCode() {
		int result = (int) (courseId ^ (courseId >>> 32));
		result = 31 * result + date.hashCode();
		result = 31 * result + start.hashCode();
		result = 31 * result + end.hashCode();
		return result;
	}
}
