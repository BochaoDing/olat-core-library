package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SkipItemDao implements CampusDao<SkipItem> {

    @Autowired
    private DB dbInstance;

    @Override
    public void save(List<SkipItem> skipItems) {
        for (SkipItem skipItem : skipItems) {
            save(skipItem);
        }
    }

    @Override
    public void saveOrUpdate(List<SkipItem> skipItems) {
        for (SkipItem skipItem : skipItems) {
            saveOrUpdate(skipItem);
        }
    }

    public void save(SkipItem skipItem) {
        dbInstance.saveObject(skipItem);
    }

    public void saveOrUpdate(SkipItem skipItem) {
        dbInstance.getCurrentEntityManager().merge(skipItem);
    }
}
