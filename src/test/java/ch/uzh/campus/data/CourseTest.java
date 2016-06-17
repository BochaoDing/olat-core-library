package ch.uzh.campus.data;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author lavinia
 */
public class CourseTest {

    private Course course = new Course();

    @Test
    public void testGetTitleToBeDisplayed() throws Exception {
        course.setTitle("Community Ecology");
        course.setShortSemester("16FS");
        course.setShortTitle("07BKECO339");
        assertEquals("16FS ECO339 Community Ecology", course.getTitleToBeDisplayed("true"));
    }

    @Test
    public void testGetTitleToBeDisplayed_shortTitle_notActivated() throws Exception {
        course.setTitle("Community Ecology");
        course.setShortSemester("16FS");
        course.setShortTitle("07BKECO339");
        assertEquals("16FS Community Ecology", course.getTitleToBeDisplayed("false"));
    }

    @Test
    public void testGetTitleToBeDisplayed_shortTitle_notActivated_missingProperty() throws Exception {
        course.setTitle("Community Ecology");
        course.setShortSemester("16FS");
        course.setShortTitle("07BKECO339");
        assertEquals("16FS Community Ecology", course.getTitleToBeDisplayed(null));
    }

    @Test
    public void testGetTitleToBeDisplayed_shortTitle_notActivated_emptyProperty() throws Exception {
        Course course = new Course();
        course.setTitle("Community Ecology");
        course.setShortSemester("16FS");
        course.setShortTitle("07BKECO339");
        assertEquals("16FS Community Ecology", course.getTitleToBeDisplayed(""));
    }

    @Test
    public void testGetTitleToBeDisplayed_semesterIsNull_shortTitleIsNull() throws Exception {
        course.setTitle("Community Ecology");
        course.setShortSemester(null);
        course.setShortTitle(null);
        assertEquals("Community Ecology", course.getTitleToBeDisplayed("true"));
    }
}

