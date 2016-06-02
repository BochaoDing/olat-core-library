package ch.uzh.campus.data;

import java.util.List;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
public interface CampusDao<T> {
    void save(List<T> items);
    void saveOrUpdate(List<T> items);
}
