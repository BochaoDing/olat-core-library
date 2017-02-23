package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.data.entity.*;
import ch.uzh.extension.campuscourse.model.CourseIdTextTypeIdLineNumber;
import ch.uzh.extension.campuscourse.model.TextCourseId;
import ch.uzh.extension.campuscourse.util.DateUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
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
public class TextDao {

    private static final OLog LOG = Tracing.createLoggerFor(TextDao.class);
	private static final String BREAK_TAG = "<br>";

    private final CampusCourseConfiguration campusCourseConfiguration;
    private final DB dbInstance;
    private final TextTypeDao textTypeDao;
    private final CourseDao courseDao;

    @Autowired
    public TextDao(CampusCourseConfiguration campusCourseConfiguration, DB dbInstance, TextTypeDao textTypeDao, CourseDao courseDao) {
		this.campusCourseConfiguration = campusCourseConfiguration;
		this.dbInstance = dbInstance;
		this.textTypeDao = textTypeDao;
		this.courseDao = courseDao;
	}

	public void save(TextCourseId textCourseId) {
		Course course = dbInstance.findObject(Course.class, textCourseId.getCourseId());
		if (course == null) {
			String warningMessage = "No course found with id " + textCourseId.getCourseId() + ". Skipping all texts of this course for table ck_text.";
			// Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by TextWriter)
			LOG.debug(warningMessage);
			throw new EntityNotFoundException(warningMessage);
		}
		TextType textType = findOrCreateTextType(textCourseId);
		Text text = new Text(course, textType, textCourseId.getLineNumber(), textCourseId.getLine(), textCourseId.getDateOfLatestImport());
		text.setDateOfFirstImport(text.getDateOfLatestImport());
		course.getTexts().add(text);
	}

	public void save(List<TextCourseId> textCourseIds) {
		textCourseIds.forEach(this::save);
	}

	public void saveOrUpdate(TextCourseId textCourseId) {
    	Text textFound = getTextById(textCourseId.getCourseId(), textCourseId.getTextTypeId(), textCourseId.getLineNumber());
    	if (textFound != null) {
    		textCourseId.mergeImportedAttributesInto(textFound);
		} else {
    		save(textCourseId);
		}
	}

	public void saveOrUpdate(List<TextCourseId> textCourseIds) {
    	textCourseIds.forEach(this::saveOrUpdate);
	}

	private TextType findOrCreateTextType(TextCourseId textCourseId) {
    	TextType textType = textTypeDao.getTextTypeById(textCourseId.getTextTypeId());
    	if (textType == null) {
    		// Create new text type
			textType = new TextType(textCourseId.getTextTypeId(), textCourseId.getTextTypeName());
			textTypeDao.save(textType);
		}
		return textType;
	}

	Text getTextById(Long courseId, int textTypeId, int lineNumber) {
		return dbInstance.getCurrentEntityManager().find(Text.class, new TextId(courseId, textTypeId, lineNumber));
	}

    public List<Text> getTextsByCourseId(Long courseId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_TEXTS_BY_COURSE_ID, Text.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    public String getContentsByCourseId(Long courseId) {
        List<Text> texts = getTextsByCourseIdAndTextTypeName(courseId, TextType.CONTENTS);
        return buildText(texts);
    }

    public String getMaterialsByCourseId(Long courseId) {
        List<Text> texts = getTextsByCourseIdAndTextTypeName(courseId, TextType.MATERIALS);
        return buildText(texts);
    }

    public String getInfosByCourseId(Long courseId) {
        List<Text> texts = getTextsByCourseIdAndTextTypeName(courseId, TextType.INFOS);
        return buildText(texts);
    }

    private List<Text> getTextsByCourseIdAndTextTypeName(Long courseId, String textTypeName) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_TEXTS_BY_COURSE_ID_AND_TEXT_TYPE_NAME, Text.class)
                .setParameter("courseId", courseId)		
                .setParameter("textTypeName", textTypeName)
                .getResultList();
    }

    private String buildText(List<Text> texts) {
        StringBuilder content = new StringBuilder();
        for (Text text : texts) {
            content.append(text.getLine());
            content.append(TextDao.BREAK_TAG);
        }
        return content.toString();
    }

	public List<CourseIdTextTypeIdLineNumber> getAllNotUpdatedTextsOfCurrentImportProcess(Date date, Semester semesterOfCurrentImportProcess) {
		// Subtract one second since modifiedDate (used in query) is rounded to seconds
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery(Text.GET_ALL_NOT_UPDATED_TEXTS_OF_CURRENT_IMPORT_PROCESS, CourseIdTextTypeIdLineNumber.class)
				.setParameter("lastDateOfImport", DateUtil.addSecondsToDate(date, -1))
				.setParameter("semesterIdOfCurrentImportProcess", semesterOfCurrentImportProcess.getId())
				.getResultList();
	}

	public void delete(Text text) {
		deleteTextBidirectionally(text);
	}

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
	int deleteTextsByCourseIdsAsBulkDelete(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
        	return 0;
		}
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.DELETE_TEXTS_BY_COURSE_IDS)
                .setParameter("courseIds", courseIds)
                .executeUpdate();
    }

	/**
	 * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
	 */
	public int deleteByCourseIdTextTypeIdLineNumbersAsBulkDelete(List<CourseIdTextTypeIdLineNumber> courseIdTextTypeIdLineNumbers) {
		EntityManager entityManager = dbInstance.getCurrentEntityManager();
		int count = 0;
		for (CourseIdTextTypeIdLineNumber courseIdTextTypeIdLineNumber : courseIdTextTypeIdLineNumbers) {
			count += entityManager
					.createNamedQuery(Text.DELETE_TEXTS_BY_COURSE_ID_TEXT_TYPE_ID_LINE_NUMBER)
					.setParameter("courseId", courseIdTextTypeIdLineNumber.getCourseId())
					.setParameter("textTypeId", courseIdTextTypeIdLineNumber.getTextTypeId())
					.setParameter("lineNumber", courseIdTextTypeIdLineNumber.getLineNumber())
					.executeUpdate();
		}
		return count;
	}

	/**
	 * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
	 */
	public int deleteAllTextsOfNotContinuedCoursesTooFarInThePastAsBulkDelete(Date date) {
		List<Long> courseIdsToBeExcluded = courseDao.getIdsOfContinuedCoursesTooFarInThePast(date);
		if (courseIdsToBeExcluded.isEmpty()) {
			// JPA would crash if courseIdsToBeExcluded was empty, so we have to use a query without courseIdsToBeExcluded
			return dbInstance.getCurrentEntityManager()
					.createNamedQuery(Text.DELETE_ALL_TEXTS_TOO_FAR_IN_THE_PAST)
					.setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
					.executeUpdate();
		} else {
			return dbInstance.getCurrentEntityManager()
					.createNamedQuery(Text.DELETE_ALL_TEXTS_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED)
					.setParameter("nYearsInThePast", DateUtil.addYearsToDate(date, -campusCourseConfiguration.getMaxYearsToKeepCkData()))
					.setParameter("courseIdsToBeExcluded", courseIdsToBeExcluded)
					.executeUpdate();
		}
	}

    private void deleteTextBidirectionally(Text text) {
		Course course = text.getCourse();
		course.getTexts().remove(text);
    }
}

