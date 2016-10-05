package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Initial Date: 04.06.2012 <br>
 *
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@Repository
public class EventDao {

    private static final OLog LOG = Tracing.createLoggerFor(EventDao.class);

    private final DB dbInstance;

    @Autowired
    public EventDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    private void addEventToCourse(Event event, Long courseId) {
        Course course = dbInstance.findObject(Course.class, courseId);
        if (course == null) {
            String warningMessage = "No course found with id " + courseId + ". Skipping all events of this course for table ck_event.";
            // Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by EventWriter)
            LOG.debug(warningMessage);
            throw new EntityNotFoundException(warningMessage);
        }
        event.setCourse(course);
        course.getEvents().add(event);
    }

    public void addEventToCourse(EventCourseId eventCourseId) {
        Event event = new Event(eventCourseId.getDate(), eventCourseId.getStart(), eventCourseId.getEnd(), eventCourseId.getDateOfImport());
        addEventToCourse(event, eventCourseId.getCourseId());
    }

    void addEventsToCourse(List<EventCourseId> eventCourseIds) {
        for (EventCourseId eventCourseId : eventCourseIds) {
            addEventToCourse(eventCourseId);
        }
    }

    List<Event> getEventsByCourseId(Long id) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_EVENTS_BY_COURSE_ID, Event.class)
                .setParameter("courseId", id)
                .getResultList();
    }

    int deleteAllEvents() {
        List<Long> idsOfEventsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_IDS_OF_ALL_EVENTS, Long.class)
                .getResultList();
        deleteEventsBidirectionally(idsOfEventsToBeDeleted);
        return idsOfEventsToBeDeleted.size();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteAllEventsAsBulkDelete() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.DELETE_ALL_EVENTS)
                .executeUpdate();
    }

    int deleteEventsByCourseId(Long courseId) {
        List<Long> idsOfEventsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_EVENT_IDS_BY_COURSE_ID, Long.class)
                .setParameter("courseId", courseId)
                .getResultList();
        deleteEventsBidirectionally(idsOfEventsToBeDeleted);
        return idsOfEventsToBeDeleted.size();
    }

    int deleteEventsByCourseIds(List<Long> courseIds) {
        List<Long> idsOfEventsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_EVENT_IDS_BY_COURSE_IDS, Long.class)
                .setParameter("courseIds", courseIds)
                .getResultList();
        deleteEventsBidirectionally(idsOfEventsToBeDeleted);
        return idsOfEventsToBeDeleted.size();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    int deleteEventsByCourseIdsAsBulkDelete(List<Long> courseIds) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.DELETE_EVENTS_BY_COURSE_IDS)
                .setParameter("courseIds", courseIds)
                .executeUpdate();
    }

    private void deleteEventsBidirectionally(List<Long> idsOfEventsToBeDeleted) {
        for (Long id : idsOfEventsToBeDeleted) {
            Event event = dbInstance.getCurrentEntityManager().getReference(Event.class, id);
            Course course = event.getCourse();
            course.getEvents().remove(event);
        }
    }

}
