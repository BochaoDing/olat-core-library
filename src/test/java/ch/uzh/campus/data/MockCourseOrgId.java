package ch.uzh.campus.data;

/**
 * Initial date: 2016-08-11<br />
 * @author sev26 (UZH)
 */
public class MockCourseOrgId extends CourseOrgId {

	private Long resourceableId;

    public Long getResourceableId() {
        return resourceableId;
    }

    public void setResourceableId(Long resourceableId) {
        this.resourceableId = resourceableId;
    }

	@Override
	public void merge(Course course) {
		super.merge(course);
		course.setResourceableId(getResourceableId());
	}
}
