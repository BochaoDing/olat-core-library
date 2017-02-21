package ch.uzh.extension.campuscourse.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
@Scope("prototype")
public class EventCourseId {

    private Date date;
    private String start;
    private String end;
    private Date dateOfLatestImport;
    private Long courseId;

    public EventCourseId() {
    }

    public EventCourseId(Date date, String start, String end, Date dateOfLatestImport, Long courseId) {
        this.date = date;
        this.start = start;
        this.end = end;
        this.dateOfLatestImport = dateOfLatestImport;
        this.courseId = courseId;
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

    public Date getDateOfLatestImport() {
        return dateOfLatestImport;
    }

    public void setDateOfLatestImport(Date dateOfLatestImport) {
        this.dateOfLatestImport = dateOfLatestImport;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
