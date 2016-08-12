package ch.uzh.campus.olat.tab;

import ch.uzh.campus.olat.CampusCourseBeanFactory;
import ch.uzh.campus.olat.CampusCourseOlatHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.*;
import org.olat.core.gui.translator.Translator;

import java.util.Locale;

/**
 * Initial date: 2016-08-04<br />
 * @author sev26 (UZH)
 */
public class CampusCourseTab extends AbstractSiteInstance {

	private final CampusCourseBeanFactory campusCourseBeanFactory;
	private final NavElement origNavElem;
	private final NavElement currentNavElement;

	CampusCourseTab(SiteDefinition siteDefinition,
					CampusCourseBeanFactory campusCourseBeanFactory,
					Locale locale) {
		super(siteDefinition);

		this.campusCourseBeanFactory = campusCourseBeanFactory;
		Translator translator = CampusCourseOlatHelper.getTranslator(locale);
		origNavElem = new DefaultNavElement(translator.translate("topnav.campuscourses"),
				translator.translate("topnav.campuscourses.alt"), "o_site_campuscourse");
		origNavElem.setAccessKey("r".charAt(0));
		currentNavElement = new DefaultNavElement(origNavElem);
	}

	@Override
	public NavElement getNavElement() {
		return currentNavElement;
	}

	@Override
	protected Controller createController(UserRequest userRequest,
										  WindowControl windowControl,
										  SiteConfiguration siteConfiguration) {
		return campusCourseBeanFactory.createCampusCourseTabController(this,
				windowControl, userRequest);
	}
}
