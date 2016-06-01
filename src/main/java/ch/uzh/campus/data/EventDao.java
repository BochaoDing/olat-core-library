package ch.uzh.campus.data;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 04.06.2012 <br>
 *
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@Repository
public class EventDao implements CampusDao<Event> {

    @Autowired
    private DB dbInstance;

    @Override
    public void save(List<Event> events) {
        for (Event event : events) {
            dbInstance.saveObject(event);
        }
    }

    @Override
    public void saveOrUpdate(List<Event> items) {
        save(items);
    }

    public void save(Event event) {
        dbInstance.saveObject(event);
    }

    public void addEventToCourse(Event event, Long courseId) {
        Course course = dbInstance.getCurrentEntityManager().getReference(Course.class, courseId);
        event.setCourse(course);
        course.getEvents().add(event);
    }

    public void addEventToCourse(EventCourseId eventCourseId) {
        Event event = new Event(eventCourseId.getDate(), eventCourseId.getStart(), eventCourseId.getEnd(), eventCourseId.getModifiedDate());
        addEventToCourse(event, eventCourseId.getCourseId());
    }

    public void addEventsToCourse(List<EventCourseId> eventCourseIds) {
        for (EventCourseId eventCourseId : eventCourseIds) {
            addEventToCourse(eventCourseId);
        }
    }

    public List<Event> getEventsByCourseId(Long id) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_EVENTS_BY_COURSE_ID, Event.class)
                .setParameter("courseId", id)
                .getResultList();
    }

    public int deleteAllEvents() {
        List<Long> idsOfEventsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_IDS_OF_ALL_EVENTS, Long.class)
                .getResultList();
        deleteEventsBidirectionally(idsOfEventsToBeDeleted);
        return idsOfEventsToBeDeleted.size();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteAllEventsAsBulkDelete() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.DELETE_ALL_EVENTS)
                .executeUpdate();
    }

    public int deleteEventsByCourseId(Long courseId) {
        List<Long> idsOfEventsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_EVENT_IDS_BY_COURSE_ID, Long.class)
                .setParameter("courseId", courseId)
                .getResultList();
        deleteEventsBidirectionally(idsOfEventsToBeDeleted);
        return idsOfEventsToBeDeleted.size();
    }

    public int deleteEventsByCourseIds(List<Long> courseIds) {
        List<Long> idsOfEventsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_EVENT_IDS_BY_COURSE_IDS, Long.class)
                .setParameter("courseIds", courseIds)
                .getResultList();
        deleteEventsBidirectionally(idsOfEventsToBeDeleted);
        return idsOfEventsToBeDeleted.size();
    }

    private void deleteEventsBidirectionally(List<Long> idsOfEventsToBeDeleted) {
        for (Long id : idsOfEventsToBeDeleted) {
            Event event = dbInstance.getCurrentEntityManager().getReference(Event.class, id);
            Course course = event.getCourse();
            course.getEvents().remove(event);
        }
    }

}
