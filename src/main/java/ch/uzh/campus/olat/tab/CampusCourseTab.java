package ch.uzh.campus.olat.tab;

import ch.uzh.campus.olat.CampusCourseBeanFactory;
import ch.uzh.campus.olat.tab.controller.CampusCourseTabTableController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;

/**
 * Initial date: 2016-08-04<br />
 * @author sev26 (UZH)
 */
public class CampusCourseTab extends AbstractSiteInstance {

	private final CampusCourseTabTableController campusCourseTabTableController;

	CampusCourseTab(SiteDefinition siteDefinition,
					CampusCourseBeanFactory campusCourseBeanFactory,
					WindowControl windowControl, UserRequest userRequest) {
		super(siteDefinition);

		campusCourseTabTableController = campusCourseBeanFactory
				.createCampusCourseTabController(this, windowControl,
						userRequest);
	}

	@Override
	public NavElement getNavElement() {
		return campusCourseTabTableController.getNavElement();
	}

	@Override
	protected Controller createController(UserRequest userRequest,
										  WindowControl windowControl,
										  SiteConfiguration siteConfiguration) {
		return campusCourseTabTableController;
	}
}
