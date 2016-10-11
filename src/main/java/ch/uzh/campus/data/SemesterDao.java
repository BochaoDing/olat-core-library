package ch.uzh.campus.data;

import ch.uzh.campus.CampusCourseConfiguration;
import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Initial Date: 07.12.2012 <br>
 *
 * @author aabouc
 * @author Martin Schraner
 */
@Repository
public class SemesterDao {

    private final DB dbInstance;
    private final CampusCourseConfiguration campusCourseConfiguration;

    @Autowired
    public SemesterDao(DB dbInstance, CampusCourseConfiguration campusCourseConfiguration) {
        this.dbInstance = dbInstance;
        this.campusCourseConfiguration = campusCourseConfiguration;
    }

    public void save(Semester semester) {
        dbInstance.saveObject(semester);
    }

    Semester getSemesterById(Long id) {
        return dbInstance.findObject(Semester.class, id);
    }

    Semester getSemesterBySemesterNameAndYear(SemesterName semesterName, Integer year) {
        List<Semester> semesters = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Semester.GET_SEMESTER_BY_SEMESTER_NAME_AND_YEAR, Semester.class)
                .setParameter("semesterName", semesterName)
                .setParameter("year", year)
                .getResultList();
        if (semesters.isEmpty()) {
            return null;
        }
        return semesters.get(0);
    }

    List<Long> getPreviousSemestersNotTooFarInThePastInDescendingOrder() {
        List<Semester> semesters = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Semester.GET_ALL_SEMESTERS, Semester.class)
                .getResultList();

        // Sort in reverse order, i.e. list starts with newest semester
        Collections.sort(semesters, Collections.reverseOrder());

        // Skip all semesters that are newer than the current semester (normally we shouldn't find anything here)
        int ind = 0;
        while (ind < semesters.size() && !semesters.get(ind).isCurrentSemester()) {
            ind++;
        }

        List<Long> idsOfSemestersOlderThanCurrentSemester = new ArrayList<>();
        for (int i = ind + 1; i < semesters.size(); i++) {
            idsOfSemestersOlderThanCurrentSemester.add(semesters.get(i).getId());
        }

        int indexOfOldestSemesterNotTooFarInThePast = campusCourseConfiguration.getMaxYearsToKeepCkData() * 2;
        if (idsOfSemestersOlderThanCurrentSemester.isEmpty() || indexOfOldestSemesterNotTooFarInThePast < 1) {
            return new ArrayList<>();
        }

        return idsOfSemestersOlderThanCurrentSemester.subList(0, Math.min(indexOfOldestSemesterNotTooFarInThePast, idsOfSemestersOlderThanCurrentSemester.size()));
    }

    void setCurrentSemester(Long id) {
        Semester semester = getSemesterById(id);
        semester.setCurrentSemester(true);
    }

    void unsetCurrentSemester() {
        List<Semester> semestersFound = dbInstance.getCurrentEntityManager()
                .createNamedQuery(Semester.GET_CURRENT_SEMESTER, Semester.class)
                .getResultList();
        for (Semester semester : semestersFound) {
            semester.setCurrentSemester(false);
        }
    }

}

