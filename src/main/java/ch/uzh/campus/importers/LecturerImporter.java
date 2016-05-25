package ch.uzh.campus.importers;

import ch.uzh.campus.connectors.CampusUtils;
import ch.uzh.campus.data.*;
import ch.uzh.campus.importers.Importer;
import org.apache.commons.lang.StringUtils;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class LecturerImporter extends Importer {

    private List<Lecturer> lecturers = new ArrayList<Lecturer>();
    private Set<Long> processedIdsSet = new HashSet<Long>();

    @Autowired
    private LecturerDao lecturerDao;

    @Autowired
    private DaoManager daoManager;

    @Override
    void processEntry(String[] entry) {
        try {
            // Ignore the duplicates
            Long personalNr = Long.parseLong(entry[0]);
            if (!CampusUtils.addIfNotAlreadyProcessed(processedIdsSet, personalNr)) {
                LOG.debug("This is a duplicate of this lecturer [" + personalNr + "]");
                skipEntry(entry, Importer.SKIP_REASON_DUPLICATE_ID);
            }
            // Take private email if normal email is not given
            String email = entry[5];
            if (StringUtils.isBlank(email)) {
                email = entry[4];
            }
            // Prepare Lecturer object for persistence
            Lecturer lecturer = new Lecturer();
            lecturer.setPersonalNr(personalNr);
            lecturer.setFirstName(entry[1]);
            lecturer.setLastName(entry[2]);
            lecturer.setEmail(email);
            lecturer.setModifiedDate(new Date());

            lecturers.add(lecturer);
            if (lecturers.size() % Importer.COMMIT_INTERVAL == 0) {
                persist();
            }
        } catch(Exception e) {
            System.out.println("Exception while processing Lecturer entry: " + e.getMessage());
            cntFailed++;
        }

    }

    @Override
    int getEntryFieldCount() {
        return 5;
    }

    @Override
    void persist() {
        persistList(lecturers, lecturerDao);
    }

    // TODO find the right place to use. In CampusInterceptor old data is used in afterStep() - why?
    private void deleteOldRecords() {
        List<Long> lecturersToBeRemoved = daoManager.getAllLecturersToBeDeleted(this.startTime);
        LOG.info("LECTURERS TO BE REMOVED [" + lecturersToBeRemoved.size() + "]");
        if (!lecturersToBeRemoved.isEmpty()) {
            daoManager.deleteLecturersAndBookingsByLecturerIds(lecturersToBeRemoved);
        }
    }

}
