/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package ch.uzh.campus.olat.popup;

import ch.uzh.campus.olat.CampusCourseOlatHelper;
import ch.uzh.campus.service.core.CampusCourseCoreService;
import org.apache.commons.lang.StringUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.*;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.RepositoryTableModel;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

import java.util.ArrayList;
import java.util.List;

import static org.olat.admin.sysinfo.HibernateQueriesController.QueryCols.row;
import static org.olat.core.gui.control.generic.closablewrapper.CloseableModalController.CLOSE_MODAL_EVENT;

/**
 * Initial Date: 23.10.2012<br />
 *
 * @author aabouc, sev26 (UZH)
 */
public class CampusCourseCreationController extends BasicController {

    public static final String COURSE_CREATION_BY_COPYING = "create_by_copying";
    public static final String COURSE_CONTINUATION = "continue";

    private static final String CANCEL = "cancel";

    private static final int RESULTS_PER_PAGE = 5;

    private final VelocityContainer campusCourseVC;

    private RepositoryTableModel creationTableModel, continuationTableModel;

    private TableController creationTableCtrl, continuationTableCtrl;

    private TextComponent infoTextComp;

    private DialogBoxController courseContinuationDialog;

    private Link cancelButton;

    private long selectedResouceableId;

    private final Long sapCampusCourseId;
    private final String campusCourseTitle;

    private final CampusCourseCoreService campusCourseCoreService;
    private final RepositoryManager repositoryManager;
    private final CampusCourseOlatHelper campusCourseOlatHelper;

    private CloseableModalController cmc;

    public void setCmc(CloseableModalController cmc) {
        this.cmc = cmc;
    }

    public CampusCourseCreationController(WindowControl wControl,
                                          UserRequest ureq,
                                          String variation,
                                          Long sapCampusCourseId,
                                          String campusCourseTitle,
                                          CampusCourseCoreService campusCourseCoreService,
                                          RepositoryManager repositoryManager,
                                          CampusCourseOlatHelper campusCourseOlatHelper
    ) {
        super(ureq, wControl);

        this.sapCampusCourseId = sapCampusCourseId;
        this.campusCourseTitle = campusCourseTitle;
        this.campusCourseCoreService = campusCourseCoreService;
        this.repositoryManager = repositoryManager;
        this.campusCourseOlatHelper = campusCourseOlatHelper;

        this.campusCourseVC = this.createVelocityContainer("campusCourseCreation");

        Translator resourceTrans = Util.createPackageTranslator(
                RepositoryTableModel.class, ureq.getLocale(), getTranslator());

        // Create the TableGuiConfiguration
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setResultsPerPage(RESULTS_PER_PAGE);
        tableConfig.setPreferencesOffered(true, "CampusCourseCreationTableGuiPrefs");

        // Create the RepositoryTableModel
        /**
         * TODO sev26
         * Use "resourceTrans" as class constructor parameter. In order to be
         * able to, extend the {@link RepositoryTableModel} class.
         */
        creationTableModel = new RepositoryTableModel(ureq.getLocale());
        continuationTableModel = new RepositoryTableModel(ureq.getLocale());

        // Create the TableController
        creationTableCtrl = createTableCtrl(resourceTrans, tableConfig, ureq);
        creationTableCtrl.setTableDataModel(creationTableModel);

        continuationTableCtrl = createTableCtrl(resourceTrans, tableConfig, ureq);
        continuationTableCtrl.setTableDataModel(continuationTableModel);

        // Create the submit and the cancel Buttons
        cancelButton = LinkFactory.createButton(CANCEL, campusCourseVC, this);

        campusCourseVC.contextPut("variation", variation);
        campusCourseVC.contextPut("infoKey", variation.equals("continue") ? "info.campus.course.continue" : "info.campus.course.creation.by.copying");

        putInitialPanel(campusCourseVC);

        update(variation, ureq);
    }

    private TableController createTableCtrl(Translator resourceTrans, TableGuiConfiguration tableConfig, UserRequest ureq) {
        TableController tableCtrl = new TableController(tableConfig, ureq, getWindowControl(), resourceTrans, true);

        tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("campus.course.table.header.displayname", RepositoryTableModel.RepoCols.displayname.ordinal(), RepositoryTableModel.TABLE_ACTION_SELECT_LINK, resourceTrans.getLocale()));
        tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", RepositoryTableModel.RepoCols.author.ordinal(), null, resourceTrans.getLocale()));
        tableCtrl.addColumnDescriptor(false, new DefaultColumnDescriptor("campus.course.table.header.access", RepositoryTableModel.RepoCols.access.ordinal(), null, resourceTrans.getLocale()));
        tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("table.header.date", RepositoryTableModel.RepoCols.creationDate.ordinal(), null, resourceTrans.getLocale()));
        tableCtrl.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lastusage", RepositoryTableModel.RepoCols.lastUsage.ordinal(), null, resourceTrans.getLocale()));
        tableCtrl.setSortColumn(0, true);
        tableCtrl.setMultiSelect(false);
        listenTo(tableCtrl);

        return tableCtrl;
    }

    public long getSelectedResouceableId() {
        return selectedResouceableId;
    }

    public String getCampusCourseTitle() {
        return campusCourseTitle;
    }

    private void refreshCreationModel(List<RepositoryEntry> entries) {
        creationTableModel.setObjects(entries);
        creationTableCtrl.modelChanged(true);
    }

    private void refreshContinuationModel(List<RepositoryEntry> entries) {
        continuationTableModel.setObjects(entries);
        continuationTableCtrl.modelChanged(true);
    }

    private void update(String courseCreationVariation, UserRequest ureq) {
        switch (courseCreationVariation) {
            case COURSE_CREATION_BY_COPYING:
                campusCourseVC.remove(continuationTableCtrl.getInitialComponent());
                refreshCreationModel(repositoryManager.queryByOwner(ureq.getIdentity(), "CourseModule"));
                campusCourseVC.put("tableController", creationTableCtrl.getInitialComponent());
                campusCourseVC.contextPut("createByTemplate", false);
                break;

            case COURSE_CONTINUATION:
                campusCourseVC.remove(creationTableCtrl.getInitialComponent());
                List<RepositoryEntry> campusCourseEntries = new ArrayList<RepositoryEntry>();
                List<RepositoryEntry> entries = repositoryManager.queryByOwner(ureq.getIdentity(), "CourseModule");
                if (!entries.isEmpty()) {
                    List<Long> allCreatedSapCourcesResourceableIds = campusCourseCoreService.getAllCreatedSapCourcesResourceableIds();
                    for (RepositoryEntry entry : entries) {
                        Long resourcableId = entry.getOlatResource().getResourceableId();
                        if (allCreatedSapCourcesResourceableIds.contains(resourcableId)) {
                            if (StringUtils.left(getCampusCourseTitle(), 4).compareTo(StringUtils.left(entry.getDisplayname(), 4)) > 0) {
                                campusCourseEntries.add(entry);
                            }
                        }
                    }
                }
                refreshContinuationModel(campusCourseEntries);
                campusCourseVC.put("tableController", continuationTableCtrl.getInitialComponent());
                campusCourseVC.contextPut("createByTemplate", false);
                break;
        }
        campusCourseVC.setDirty(true);
    }

    private void openErrorDialog(final UserRequest ureq, String errorText) {
        showError(errorText);
    }

    private void closeDialog(final UserRequest ureq) {
        if (cmc != null) {
            cmc.deactivate();
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == cancelButton) {
            closeDialog(ureq);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == courseContinuationDialog) {
            if (event.equals(Event.CANCELLED_EVENT)) {
                closeDialog(ureq);
            }
        } else if (source == creationTableCtrl || source == continuationTableCtrl) {
            if (event instanceof TableEvent) {
                TableEvent te = (TableEvent)event;
                int rowId = te.getRowId();
                if (te.getActionId().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
                    final RepositoryEntry selectedEntry = (RepositoryEntry)((TableController)source).getTableDataModel().getObject(rowId);
                    final OLATResource ores = OLATResourceManager.getInstance().findResourceable(selectedEntry.getOlatResource());
                    selectedResouceableId = ores.getResourceableId();
                    if (source == creationTableCtrl) {
                        campusCourseOlatHelper.createCampusCourseFromResourcableId(ureq, getWindowControl(), sapCampusCourseId, selectedResouceableId);
                        closeDialog(ureq);
                    } else if (source == continuationTableCtrl) {
                        openCourseContinuationDialog(ureq, selectedEntry.getDisplayname());
                    }
                }
            }
        }
    }

    private void openCourseContinuationDialog(final UserRequest ureq, String courseTitle) {
        List<String> buttonLabels = new ArrayList<String>();
        buttonLabels.add(translate("popup.course.continuation.button.label.yes"));
        buttonLabels.add(translate("popup.course.continuation.button.label.no"));
        String text = translate("popup.course.continuation.text", StringHelper.escapeHtml(courseTitle));
        courseContinuationDialog = activateGenericDialog(ureq, translate("popup.course.continuation.title", courseTitle), text, buttonLabels, courseContinuationDialog);
    }

    @Override
    protected void doDispose() {
    }

}
