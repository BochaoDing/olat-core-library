package ch.uzh.extension.campuscourse.presentation;

import ch.uzh.extension.campuscourse.presentation.list.CampusCourseRepositoryEntryRow;
import ch.uzh.extension.campuscourse.presentation.tab.CampusCourseTabDefinition;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.list.RepositoryEntryListController;
import org.olat.repository.ui.list.RepositoryEntryRow;
import org.olat.repository.ui.list.RepositoryEntryRowFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

import static ch.uzh.extension.campuscourse.presentation.CampusOlatConfig.NOT_CREATED_CAMPUS_COURSE_RESOURCE_ID;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Configuration
public class CampusOlatBeanFactory {

	@Autowired
	private RepositoryModule repositoryModule;

	@Autowired
	private MapperService mapperService;

	@Autowired
	private RepositoryManager repositoryManager;

	@Bean
	public CampusCourseTabDefinition campusCourseTabDefinition() {
		CampusCourseTabDefinition campusCourseTabDefinition = new CampusCourseTabDefinition();
		campusCourseTabDefinition.setOrder(15);
		return campusCourseTabDefinition;
	}

	@Bean(initMethod="initExtensionPoints")
	public GenericActionExtension genericActionExtension() {

		AutoCreator autoCreator = new AutoCreator();
		autoCreator.setClassName("ch.uzh.extension.campuscourse.olat.admin.CampusAdminController");

		List<String> extensionPoints = new ArrayList<>();
		extensionPoints.add("org.olat.admin.SystemAdminMainController");

		GenericActionExtension genericActionExtension = new GenericActionExtension();
		genericActionExtension.setActionController(autoCreator);
		genericActionExtension.setNavigationKey("ck");
		genericActionExtension.setI18nActionKey("menu.campus");
		genericActionExtension.setI18nDescriptionKey("menu.campus");
		genericActionExtension.setTranslationPackage("ch.uzh.extension.campuscourse.olat.admin");
		genericActionExtension.setExtensionPoints(extensionPoints);
		genericActionExtension.setParentTreeNodeIdentifier("modulesParent");
		return genericActionExtension;
	}

	@Bean(name={"row_1"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@Primary
	protected VelocityContainer row1(RepositoryEntryListController caller) {
		VelocityContainer velocityContainer = new VelocityContainer(null,
				"vc_" + "row_1",
				Util.getPackageVelocityRoot(CampusCourseRepositoryEntryRow.class) + "/row_1.html",
				CampusCourseOlatHelper.getTranslator(caller.getLocale(),
						RepositoryService.class),
				caller
		);
		velocityContainer.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		return velocityContainer;
	}

	@Bean(name={"RepositoryEntryRowFactory"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@Primary
	protected RepositoryEntryRowFactory repositoryEntryRowFactory(UserRequest userRequest) {
		return new RepositoryEntryRowFactory(repositoryManager,
				repositoryModule, mapperService, userRequest) {

			Translator translator = CampusCourseOlatHelper
					.getTranslator(userRequest.getLocale());

			@Override
			public RepositoryEntryRow create(RepositoryEntryMyView entry) {
				if (entry.getOlatResource().getResourceableId() == NOT_CREATED_CAMPUS_COURSE_RESOURCE_ID)
					return new CampusCourseRepositoryEntryRow(entry, translator);
				else
					return super.create(entry);
			}
		};
	}
}
