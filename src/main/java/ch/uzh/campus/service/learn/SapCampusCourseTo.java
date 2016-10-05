package ch.uzh.campus.service.learn;

/**
 * Initial Date: 06.06.2012 <br>
 * 
 * @author cg
 */
public final class SapCampusCourseTo implements Comparable<SapCampusCourseTo> {

    private String title;
    private boolean activated;
    private final Long sapCourseId;

    public SapCampusCourseTo(String title, Long sapCourseId) {
        this.title = title;
        this.sapCourseId = sapCourseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getSapCourseId() {
        return sapCourseId;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @Override
    public int compareTo(SapCampusCourseTo compareSapCampusCourseTo) {
        String compareTitle = compareSapCampusCourseTo.getTitle();
        // ASCENDING ORDER
        return this.getTitle().compareTo(compareTitle);
    }
}
