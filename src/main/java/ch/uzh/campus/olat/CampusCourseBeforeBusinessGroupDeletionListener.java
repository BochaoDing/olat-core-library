package ch.uzh.campus.olat;

import ch.uzh.campus.service.core.CampusCourseCoreService;
import org.olat.group.BusinessGroup;
import org.olat.repository.listener.BeforeBusinessGroupDeletionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial date: 2016-11-02<br />
 * @author Martin Schraner
 */
@Component
public class CampusCourseBeforeBusinessGroupDeletionListener extends BeforeBusinessGroupDeletionListener {

	private final CampusCourseCoreService campusCourseCoreService;

	@Autowired
	public CampusCourseBeforeBusinessGroupDeletionListener(CampusCourseCoreService campusCourseCoreService) {
		this.campusCourseCoreService = campusCourseCoreService;

	}

	@Override
	public void onAction(BusinessGroup businessGroup) {
		campusCourseCoreService.resetCampusGroup(businessGroup);
	}
}
