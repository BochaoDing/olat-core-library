package ch.uzh.extension.campuscourse.presentation.tab;

import ch.uzh.extension.campuscourse.presentation.CampusCourseControllerFactory;
import ch.uzh.extension.campuscourse.presentation.tab.controller.CampusCourseTabTableController;
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

	private final CampusCourseControllerFactory campusCourseControllerFactory;

	private CampusCourseTabTableController campusCourseTabTableController;

	CampusCourseTab(SiteDefinition siteDefinition,
					CampusCourseControllerFactory campusCourseControllerFactory,
					WindowControl windowControl, UserRequest userRequest) {
		super(siteDefinition);

		this.campusCourseControllerFactory = campusCourseControllerFactory;

		campusCourseTabTableController = campusCourseControllerFactory
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
		return campusCourseTabTableController = campusCourseControllerFactory
				.createCampusCourseTabController(this, windowControl,
						userRequest);
	}
}
