package ch.uzh.campus.service.core.impl;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Martin Schraner
 */
public class CampusCourseToolTest {

    @Test
    public void getShortSemestersOfDisplayname() {
        assertEquals("16HS", CampusCourseTool.getShortSemestersOfDisplayname("16HS Campus Course"));
        assertEquals("16HS/16FS", CampusCourseTool.getShortSemestersOfDisplayname("16HS/16FS Continued Campus Course"));
    }

    @Test
    public void getTruncatedDisplaynameWithoutShortSemesters() {
        assertEquals("Campus Course", CampusCourseTool.getTruncatedDisplaynameWithoutShortSemesters("16HS Campus Course"));
        assertEquals("Continued Campus Course", CampusCourseTool.getTruncatedDisplaynameWithoutShortSemesters("16HS/16FS Continued Campus Course"));
    }

}