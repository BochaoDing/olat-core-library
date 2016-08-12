package ch.uzh.campus.olat.tab.controller;

import ch.uzh.campus.data.SapOlatUser;
import ch.uzh.campus.olat.CampusCourseBeanFactory;
import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.olat.controller.CampusCourseTableController;
import ch.uzh.campus.olat.dialog.controller.CampusCourseCreateDialogController;
import ch.uzh.campus.olat.tab.CampusCourseTab;
import ch.uzh.campus.service.learn.CampusCourseService;
import ch.uzh.campus.service.learn.SapCampusCourseTo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryTableModel;
import org.olat.util.logging.activity.LoggingResourceable;

import java.util.ArrayList;
import java.util.List;

/**
 * Initial date: 2016-08-05<br />
 * @author sev26 (UZH)
 */
public class CampusCourseTabTableController extends CampusCourseTableController {

	private static TableGuiConfiguration createTableGuiConfiguration() {
		TableGuiConfiguration result = new TableGuiConfiguration();
		result.setPreferencesOffered(true, "CampusCourseCreationTableGuiPrefs");
		return result;
	}

	private static WindowControl getBusinessWindowControl(StateSite stateSite,
														  WindowControl windowControl,
														  UserRequest userRequest) {
		OLATResourceable olatResourceable = OresHelper
				.createOLATResourceableInstance(CampusCourseTab.class, 0L);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(
				LoggingResourceable.wrapBusinessPath(olatResourceable));
		return BusinessControlFactory.getInstance()
				.createBusinessWindowControl(userRequest, olatResourceable,
						stateSite, windowControl, true);
	}

	private final CampusCourseOlatHelper campusCourseOlatHelper;
	private final CampusCourseBeanFactory campusCourseBeanFactory;

	public CampusCourseTabTableController(CampusCourseService campusCourseService,
										  CampusCourseOlatHelper campusCourseOlatHelper,
										  CampusCourseBeanFactory campusCourseBeanFactory,
										  StateSite stateSite,
										  WindowControl windowControl,
										  UserRequest userRequest) {
		super(createTableGuiConfiguration(),
				getBusinessWindowControl(stateSite, windowControl, userRequest),
				userRequest);

		addColumnDescriptor(new DefaultColumnDescriptor(
				"tab.table.action.course",
				RepositoryTableModel.RepoCols.externalId.ordinal(),
				RepositoryTableModel.TABLE_ACTION_SELECT_LINK, getLocale()));

		this.campusCourseOlatHelper = campusCourseOlatHelper;
		this.campusCourseBeanFactory = campusCourseBeanFactory;

		SapOlatUser.SapUserType userType = userRequest.getUserSession().getRoles().isAuthor() ?
				SapOlatUser.SapUserType.LECTURER : SapOlatUser.SapUserType.STUDENT;

		List<SapCampusCourseTo> sapCampusCourseTos = campusCourseService
				.getCoursesWhichCouldBeCreated(userRequest.getIdentity(),
						userType, "");

		List<RepositoryEntry> campusCourseEntries = new ArrayList<>(
				sapCampusCourseTos.size());

		for (SapCampusCourseTo sapCampusCourseTo : sapCampusCourseTos) {
			RepositoryEntry repositoryEntry = CampusCourseOlatHelper
					.getRepositoryEntry(sapCampusCourseTo);
			/**
			 * TODO sev26
			 * Value for the "create" link.
			 * This is a hack but not other possible without rewriting
			 * everything.
			 */
			repositoryEntry.setExternalId(
					getTranslator().translate("tab.table.create.course"));
			campusCourseEntries.add(repositoryEntry);
		}

		getTableDataModel().setObjects(campusCourseEntries);
		modelChanged(true);
	}

	@Override
	public void event(UserRequest userRequest, Component source, Event event) {
		super.event(userRequest, source, event);

		/**
		 * Only react if its an event triggered by the table rows and not by
		 * the surroundings like a "change sorting" event.
		 */
		if ("r".equals(event.getCommand())) {
			RepositoryEntry repositoryEntry = getSelectedEntry(event);

			CampusCourseCreateDialogController controller = campusCourseBeanFactory
					.createCampusCourseCreateDialogController(repositoryEntry.getKey(),
							repositoryEntry.getDisplayname(), getWindowControl(),
							userRequest);
			controller.addControllerListener(this);

			campusCourseOlatHelper.showDialog("campus.course.creation.title",
					controller, userRequest, getWindowControl(), this);
		}
	}
}
