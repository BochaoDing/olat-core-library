package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.common.CampusCourseException;

import java.util.List;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
public interface CampusDao<T> {
    void saveOrUpdate(List<T> items) throws CampusCourseException;
}
