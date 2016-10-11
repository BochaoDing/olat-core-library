package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseException;

import java.util.List;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
public interface CampusDao<T> {
    void save(List<T> items) throws CampusCourseException;
    void saveOrUpdate(List<T> items) throws CampusCourseException;
}
