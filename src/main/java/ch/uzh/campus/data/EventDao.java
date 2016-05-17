package ch.uzh.campus.data;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    public void save(Event event) {
        dbInstance.saveObject(event);
    }

    public List<Event> getEventsByCourseId(Long id) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_EVENTS_BY_COURSE_ID, Event.class)
                .setParameter("courseId", id)
                .getResultList();
    }

    public int deleteAllEvents() {
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
