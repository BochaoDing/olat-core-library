package ch.uzh.campus.olat;

import ch.uzh.campus.olat.dialog.controller.CampusCourseCreateDialogController;
import ch.uzh.campus.olat.dialog.controller.CampusCourseCreationChoiceController;
import ch.uzh.campus.olat.dialog.controller.CampusCourseCreationChoiceController.CampusCourseCreationChoiceControllerListener;
import ch.uzh.campus.olat.dialog.controller.CreateCampusCourseCompletedEventListener;
import ch.uzh.campus.olat.dialog.controller.selection.CampusCourseSubmitController;
import ch.uzh.campus.olat.dialog.controller.selection.ContinueCampusCourseSelectionController;
import ch.uzh.campus.olat.dialog.controller.selection.CreationCampusCourseSelectionController;
import ch.uzh.campus.olat.list.CampusCourseRepositoryEntryRow;
import ch.uzh.campus.olat.tab.controller.CampusCourseTabTableController;
import ch.uzh.campus.service.CampusCourseService;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.StateSite;
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

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Configuration
public class CampusCourseBeanFactory {

	final static String RESOURCEABLE_TYPE_NAME = "CampusCourse";
	final static String STUDENT_RESOURCEABLE_TYPE_NAME = "Student" + RESOURCEABLE_TYPE_NAME;
	public final static String LECTURER_RESOURCEABLE_TYPE_NAME = "Lecturer"  + RESOURCEABLE_TYPE_NAME;
	public final static String AUTHOR_LECTURER_RESOURCEABLE_TYPE_NAME = "AuthorLecturer"  + RESOURCEABLE_TYPE_NAME;
	final static Long NOT_CREATED_CAMPUS_COURSE_KEY = 0L;
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
				Util.getPackageVelocityRoot(CampusCourseRepositoryEntryRow.class) + "/row_1.html",
				CampusCourseOlatHelper.getTranslator(caller.getLocale(),
						RepositoryService.class),
				caller
		);
		result.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		return result;
	}

	@Bean(name={"RepositoryEntryRowFactory"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@Primary
	protected RepositoryEntryRowFactory createRepositoryEntryRowFactory(UserRequest userRequest) {
		return new RepositoryEntryRowFactory(repositoryManager,
				repositoryModule, mapperService, userRequest) {

			Translator translator = CampusCourseOlatHelper
					.getTranslator(userRequest.getLocale());

			@Override
			public RepositoryEntryRow create(RepositoryEntryMyView entry) {
				if (entry.getOlatResource().getResourceableId() == NOT_CREATED_CAMPUSKURS_RESOURCE_ID)
					return new CampusCourseRepositoryEntryRow(entry, translator);
				else
					return super.create(entry);
			}
		};
	}

	public CampusCourseCreateDialogController createCampusCourseCreateDialogController(
			Long sapCampusCourseId,
			WindowControl windowControl,
			UserRequest userRequest
	) {
		return new CampusCourseCreateDialogController(sapCampusCourseId,
				this, windowControl, userRequest);
	}

	public CampusCourseCreationChoiceController createCampusCourseCreationChoiceController(
			CampusCourseCreationChoiceControllerListener listener,
			WindowControl windowControl,
			UserRequest userRequest
	) {
		return new CampusCourseCreationChoiceController(listener,
				windowControl, userRequest);
	}

	public CampusCourseSubmitController createCampusCourseSubmitController(
			Long sapCampusCourseId,
			CreateCampusCourseCompletedEventListener listener,
			WindowControl windowControl, UserRequest userRequest
	) {
		return new CampusCourseSubmitController(sapCampusCourseId,
				campusCourseService, campusCourseOlatHelper, listener,
				windowControl, userRequest);
	}

	public CreationCampusCourseSelectionController createCreationCampusCourseSelectionTableController(
			Long sapCampusCourseId, CreateCampusCourseCompletedEventListener listener,
			WindowControl windowControl, UserRequest userRequest
	) {
		return new CreationCampusCourseSelectionController(
				sapCampusCourseId,
				campusCourseService,
				repositoryManager,
				listener,
				windowControl,
				userRequest
		);
	}

	public ContinueCampusCourseSelectionController createContinueCampusCourseSelectionTableController(
			Long sapCampusCourseId,
			CreateCampusCourseCompletedEventListener listener,
			WindowControl windowControl, UserRequest userRequest
	) {
		return new ContinueCampusCourseSelectionController(
				sapCampusCourseId,
				campusCourseService,
				repositoryManager,
				listener,
				windowControl,
				userRequest
		);
	}

	public CampusCourseTabTableController createCampusCourseTabController(
			SiteInstance parent,
			WindowControl windowControl,
			UserRequest userRequest
	) {
		return new CampusCourseTabTableController(
				campusCourseService,
				campusCourseOlatHelper,
				this,
				new StateSite(parent),
				windowControl,
				userRequest
		);
	}
}
