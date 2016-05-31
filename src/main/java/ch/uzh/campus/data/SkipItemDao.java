package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class SkipItemDao implements CampusDao<SkipItem> {

    @Autowired
    private DB dbInstance;

    @Override
    public void save(List<SkipItem> skipItems) {
        for (SkipItem skipItem : skipItems) {
            dbInstance.saveObject(skipItem);
        }
    }

    @Override
    public void saveOrUpdate(List<SkipItem> items) {
        save(items);
    }

    public void save(SkipItem skipItem) {
        dbInstance.saveObject(skipItem);
    }

}
