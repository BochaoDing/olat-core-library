package ch.uzh.campus.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author lavinia
 */
public class CourseTest {

    private Course course = new Course();

    @Test
    public void testGetTitleToBeDisplayed() throws Exception {
        course.setTitle("Community Ecology");
        Semester semester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        course.setSemester(semester);
        course.setShortTitle("07BKECO339");
        assertEquals("16FS ECO339 Community Ecology", course.getTitleToBeDisplayed(true));
    }

    @Test
    public void testGetTitleToBeDisplayed_shortTitle_notActivated() throws Exception {
        course.setTitle("Community Ecology");
        Semester semester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        course.setSemester(semester);
        course.setShortTitle("07BKECO339");
        assertEquals("16FS Community Ecology", course.getTitleToBeDisplayed(false));
    }

    @Test
    public void testGetTitleToBeDisplayed_semesterIsNull_shortTitleIsNull() throws Exception {
        course.setTitle("Community Ecology");
        course.setSemester(null);
        course.setShortTitle(null);
        assertEquals("Community Ecology", course.getTitleToBeDisplayed(true));
    }

    @Test
    public void testGetTitleToBeDisplayed_semesterIsNull_shortTitleIsEmptyl() throws Exception {
        course.setTitle("Community Ecology");
        course.setSemester(null);
        course.setShortTitle("");
        assertEquals("Community Ecology", course.getTitleToBeDisplayed(true));
    }
}
