package ch.uzh.campus.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 */
@Repository
public class TextDao implements CampusDao<Text> {

	@Autowired
    private DB dbInstance;

    @Override
    public void save(List<Text> texts) {
        //genericDao.save(texts);
    	for(Text text:texts) {
    		dbInstance.saveObject(text);
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
                .createNamedQuery(Text.GET_TEXTS, Text.class) 
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

    /*public int deleteAllTexts() {
        return genericDao.getNamedQuery(Text.DELETE_ALL_TEXTS).executeUpdate();
    }

    public void deleteTextsByCourseId(Long courseId) {
        Query query = genericDao.getNamedQuery(Text.DELETE_TEXTS_BY_COURSE_ID);
        query.setParameter("courseId", courseId);
        query.executeUpdate();
    }

    public void deleteTextsByCourseIds(List<Long> courseIds) {
        Query query = genericDao.getNamedQuery(Text.DELETE_TEXTS_BY_COURSE_IDS);
        query.setParameterList("courseIds", courseIds);
        query.executeUpdate();
    }
*/
}

