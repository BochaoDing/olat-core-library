package ch.uzh.extension.campuscourse.data.entity;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.sql.Time;
import java.util.Date;

/**
 * 
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author Martin Schraner
 */
@Entity
@Table(name = "ck_event")
@IdClass(EventId.class)
@NamedQueries({
		@NamedQuery(name = Event.GET_ALL_NOT_UPDATED_EVENTS_OF_CURRENT_IMPORT_PROCESS, query =
		"select new ch.uzh.extension.campuscourse.model.CourseIdDateStartEnd(e.course.id, e.date, e.start, e.end) from Event e where " +
				"e.dateOfLatestImport < :lastDateOfImport " +
				"and e.course.semester.id = :semesterIdOfCurrentImportProcess"),
		@NamedQuery(name = Event.GET_EVENTS_BY_COURSE_ID, query = "select e from Event e where " +
				"e.course.id = :courseId"),
		@NamedQuery(name = Event.DELETE_EVENTS_BY_COURSE_IDS, query = "delete from Event e where " +
				"e.course.id in :courseIds"),
		@NamedQuery(name = Event.DELETE_EVENTS_BY_COURSE_ID_DATE_START_END, query = "delete from Event e where " +
				"e.course.id = :courseId " +
				"and e.date = :date " +
				"and e.start = :start " +
				"and e.end = :end"),
		@NamedQuery(name = Event.DELETE_ALL_EVENTS_TOO_FAR_IN_THE_PAST, query = "delete from Event e where " +
				"e.dateOfLatestImport < :nYearsInThePast"),
		@NamedQuery(name = Event.DELETE_ALL_EVENTS_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED, query = "delete from Event e where " +
				"e.dateOfLatestImport < :nYearsInThePast " +
				"and e.course.id not in :courseIdsToBeExcluded")
})
public class Event {

	public static final String GET_ALL_NOT_UPDATED_EVENTS_OF_CURRENT_IMPORT_PROCESS = "getAllNotUpdatedEventsOfCurrentImportProcess";
	public static final String GET_EVENTS_BY_COURSE_ID ="getEventsByCourseId";
	public static final String DELETE_EVENTS_BY_COURSE_IDS = "deleteEventsByCourseIds";
	public static final String DELETE_EVENTS_BY_COURSE_ID_DATE_START_END = "deleteEventsByCourseIdDateStartEnd";
	public static final String DELETE_ALL_EVENTS_TOO_FAR_IN_THE_PAST = "deleteAllEventsTooFarInThePast";
	public static final String DELETE_ALL_EVENTS_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED = "deleteAllEventsTooFarInThePastExceptForCoursesToBeExcluded";

    @Id
	@ManyToOne
	@JoinColumn(name = "fk_course", nullable = false)
	private Course course;

	@Id
	@Temporal(TemporalType.DATE)
    @Column(name = "date", nullable = false)
    private Date date;

	@Id
    @Column(name = "start", nullable = false)
    private Time start;

	@Id
    @Column(name = "end", nullable = false)
    private Time end;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_first_import", nullable = false)
    private Date dateOfFirstImport;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_latest_import", nullable = false)
    private Date dateOfLatestImport;

    public Event() {
    }

    public Event(Course course, Date date, Time start, Time end, Date dateOfLatestImport) {
    	this.course = course;
        this.date = date;
        this.start = start;
        this.end = end;
        this.dateOfLatestImport = dateOfLatestImport;
    }

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getStart() {
        return start;
    }

    public void setStart(Time start) {
        this.start = start;
    }

    public Time getEnd() {
        return end;
    }

    public void setEnd(Time end) {
        this.end = end;
    }

	public Date getDateOfFirstImport() {
		return dateOfFirstImport;
	}

	public void setDateOfFirstImport(Date dateOfFirstImport) {
		this.dateOfFirstImport = dateOfFirstImport;
	}

	public Date getDateOfLatestImport() {
        return dateOfLatestImport;
    }

    public void setDateOfLatestImport(Date dateOfImport) {
        this.dateOfLatestImport = dateOfImport;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("courseId", course.getId());
        builder.append("date", getDate().toString());
        builder.append("start", getStart().toString());
        builder.append("end", getEnd().toString());

        return builder.toString();
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Event event = (Event) o;

		if (!course.equals(event.course)) return false;
		if (!date.equals(event.date)) return false;
		if (!start.equals(event.start)) return false;
		return end.equals(event.end);
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
