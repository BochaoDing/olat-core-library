package ch.uzh.extension.campuscourse.model;

/**
 * Initial Date: 06.06.2012 <br>
 * 
 * @author cg
 */
public final class CampusCourseTOForUI implements Comparable<CampusCourseTOForUI> {

    private final String title;
    private final Long sapCourseId;

    public CampusCourseTOForUI(String title, Long sapCourseId) {
        this.title = title;
        this.sapCourseId = sapCourseId;
    }

    public String getTitle() {
        return title;
    }

    public Long getSapCourseId() {
        return sapCourseId;
    }

    @Override
    public int compareTo(CampusCourseTOForUI otherCampusCourseTOForUI) {
        return title.compareTo(otherCampusCourseTOForUI.title);
    }
}
