package ch.uzh.campus.importers;

import ch.uzh.campus.connectors.CampusUtils;
import ch.uzh.campus.data.*;
import ch.uzh.campus.importers.Importer;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

public class StudentImporter extends Importer {

    private List<Student> students = new ArrayList<Student>();
    private Set<Long> processedIdsSet;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private DaoManager daoManager;

    @PostConstruct
    public void init() {
        processedIdsSet = new HashSet<Long>();
    }

    @PreDestroy
    public void cleanUp() {
        processedIdsSet.clear();
    }

    @Override
    void processEntry(String[] entry) {
        // Prepare a Student object for persistence
        try {
            // Ignore the duplicates
            Long studentId = Long.parseLong(entry[0]);
            if (!CampusUtils.addIfNotAlreadyProcessed(processedIdsSet, studentId)) {
                LOG.debug("Found a duplicate student: [" + studentId + "]");
                skipEntry(entry, Importer.SKIP_REASON_DUPLICATE_ID);
            }
            // Prepare Student object for persistence
            Student student = new Student();
            student.setId(studentId);
            student.setRegistrationNr(entry[1]);
            student.setFirstName(entry[2]);
            student.setLastName(entry[3]);
            student.setEmail(entry[4]);
            student.setModifiedDate(new Date());
            students.add(student);
            if (students.size() % Importer.COMMIT_INTERVAL == 0) {
                persist();
            }
        } catch (Exception e) {
            System.out.println("Exception while processing Student entry: " + e.getMessage());
            cntFailed++;
        }
    }

    @Override
    int getEntryFieldCount() {
        return 6;
    }

    @Override
    void persist() {
        persistList(students, studentDao);
    }

    // TODO find the right place to use. In CampusInterceptor old data is used in afterStep() - why?
    private void deleteOldRecords() {
        List<Long> studentsToBeRemoved = daoManager.getAllStudentsToBeDeleted(this.startTime);
        LOG.info("STUDENTS TO BE REMOVED [" + studentsToBeRemoved.size() + "]");
        if (!studentsToBeRemoved.isEmpty()) {
            daoManager.deleteStudentsAndBookingsByStudentIds(studentsToBeRemoved);
        }
    }

}
