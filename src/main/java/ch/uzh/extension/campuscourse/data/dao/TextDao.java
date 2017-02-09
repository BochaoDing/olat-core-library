package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.data.entity.Course;
import ch.uzh.extension.campuscourse.data.entity.Text;
import ch.uzh.extension.campuscourse.model.TextCourseId;
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
public class TextDao {

    private static final OLog LOG = Tracing.createLoggerFor(TextDao.class);

    private final DB dbInstance;

    @Autowired
    public TextDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    private void addTextToCourse(Text text, Long courseId) {
        Course course = dbInstance.findObject(Course.class, courseId);
        if (course == null) {
            String warningMessage = "No course found with id " + courseId + ". Skipping all texts of this course for table ck_text.";
            // Here we only log on the debug level to avoid duplicated warnings (LOG.warn is already called by TextWriter)
            LOG.debug(warningMessage);
            throw new EntityNotFoundException(warningMessage);
        }
        text.setCourse(course);
        course.getTexts().add(text);
    }

    public void addTextToCourse(TextCourseId textCourseId) {
        Text text = new Text(textCourseId.getType(), textCourseId.getLineSeq(), textCourseId.getLine(), textCourseId.getDateOfImport());
        addTextToCourse(text, textCourseId.getCourseId());
    }

    void addTextsToCourse(List<TextCourseId> textCourseIds) {
        textCourseIds.forEach(this::addTextToCourse);
    }

    public List<Text> getTextsByCourseId(Long courseId) {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_TEXTS_BY_COURSE_ID, Text.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    public String getContentsByCourseId(Long courseId) {
        List<Text> texts = getTextsByCourseIdAndType(courseId, Text.CONTENTS);
        return buildText(texts);
    }

    public String getMaterialsByCourseId(Long courseId) {
        List<Text> texts = getTextsByCourseIdAndType(courseId, Text.MATERIALS);
        return buildText(texts);
    }

    public String getInfosByCourseId(Long courseId) {
        List<Text> texts = getTextsByCourseIdAndType(courseId, Text.INFOS);
        return buildText(texts);
    }

    private List<Text> getTextsByCourseIdAndType(Long courseId, String type) {               
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_TEXTS_BY_COURSE_ID_AND_TYPE, Text.class)
                .setParameter("courseId", courseId)		
                .setParameter("type", type)		
                .getResultList();
    }

    private String buildText(List<Text> texts) {
        StringBuilder content = new StringBuilder();
        for (Text text : texts) {
            content.append(text.getLine());
            content.append(Text.BREAK_TAG);
        }
        return content.toString();
    }

    int deleteAllTexts() {
        List<Long> idsOfTextsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_IDS_OF_ALL_TEXTS, Long.class)
                .getResultList();
        deleteTextsBidirectionally(idsOfTextsToBeDeleted);
        return idsOfTextsToBeDeleted.size();
    }

    /**
     * Bulk delete for efficient deletion of a big number of entries. Does not update persistence context!
     */
    public int deleteAllTextsAsBulkDelete() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.DELETE_ALL_TEXTS)
                .executeUpdate();
    }

    public int deleteTextsByCourseId(Long courseId) {
        List<Long> idsOfTextsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_TEXT_IDS_BY_COURSE_ID, Long.class)
                .setParameter("courseId", courseId)
                .getResultList();
        deleteTextsBidirectionally(idsOfTextsToBeDeleted);
        return idsOfTextsToBeDeleted.size();
    }

    int deleteTextsByCourseIds(List<Long> courseIds) {
    	if (courseIds.isEmpty()) {
    		return 0;
		}
        List<Long> idsOfTextsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_TEXT_IDS_BY_COURSE_IDS, Long.class)
                .setParameter("courseIds", courseIds)
                .getResultList();
        deleteTextsBidirectionally(idsOfTextsToBeDeleted);
        return idsOfTextsToBeDeleted.size();
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

    private void deleteTextsBidirectionally(List<Long> idsOfTextsToBeDeleted) {
        for (Long id : idsOfTextsToBeDeleted) {
            Text text = dbInstance.getCurrentEntityManager().getReference(Text.class, id);
            Course course = text.getCourse();
            course.getTexts().remove(text);
        }
    }
}

