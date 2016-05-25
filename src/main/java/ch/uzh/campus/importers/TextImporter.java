package ch.uzh.campus.importers;

import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.importers.Importer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TextImporter extends Importer {

    @Autowired
    private DaoManager daoManager;

    @Override
    protected void beforeImport() {
        super.beforeImport();
        daoManager.deleteAllTexts();
    }

    @Override
    void processEntry(String[] entry) {

    }

    @Override
    int getEntryFieldCount() {
        return 2;
    }

    @Override
    void persist() {

    }
}
