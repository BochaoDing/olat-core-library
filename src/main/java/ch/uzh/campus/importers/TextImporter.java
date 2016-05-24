package ch.uzh.campus.importers;

import ch.uzh.campus.importers.Importer;
import org.springframework.stereotype.Service;

@Service
public class TextImporter extends Importer {

    @Override
    void processEntry(String[] entry) {

    }

    @Override
    void skipEntry(String[] entry, String reason) {
        LOG.info("Skipped entry(" + reason + "):" + String.join(";", entry));
    }

    @Override
    int getEntryFieldCount() {
        return 2;
    }

    @Override
    void persist() {

    }
}
