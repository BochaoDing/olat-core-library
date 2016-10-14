package ch.uzh.campus.data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Martin Schraner
 */
public class CourseSemesterOrgIdTest {

    @Test
    public void getSemesterName() throws Exception {
        CourseSemesterOrgId courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("Frühjahrssemester 2015");
        assertEquals(SemesterName.FRUEHJAHRSSEMESTER, courseSemesterOrgId.getSemesterName());

        courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("Herbstsemester 2015");
        assertEquals(SemesterName.HERBSTSEMESTER, courseSemesterOrgId.getSemesterName());

        courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("Foo 2015");
        assertNull(courseSemesterOrgId.getSemesterName());
    }

    @Test
    public void getSemesterYear() throws Exception {
        CourseSemesterOrgId courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("Herbstsemester 2015");
        assertEquals(2015, courseSemesterOrgId.getSemesterYear().intValue());

        courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("2015");
        assertNull(courseSemesterOrgId.getSemesterYear());
    }

}