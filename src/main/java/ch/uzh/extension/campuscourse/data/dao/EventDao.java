package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.entity.Event;
import ch.uzh.extension.campuscourse.data.entity.EventId;
import ch.uzh.extension.campuscourse.data.entity.Semester;
import ch.uzh.extension.campuscourse.model.CourseIdDateStartEnd;
import ch.uzh.extension.campuscourse.model.EventCourseId;
import ch.uzh.extension.campuscourse.util.DateUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.sql.Time;
import java.util.Date;
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

    private final CampusCourseConfiguration campusCourseConfiguration;
    private final DB dbInstance;
    private final CourseDao courseDao;

    @Autowired
    public EventDao(CampusCourseConfiguration campusCourseConfiguration, DB dbInstance, CourseDao courseDao) {
        this.campusCourseConfiguration = campusCourseConfiguration;
        this.dbInstance = dbInstance;
        this.courseDao = courseDao;
    }

	public void save(EventCourseId eventCourseId) {
		Course course = dbInstance.findObject(Course.class, eventCourseId.getCourseId());
		if (course == null) {
			String warningMessage = "No course found with id " + eventCourseId.getCourseId() + ". Skipping all events of this course for table ck_event.";
			// Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by EventWriter)
			LOG.debug(warningMessage);
			throw new EntityNotFoundException(warningMessage);
		}
		Event event = new Event(
				course,
				eventCourseId.getDate(),
				Time.valueOf(eventCourseId.getStart()),
				Time.valueOf(eventCourseId.getEnd()),
				eventCourseId.getDateOfLatestImport());
		event.setDateOfFirstImport(event.getDateOfLatestImport());
		course.getEvents().add(event);
	}

	public void save(List<EventCourseId> eventCourseIds) {
    	eventCourseIds.forEach(this::save);
	}

	public void saveOrUpdate(EventCourseId eventCourseId) {
		Event eventFound = getEventById(
				eventCourseId.getCourseId(),
				eventCourseId.getDate(),
				Time.valueOf(eventCourseId.getStart()),
				Time.valueOf(eventCourseId.getEnd()));
		if (eventFound != null) {
			eventCourseId.mergeImportedAttributesInto(eventFound);
		} else {
			save(eventCourseId);
		}
	}

	public void saveOrUpdate(List<EventCourseId> eventCourseIds) {
		eventCourseIds.forEach(this::saveOrUpdate);
	}

	Event getEventById(Long courseId, Date date, Time start, Time end) {
		return dbInstance.getCurrentEntityManager().find(Event.class, new EventId(courseId, date, start, end));
	}

    public List<Event> getEventsByCourseId(Long id) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Event.GET_EVENTS_BY_COURSE_ID, Event.class)
                .setParameter("courseId", id)
                .getResultList();
    }

	public List<CourseIdDateStartEnd> getAllNotUpdatedEventsOfCurrentImportProcess(Date date, Semester semesterOfCurrentImportProcess) {
		// Subtract one second since modifiedDate (used in query) is rounded to seconds
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery(Event.GET_ALL_NOT_UPDATED_EVENTS_OF_CURRENT_IMPORT_PROCESS, CourseIdDateStartEnd.class)
				.setParameter("lastDateOfImport", DateUtil.addSecondsToDate(date, -1))
				.setParameter("semesterIdOfCurrentImportProcess", semesterOfCurrentImportProcess.getId())
				.getResultList();
	}

	public void delete(Event event) {
		deleteEventBidirectionally(event);
	}

	/**
	 * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
	 */
	int deleteEventsByCourseIdsAsBulkDelete(List<Long> courseIds) {
		if (courseIds.isEmpty()) {
			return 0;
		}
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery(Event.DELETE_EVENTS_BY_COURSE_IDS)
				.setParameter("courseIds", courseIds)
				.executeUpdate();
	}

	/**
	 * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
	 */
	public int deleteByCourseIdDateStartEndsAsBulkDelete(List<CourseIdDateStartEnd> courseIdDateStartEnds) {
		EntityManager entityManager = dbInstance.getCurrentEntityManager();
		int count = 0;
		for (CourseIdDateStartEnd courseIdDateStartEnd : courseIdDateStartEnds) {
			count += entityManager
					.createNamedQuery(Event.DELETE_EVENTS_BY_COURSE_ID_DATE_START_END)
					.setParameter("courseId", courseIdDateStartEnd.getCourseId())
					.setParameter("date", courseIdDateStartEnd.getDate())
					.setParameter("start", courseIdDateStartEnd.getStart())
					.setParameter("end", courseIdDateStartEnd.getEnd())
					.executeUpdate();
		}
		return count;
	}

	/**
	 * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
	 */
	public int deleteAllEventsOfNotContinuedCoursesTooFarInThePastAsBulkDelete(Date date) {
		List<Long> courseIdsToBeExcluded = courseDao.getIdsOfContinuedCoursesTooFarInThePast(date);
		if (courseIdsToBeExcluded.isEmpty()) {
			// JPA would crash if courseIdsToBeExcluded was empty, so we have to use a query without courseIdsToBeExcluded
			return dbInstance.getCurrentEntityManager()
					.createNamedQuery(Event.DELETE_ALL_EVENTS_TOO_FAR_IN_THE_PAST)
					.setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
					.executeUpdate();
		} else {
			return dbInstance.getCurrentEntityManager()
					.createNamedQuery(Event.DELETE_ALL_EVENTS_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED)
					.setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
					.setParameter("courseIdsToBeExcluded", courseIdsToBeExcluded)
					.executeUpdate();
		}
	}

	private void deleteEventBidirectionally(Event event) {
		Course course = event.getCourse();
		course.getEvents().remove(event);
	}
}
