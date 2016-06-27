
package ch.uzh.campus.presentation;

import org.olat.core.util.event.MultiUserEvent;

public class CampusCourseEvent extends MultiUserEvent {

    private Long campusCourseId;
    private int status;

    public static final int CREATED = 1;
    public static final int DELETED = 2;

    public CampusCourseEvent() {
        super("campusCourse_event");

    }

    public CampusCourseEvent(Long campusCourseId, int status) {
        this();
        this.campusCourseId = campusCourseId;
        this.status = status;
    }

    public Long getCampusCourseId() {
        return campusCourseId;
    }

    public void setCampusCourseId(Long campusCourseId) {
        this.campusCourseId = campusCourseId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
