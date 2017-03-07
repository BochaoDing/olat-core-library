package ch.uzh.extension.campuscourse.presentation;

import ch.uzh.extension.campuscourse.presentation.admin.DelegationController;
import ch.uzh.extension.campuscourse.presentation.mycourses.CampusCourseRepositoryEntryRow;
import ch.uzh.extension.campuscourse.presentation.tab.CampusCourseTabDefinition;
import ch.uzh.extension.campuscourse.presentation.undocoursecontinuation.ContinuedCampusCourseRepositoryEntryLifeCycleChangeController;
import ch.uzh.extension.campuscourse.service.CampusCourseService;
import org.olat.admin.user.UserAdminControllerAdditionalTabs;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tabbedpane.Tab;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.repository.*;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryLifeCycleChangeController;
import org.olat.repository.ui.RepositoryEntryLifeCycleChangeControllerFactory;
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
import java.util.Objects;

import static ch.uzh.extension.campuscourse.common.CampusCourseConfiguration.NOT_CREATED_CAMPUS_COURSE_RESOURCE_ID;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Configuration
public class CampusCoursePresentationBeanFactory {

	@Autowired
	private RepositoryModule repositoryModule;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private MapperService mapperService;

	@Autowired
	private RepositoryManager repositoryManager;

	@Autowired
	private CampusCourseService campusCourseService;

	@Bean
	public CampusCourseTabDefinition campusCourseTabDefinition() {
		CampusCourseTabDefinition campusCourseTabDefinition = new CampusCourseTabDefinition();
		campusCourseTabDefinition.setOrder(15);
		return campusCourseTabDefinition;
	}

	@Bean(initMethod="initExtensionPoints")
	public GenericActionExtension genericActionExtension() {

		AutoCreator autoCreator = new AutoCreator();
		autoCreator.setClassName("ch.uzh.extension.campuscourse.presentation.admin.CampusAdminController");

		List<String> extensionPoints = new ArrayList<>();
		extensionPoints.add("org.olat.admin.SystemAdminMainController");

		GenericActionExtension genericActionExtension = new GenericActionExtension();
		genericActionExtension.setActionController(autoCreator);
		genericActionExtension.setNavigationKey("ck");
		genericActionExtension.setI18nActionKey("menu.campus");
		genericActionExtension.setI18nDescriptionKey("menu.campus");
		genericActionExtension.setTranslationPackage("ch.uzh.extension.campuscourse.presentation.admin");
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
				CampusCoursePresentationHelper.getTranslator(caller.getLocale(), RepositoryService.class),
				caller
		);
		velocityContainer.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		return velocityContainer;
	}

	@Bean(name={"repositoryEntryRowFactory"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@Primary
	protected RepositoryEntryRowFactory repositoryEntryRowFactory(UserRequest userRequest) {
		return new RepositoryEntryRowFactory(repositoryManager, repositoryModule, mapperService, userRequest) {

			Translator translator = CampusCoursePresentationHelper.getTranslator(userRequest.getLocale());

			@Override
			public RepositoryEntryRow create(RepositoryEntryMyView entry) {
				if (Objects.equals(entry.getOlatResource().getResourceableId(), NOT_CREATED_CAMPUS_COURSE_RESOURCE_ID))
					return new CampusCourseRepositoryEntryRow(entry, translator);
				else
					return super.create(entry);
			}
		};
	}

	@Bean(name={"repositoryEntryLifeCycleChangeControllerFactory"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@Primary
	protected RepositoryEntryLifeCycleChangeControllerFactory repositoryEntryLifeCycleChangeControllerFactory(UserRequest userRequest,
																											  WindowControl windowControl,
																											  RepositoryEntrySecurity repositoryEntrySecurity,
																											  RepositoryHandler repositoryHandler) {
		return new RepositoryEntryLifeCycleChangeControllerFactory(repositoryService, repositoryManager, userRequest, windowControl, repositoryEntrySecurity, repositoryHandler) {

			@Override
			public RepositoryEntryLifeCycleChangeController create(RepositoryEntry repositoryEntry) {
				if (campusCourseService.isContinuedCourse(repositoryEntry)) {
					return new ContinuedCampusCourseRepositoryEntryLifeCycleChangeController(repositoryService,
							repositoryManager,
							campusCourseService,
							userRequest,
							windowControl,
							repositoryEntry,
							repositoryEntrySecurity,
							repositoryHandler,
							CampusCoursePresentationHelper.getTranslator(userRequest.getLocale(), RepositoryService.class));
				} else {
					return super.create(repositoryEntry);
				}
			}

		};
	}

	@Bean(name={"userAdminControllerAdditionalTabs"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@Primary
	public UserAdminControllerAdditionalTabs userAdminControllerAdditionalTabs(UserRequest userRequest,
																			   WindowControl windowControl,
																			   Identity identity) {
		List<Tab> tabs = new ArrayList<>();
		DelegationController delegationController = new DelegationController(campusCourseService,
				userRequest,
				windowControl,
				identity);
		Translator translator = CampusCoursePresentationHelper.getTranslator(userRequest.getLocale(), DelegationController.class);
		tabs.add(new Tab(translator.translate("edit.delegation"),
				delegationController.getInitialComponent(),
				101));
		return new UserAdminControllerAdditionalTabs(tabs);
	}
}
