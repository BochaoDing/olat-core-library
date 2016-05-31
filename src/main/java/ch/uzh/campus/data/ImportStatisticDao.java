package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ImportStatisticDao implements CampusDao<ImportStatistic> {

    @Autowired
    private DB dbInstance;

    @Override
    public void save(List<ImportStatistic> statistics) {
        for(ImportStatistic statistic : statistics) {
            dbInstance.saveObject(statistic);
        }
    }

    @Override
    public void saveOrUpdate(List<ImportStatistic> items) {
        save(items);
    }

    public void save(ImportStatistic statistic) {
        dbInstance.saveObject(statistic);
    }

    public List<ImportStatistic> getLastCompletedImportedStatistic() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(ImportStatistic.GET_LAST_COMPLETED_IMPORT_STATISTIC, ImportStatistic.class)
                .getResultList();
    }
}
