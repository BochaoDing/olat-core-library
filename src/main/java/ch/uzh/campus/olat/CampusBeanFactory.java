package ch.uzh.campus.olat;

import ch.uzh.campus.olat.controller.ContinueCampusCourseSelectionController;
import ch.uzh.campus.olat.controller.CreationCampusCourseSelectionController;
import ch.uzh.campus.service.learn.CampusCourseService;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.ui.list.RepositoryEntryListController;
import org.olat.repository.ui.list.RepositoryEntryRow;
import org.olat.repository.ui.list.RepositoryEntryRowFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Configuration
public class CampusBeanFactory {

	final static String RESOURCEABLE_TYPE_NAME = "Campuskurs";
	final static Long NOT_CREATED_CAMPUSKURS_KEY = 0L;
	final static Long NOT_CREATED_CAMPUSKURS_RESOURCE_ID = 0L;

	@Autowired
	private RepositoryModule repositoryModule;

	@Autowired
	private MapperService mapperService;

	@Autowired
	private CampusCourseService campusCourseService;

	@Autowired
	private RepositoryManager repositoryManager;

	@Autowired
	private CampusCourseOlatHelper campusCourseOlatHelper;

	@Bean(name={"row_1"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@Primary
	protected VelocityContainer createRow1(RepositoryEntryListController caller) {
		VelocityContainer result = new VelocityContainer(null,
				"vc_" + "row_1",
				Util.getPackageVelocityRoot(CampusBeanFactory.class) + "/row_1.html",
				CampusCourseOlatHelper.getTranslator(caller.getLocale()),
				caller
		);
		result.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		return result;
	}

	@Bean(name={"RepositoryEntryRowFactory"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@Primary
	protected RepositoryEntryRowFactory createRepositoryEntryRowFactory(UserRequest userRequest) {
		return new RepositoryEntryRowFactory(repositoryModule, mapperService, userRequest) {
			@Override
			public RepositoryEntryRow create(RepositoryEntryMyView entry) {
				if (entry.getOlatResource().getResourceableId() == NOT_CREATED_CAMPUSKURS_RESOURCE_ID)
					return new CampusRepositoryEntryRow(entry);
				else
					return super.create(entry);
			};
		};
	}

	CreationCampusCourseSelectionController createCreationCampusCourseSelectionTableController(
			WindowControl windowControl,
			UserRequest userRequest,
			Long sapCampusCourseId
	) {
		return new CreationCampusCourseSelectionController(
				sapCampusCourseId,
				campusCourseService,
				repositoryManager,
				campusCourseOlatHelper,
				windowControl,
				userRequest
		);
	}


	ContinueCampusCourseSelectionController createContinueCampusCourseSelectionTableController(
			String campusCourseTitle,
			WindowControl windowControl,
			UserRequest userRequest,
			Long sapCampusCourseId
	) {
		return new ContinueCampusCourseSelectionController(
				campusCourseTitle,
				sapCampusCourseId,
				campusCourseService,
				repositoryManager,
				campusCourseOlatHelper,
				windowControl,
				userRequest
		);
	}
}
