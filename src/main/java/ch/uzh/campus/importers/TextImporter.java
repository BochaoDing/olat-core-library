package ch.uzh.campus.importers;

import ch.uzh.campus.connectors.CampusUtils;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.data.Text;
import ch.uzh.campus.data.TextDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TextImporter extends Importer {

    @Autowired
    private DaoManager daoManager;

    @Autowired
    private TextDao textDao;

    private List<Text> texts = new ArrayList<Text>();

    @Override
    protected void beforeImport() {
        super.beforeImport();
        daoManager.deleteAllTexts();
    }

    @Override
    void processEntry(String[] entry) {
        try {
            Text text  = new Text();
            text.setLine(StringUtils.replace(text.getLine(), CampusUtils.SEMICOLON_REPLACEMENT, CampusUtils.SEMICOLON));
            text.setModifiedDate(new Date());
            texts.add(text);
            if (texts.size() % Importer.COMMIT_INTERVAL == 0) {
                persist();
            }
        } catch (Exception e) {
            failEntry(entry, Importer.SKIP_REASON_FAILED_TO_PROCESS);
            cntFailed++;
        }
    }

    @Override
    int getEntryFieldCount() {
        return 2;
    }

    @Override
    void persist() {
        //persistList(texts, textDao);
    }
}
