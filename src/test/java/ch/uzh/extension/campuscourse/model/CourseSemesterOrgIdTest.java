package ch.uzh.extension.campuscourse.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Martin Schraner
 */
public class CourseSemesterOrgIdTest {

    @Test
    public void getSemesterName() throws Exception {
        CourseSemesterOrgId courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("Frühjahrssemester 2015");
        Assert.assertEquals(SemesterName.FRUEHJAHRSSEMESTER, courseSemesterOrgId.getSemesterName());

        courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("Herbstsemester 2015");
        Assert.assertEquals(SemesterName.HERBSTSEMESTER, courseSemesterOrgId.getSemesterName());

        courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("Foo 2015");
        Assert.assertNull(courseSemesterOrgId.getSemesterName());
    }

    @Test
    public void getSemesterYear() throws Exception {
        CourseSemesterOrgId courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("Herbstsemester 2015");
        Assert.assertEquals(2015, courseSemesterOrgId.getSemesterYear().intValue());

        courseSemesterOrgId = new CourseSemesterOrgId();
        courseSemesterOrgId.setSemester("2015");
        Assert.assertNull(courseSemesterOrgId.getSemesterYear());
    }

}