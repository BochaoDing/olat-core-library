package ch.uzh.campus.data;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 */
@Repository
public class ExportDao implements CampusDao<Export> {

    private final DB dbInstance;

    @Autowired
    public ExportDao(DB dbInstance) {
        this.dbInstance = dbInstance;
    }

    public void save(List<Export> exports) {
        exports.forEach(dbInstance::saveObject);
    }

    @Override
    public void saveOrUpdate(List<Export> exports) {
        EntityManager em = dbInstance.getCurrentEntityManager();
        exports.forEach(em::merge);
    }
}

