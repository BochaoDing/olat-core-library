package ch.uzh.campus.data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Martin Schraner
 */
public class SemesterTest {

    @Test
    public void getSemesterNameYear() throws Exception {

        Semester semester = new Semester(SemesterName.HERBSTSEMESTER, 2016, false);
        assertEquals("Herbstsemester 2016", semester.getSemesterNameYear());

        semester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        assertEquals("Frühjahrssemester 2016", semester.getSemesterNameYear());
    }

    @Test
    public void getShortYearShortSemesterName() throws Exception {
        
        Semester semester = new Semester(SemesterName.HERBSTSEMESTER, 2016, false);
        assertEquals("16HS", semester.getShortYearShortSemesterName());
        
        semester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        assertEquals("16FS", semester.getShortYearShortSemesterName());
    }

    @Test
    public void getSequenceWithinYear() throws Exception {
        
        Semester semester = new Semester(SemesterName.HERBSTSEMESTER, 2016, false);
        assertEquals(2, semester.getSequenceWithinYear().intValue());
        
        semester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        assertEquals(1, semester.getSequenceWithinYear().intValue());
    }

}