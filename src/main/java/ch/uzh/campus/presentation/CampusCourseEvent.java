
package ch.uzh.campus.presentation;

import org.olat.core.util.event.MultiUserEvent;

public class CampusCourseEvent extends MultiUserEvent {

    private int status;

    public static final int CREATED = 1;
    public static final int DELETED = 2;
    public static final int CONTINUED = 3;

    public CampusCourseEvent() {
        super("campusCourse_event");

    }

    public CampusCourseEvent(int status) {
        this();
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
