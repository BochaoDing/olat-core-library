package ch.uzh.campus.olat;

import ch.uzh.campus.olat.popup.CampusCourseCreationController;
import ch.uzh.campus.service.core.CampusCourseCoreService;
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
	private CampusCourseCoreService campusCourseCoreService;

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
				Util.createPackageTranslator(caller.getClass(), caller.getLocale()),
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

	CampusCourseCreationController createCampusCourseCreationController(
			String campuskursTitle,
			WindowControl windowControl,
			UserRequest userRequest,
			String variation,
			Long sapCampusCourseId
	) {
		return new CampusCourseCreationController(
				windowControl,
				userRequest,
				variation,
				sapCampusCourseId,
				campuskursTitle,
				campusCourseCoreService,
				repositoryManager,
				campusCourseOlatHelper
		);
	}
}
