package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.data.entity.BatchJobSkippedItem;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BatchJobSkippedItemDao {

    private final DB dbInstance;

    @Autowired
    public BatchJobSkippedItemDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(BatchJobSkippedItem batchJobSkippedItem) {
        dbInstance.saveObject(batchJobSkippedItem);
    }
}
