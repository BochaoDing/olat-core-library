package ch.uzh.extension.campuscourse.data.entity;

import ch.uzh.extension.campuscourse.model.SemesterName;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author Martin Schraner
 */
public class CourseTest {

    @Test
    public void testGetShortSemesterLvKuerzelTitle() {
        Course course = new Course();
        course.setTitle("Community Ecology");
        Semester semester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        course.setSemester(semester);
        course.setLvKuerzel("07BKECO339");
        assertEquals("16FS ECO339 Community Ecology", course.getTitleToBeDisplayed());

        // Empty lvKuerzel
        course.setLvKuerzel("");
        assertEquals("16FS Community Ecology", course.getTitleToBeDisplayed());
    }

    @Test
    public void testGetShortSemesterLvKuerzelTitle_continuedCampusCourse_twoSemesters() {
        Course parentCourse = new Course();
        Semester parentSemester = new Semester(SemesterName.HERBSTSEMESTER, 2015, false);
        parentCourse.setSemester(parentSemester);

        Course childCourse = new Course();
        childCourse.setTitle("Community Ecology");
        Semester childSemester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        childCourse.setSemester(childSemester);
        childCourse.setLvKuerzel("07BKECO339");
        childCourse.setParentCourse(parentCourse);

        assertEquals("16FS/15HS ECO339 Community Ecology", childCourse.getTitleToBeDisplayed());

        // Empty lvKuerzel
        childCourse.setLvKuerzel("");
        assertEquals("16FS/15HS Community Ecology", childCourse.getTitleToBeDisplayed());
    }

    @Test
    public void testGetShortSemesterLvKuerzelTitle_continuedCampusCourse_threeSemesters() {
        Course parentParentCourse = new Course();
        Semester parentParentSemester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2015, false);
        parentParentCourse.setSemester(parentParentSemester);

        Course parentCourse = new Course();
        Semester parentSemester = new Semester(SemesterName.HERBSTSEMESTER, 2015, false);
        parentCourse.setSemester(parentSemester);
        parentCourse.setParentCourse(parentParentCourse);

        Course childCourse = new Course();
        childCourse.setTitle("Community Ecology");
        Semester childSemester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        childCourse.setSemester(childSemester);
        childCourse.setLvKuerzel("07BKECO339");
        childCourse.setParentCourse(parentCourse);

        assertEquals("16FS/15HS/15FS ECO339 Community Ecology", childCourse.getTitleToBeDisplayed());

        // Empty lvKuerzel
        childCourse.setLvKuerzel("");
        assertEquals("16FS/15HS/15FS Community Ecology", childCourse.getTitleToBeDisplayed());
    }

    @Test
    public void testGetTitlesOfCourseAndParentCoursesInAscendingOrder() {
        Course parentParentCourse = new Course();
        Semester parentParentSemester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2015, false);
        parentParentCourse.setSemester(parentParentSemester);
        parentParentCourse.setLvKuerzel("07BKECO119");
        parentParentCourse.setTitle("Community Ecology I");

        Course parentCourse = new Course();
        Semester parentSemester = new Semester(SemesterName.HERBSTSEMESTER, 2015, false);
        parentCourse.setSemester(parentSemester);
        parentCourse.setLvKuerzel("07BKECO229");
        parentCourse.setTitle("Community Ecology II");
        parentCourse.setParentCourse(parentParentCourse);

        Course childCourse = new Course();
        Semester childSemester = new Semester(SemesterName.FRUEHJAHRSSEMESTER, 2016, false);
        childCourse.setSemester(childSemester);
        childCourse.setLvKuerzel("07BKECO339");
        childCourse.setTitle("Community Ecology III");
        childCourse.setParentCourse(parentCourse);

        List<String> titlesOfCourseAndParentCoursesInAscendingOrder = childCourse.getTitlesOfCourseAndParentCoursesInAscendingOrder();
        assertNotNull(titlesOfCourseAndParentCoursesInAscendingOrder);
        assertEquals(3, titlesOfCourseAndParentCoursesInAscendingOrder.size());
        assertEquals("15FS ECO119 Community Ecology I", titlesOfCourseAndParentCoursesInAscendingOrder.get(0));
        assertEquals("15HS ECO229 Community Ecology II", titlesOfCourseAndParentCoursesInAscendingOrder.get(1));
        assertEquals("16FS ECO339 Community Ecology III", titlesOfCourseAndParentCoursesInAscendingOrder.get(2));
    }
}
