package ch.uzh.campus.importers;

import ch.uzh.campus.connectors.CampusUtils;
import ch.uzh.campus.data.*;

import ch.uzh.campus.importers.Importer;
import org.apache.commons.lang.StringUtils;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CourseImporter extends Importer {

    @Autowired
    private CourseDao courseDao;

    @Autowired
    DaoManager daoManager;

    private static final String WHITESPACE = " ";
    private static final String SEMICOLON_REPLACEMENT = "&Semikolon&";
    private static final String SEMICOLON = ";";

    private List<Course> courses = new ArrayList<Course>();
    private Map<String, String> semesterMap = new HashMap<String, String>();
    private List<Long> enabledOrgs;
    private Set<Long> processedIdsSet;

    @PostConstruct
    public void init() {
        processedIdsSet = new HashSet<Long>();
        enabledOrgs = daoManager.getIdsOfAllEnabledOrgs();
    }

    @PreDestroy
    public void cleanUp() {
        processedIdsSet.clear();
        enabledOrgs.clear();
    }

    public void setSemesterMap(Map<String, String> semesterMap) {
        this.semesterMap = semesterMap;
        LOG.info("Semester Map: " + semesterMap.toString());
    }

    @Override
    void processEntry(String[] entry) {
        try {
            // Ignore the duplicates
            Long courseId = Long.parseLong(entry[0]);
            if (!CampusUtils.addIfNotAlreadyProcessed(processedIdsSet, courseId)) {
                LOG.debug("Found a duplicate course: [" + courseId + "]");
                skipEntry(entry, Importer.SKIP_REASON_DUPLICATE_ID);
            }
            // Prepare a Course object for persistence
            Course course = new Course();
            DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            course.setId(courseId);
            course.setShortTitle(entry[1]);
            course.setTitle(sanitizeTitle(entry[2]));
            course.setVstNr(entry[3]);
            course.setLanguage(entry[4]);
            course.setCategory(entry[5]);
            course.setIsELearning(entry[6]);
            course.setStartDate(format.parse(entry[7]));
            course.setEndDate(format.parse(entry[8]));
            course.setVvzLink(entry[9]);
            course.setSemester(entry[10]);
            course.setShortSemester(buildShortSemester(entry[10]));
            course.setIpz(entry[11]);
            course.setOrg1(Long.parseLong(entry[12]));
            course.setOrg2(Long.parseLong(entry[13]));
            course.setOrg3(Long.parseLong(entry[14]));
            course.setOrg4(Long.parseLong(entry[15]));
            course.setOrg5(Long.parseLong(entry[16]));
            course.setEnabled(buildIsEnabled(course));
            course.setModifiedDate(new Date());
            courses.add(course);
            if (courses.size() % Importer.COMMIT_INTERVAL == 0) {
                persist();
            }
        } catch(Exception e) {
            System.out.println("Exception while processing Course entry: " + e.getMessage());
            cntFailed++;
        }
    }

    @Override
    void skipEntry(String[] entry, String reason) {
        LOG.info("Skipped entry(" + reason + "):" + String.join(";", entry));
        cntSkipped++;
    }

    @Override
    int getEntryFieldCount() {
        return 21;
    }

    @Override
    void persist() {
        persistList(courses, courseDao);
    }

    private String buildShortSemester(String semester) {
        String shortSemester = null;

        String[] split = StringUtils.split(semester, WHITESPACE);
        if (split != null) {
            String yy = (split[1] != null) ? split[1].substring(2) : "";
            if (split[0] != null) {
                shortSemester = yy.concat(semesterMap.get(split[0].substring(0, 1)));
            }
        }
        return shortSemester;
    }

    private String buildIsEnabled(Course course) {
        if (enabledOrgs == null || enabledOrgs.isEmpty()) {
            return course.getIpz().equals("X") ? "1" : "0";
        } else {
            boolean isEnabled = enabledOrgs.contains(course.getOrg1());
            isEnabled = isEnabled || enabledOrgs.contains(course.getOrg2());
            isEnabled = isEnabled || enabledOrgs.contains(course.getOrg3());
            isEnabled = isEnabled || enabledOrgs.contains(course.getOrg4());
            isEnabled = isEnabled || enabledOrgs.contains(course.getOrg5());
            return isEnabled ? "1" : "0";
        }
    }

    private String sanitizeTitle(String title) {
        if (title.contains(SEMICOLON_REPLACEMENT)) {
            title = StringUtils.replace(title, SEMICOLON_REPLACEMENT, SEMICOLON);
        }

        return title;
    }

}
