package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.data.entity.SkipItem;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SkipItemDao {

    private final DB dbInstance;

    @Autowired
    public SkipItemDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(SkipItem skipItem) {
        dbInstance.saveObject(skipItem);
    }
}
