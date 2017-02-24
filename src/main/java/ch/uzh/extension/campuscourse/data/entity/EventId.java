package ch.uzh.extension.campuscourse.data.entity;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

/**
 * @author Martin Schraner
 */
public class EventId implements Serializable {

	private Long course;
	private Date date;
	private Time start;
	private Time end;

	public EventId() {
	}

	public EventId(Long course, Date date, Time start, Time end) {
		this.course = course;
		this.date = date;
		this.start = start;
		this.end = end;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EventId eventId = (EventId) o;

		if (!course.equals(eventId.course)) return false;
		if (!date.equals(eventId.date)) return false;
		if (!start.equals(eventId.start)) return false;
		return end.equals(eventId.end);
	}

	@Override
	public int hashCode() {
		int result = course.hashCode();
		result = 31 * result + date.hashCode();
		result = 31 * result + start.hashCode();
		result = 31 * result + end.hashCode();
		return result;
	}
}
