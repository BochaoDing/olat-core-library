package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@Repository
public class TextDao implements CampusDao<Text> {

	@Autowired
    private DB dbInstance;

    @Override
    public void save(List<Text> texts) {
        for(Text text : texts) {
            dbInstance.saveObject(text);
        }
    }

    public void save(Text text) {
        dbInstance.saveObject(text);
    }

    public void addTextToCourse(Text text, Long courseId) {
        Course course = dbInstance.getCurrentEntityManager().getReference(Course.class, courseId);
        text.setCourse(course);
        course.getTexts().add(text);
    }

    public void addTextToCourse(TextCourseId textCourseId) {
        addTextToCourse(textCourseId, textCourseId.getCourseId());
    }

    public void addTextsToCourse(List<TextCourseId> textCourseIds) {
        for (TextCourseId textCourseId : textCourseIds) {
            addTextToCourse(textCourseId);
        }
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
        StringBuffer content = new StringBuffer();
        for (Text text : texts) {
            content.append(text.getLine());
            content.append(Text.BREAK_TAG);
        }
        return content.toString();
    }

    public int deleteAllTexts() {
        List<Long> idsOfTextsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_IDS_OF_ALL_TEXT, Long.class)
                .getResultList();
        deleteTextsBidirectionally(idsOfTextsToBeDeleted);
        return idsOfTextsToBeDeleted.size();
    }

    public int deleteTextsByCourseId(Long courseId) {
        List<Long> idsOfTextsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_TEXT_IDS_BY_COURSE_ID, Long.class)
                .setParameter("courseId", courseId)
                .getResultList();
        deleteTextsBidirectionally(idsOfTextsToBeDeleted);
        return idsOfTextsToBeDeleted.size();
    }

    public int deleteTextsByCourseIds(List<Long> courseIds) {
        List<Long> idsOfTextsToBeDeleted = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Text.GET_TEXT_IDS_BY_COURSE_IDS, Long.class)
                .setParameter("courseIds", courseIds)
                .getResultList();
        deleteTextsBidirectionally(idsOfTextsToBeDeleted);
        return idsOfTextsToBeDeleted.size();
    }

    private void deleteTextsBidirectionally(List<Long> idsOfTextsToBeDeleted) {
        for (Long id : idsOfTextsToBeDeleted) {
            Text text = dbInstance.getCurrentEntityManager().getReference(Text.class, id);
            Course course = text.getCourse();
            course.getTexts().remove(text);
        }
    }
}

