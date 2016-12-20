package ch.uzh.extension.campuscourse.data.entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * 
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_event")
@NamedQueries({ @NamedQuery(name = Event.GET_IDS_OF_ALL_EVENTS, query = "select e.id from Event e"),
        @NamedQuery(name = Event.GET_EVENTS_BY_COURSE_ID, query = "select e from Event e where e.course.id = :courseId"),
        @NamedQuery(name = Event.GET_EVENT_IDS_BY_COURSE_ID, query = "select e.id from Event e where e.course.id = :courseId"),
        @NamedQuery(name = Event.GET_EVENT_IDS_BY_COURSE_IDS, query = "select e.id from Event e where e.course.id in :courseIds"),
        @NamedQuery(name = Event.DELETE_ALL_EVENTS, query = "delete from Event"),
        @NamedQuery(name = Event.DELETE_EVENTS_BY_COURSE_IDS, query = "delete from Event e where e.course.id in :courseIds")
})
public class Event {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id;
    
    @Column(name = "date", nullable = false)
    private Date date;
    
    @Column(name = "start", nullable = false)
    private String start;
    
    @Column(name = "end", nullable = false)
    private String end;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_import", nullable = false)
    private Date dateOfImport;

    @ManyToOne
    @JoinColumn(name = "fk_course", nullable = false)
    private Course course;

    public Event() {
    }

    public Event(Date date, String start, String end, Date dateOfImport) {
        this.date = date;
        this.start = start;
        this.end = end;
        this.dateOfImport = dateOfImport;
    }

    public static final String GET_IDS_OF_ALL_EVENTS = "getIdsOfAllEvents";
    public static final String GET_EVENT_IDS_BY_COURSE_ID = "getEventIdsByCourseId";
    public static final String GET_EVENT_IDS_BY_COURSE_IDS = "getEventIdsByCourseIds";
    public static final String GET_EVENTS_BY_COURSE_ID = "getEventsByCourseId";
    public static final String DELETE_ALL_EVENTS = "deleteAllEvents";
    public static final String DELETE_EVENTS_BY_COURSE_IDS = "deleteEventsByCourseIds";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Date getDateOfImport() {
        return dateOfImport;
    }

    public void setDateOfImport(Date dateOfImport) {
        this.dateOfImport = dateOfImport;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("courseId", course.getId());
        builder.append("date", getDate());
        builder.append("start", getStart());
        builder.append("end", getEnd());

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (!date.equals(event.date)) return false;
        if (!start.equals(event.start)) return false;
        return end.equals(event.end);

    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }
}
