package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.data.entity.ImportStatistic;
import org.apache.commons.lang.time.DateUtils;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
public class ImportStatisticDao implements CampusDao<ImportStatistic> {

    private final DB dbInstance;

    @Autowired
    public ImportStatisticDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(List<ImportStatistic> statistics) {
        statistics.forEach(this::save);
    }

    @Override
    public void saveOrUpdate(List<ImportStatistic> statistics) {
        statistics.forEach(this::saveOrUpdate);
    }

    public void save(ImportStatistic statistic) {
        dbInstance.saveObject(statistic);
    }

    public void saveOrUpdate(ImportStatistic statistic) {
        dbInstance.getCurrentEntityManager().merge(statistic);
    }

    public List<ImportStatistic> getLastCompletedImportedStatistic() {
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(ImportStatistic.GET_LAST_COMPLETED_IMPORT_STATISTIC, ImportStatistic.class)
                .getResultList();
    }

    public List<ImportStatistic> getImportStatisticOfToday() {
        Date midnight = DateUtils.truncate(new Date(), Calendar.DATE);
        return dbInstance.getCurrentEntityManager()
                .createNamedQuery(ImportStatistic.GET_IMPORT_STATISTICS_OF_TODAY, ImportStatistic.class)
                .setParameter("midnight", midnight)
                .getResultList();
    }
}
