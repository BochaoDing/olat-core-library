package ch.uzh.campus.data;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 */
@Repository
public class ExportDao implements CampusDao<Export> {
	@Autowired
    private DB dbInstance;
    
    @Override
    public void save(List<Export> exports) {
    	for(Export export: exports) {
    		dbInstance.saveObject(export);
    	}
    }

}

