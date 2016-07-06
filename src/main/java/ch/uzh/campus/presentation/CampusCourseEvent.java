
package ch.uzh.campus.presentation;

import org.olat.core.util.event.MultiUserEvent;

public class CampusCourseEvent extends MultiUserEvent {

    private Long campusCourseResourceableId;
    private int status;

    public static final int CREATED = 1;
    public static final int DELETED = 2;
    public static final int CONTINUED = 3;

    public CampusCourseEvent() {
        super("campusCourse_event");

    }

    public CampusCourseEvent(Long campusCourseId, int status) {
        this();
        this.campusCourseResourceableId = campusCourseId;
        this.status = status;
    }

    public Long getCampusCourseResourceableId() {
        return campusCourseResourceableId;
    }

    public void setCampusCourseResourceableId(Long campusCourseResourceableId) {
        this.campusCourseResourceableId = campusCourseResourceableId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
