package ch.uzh.extension.campuscourse.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Martin Schraner
 */
public class SemesterNameTest {

    @Test
    public void findByName() throws Exception {
        assertEquals(SemesterName.FRUEHJAHRSSEMESTER, SemesterName.findByName("Frühjahrssemester"));
        assertEquals(SemesterName.HERBSTSEMESTER, SemesterName.findByName("Herbstsemester"));
        assertNull(SemesterName.findByName("foo"));
    }

}