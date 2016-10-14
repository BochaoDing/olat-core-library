package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SkipItemDao implements CampusDao<SkipItem> {

    private final DB dbInstance;

    @Autowired
    public SkipItemDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(SkipItem skipItem) {
        dbInstance.saveObject(skipItem);
    }

    public void save(List<SkipItem> skipItems) {
        skipItems.forEach(this::save);
    }

    public void saveOrUpdate(SkipItem skipItem) {
        dbInstance.getCurrentEntityManager().merge(skipItem);
    }

    @Override
    public void saveOrUpdate(List<SkipItem> skipItems) {
        skipItems.forEach(this::saveOrUpdate);
    }
}
