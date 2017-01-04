package ch.uzh.extension.campuscourse.olat.tab;

import ch.uzh.extension.campuscourse.olat.CampusOlatControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2016-08-04<br />
 * @author sev26 (UZH)
 */
public class CampusCourseTabDefinition extends AbstractSiteDefinition implements SiteDefinition {

	@Autowired
	private CampusOlatControllerFactory campusCoursecampusOlatControllerFactory;

	@Override
	public SiteInstance createSite(UserRequest userRequest,
								   WindowControl windowControl,
								   SiteConfiguration siteConfiguration) {
		/*
		 * Not created by a factory because this class acts like a factory.
		 */
		return new CampusCourseTab(this, campusCoursecampusOlatControllerFactory,
				windowControl, userRequest);
	}
}
