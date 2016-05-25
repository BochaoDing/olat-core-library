package ch.uzh.campus.importers;

import ch.uzh.campus.importers.Importer;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StudentCourseImporter extends Importer {

    @Override
    void processEntry(String[] entry) {

    }

    @Override
    int getEntryFieldCount() {
        return 4;
    }

    @Override
    void persist() {

    }

    // TODO find the right place to use. In CampusInterceptor old data is used in afterStep() - why?
    private void deleteOldRecords() {
        // TODO
//            int stornos = daoManager.deleteAllNotUpdatedSCBooking(this.startTime);
//            LOG.info("STORNOS(STUDENT_COURSE): " + stornos);
    }
}