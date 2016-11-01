package ch.uzh.campus.presentation;

import ch.uzh.campus.CampusCourseConfiguration;
import ch.uzh.campus.data.DaoManager;
import ch.uzh.campus.data.Semester;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.CorruptedCourseException;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryServiceImpl;
import org.olat.resource.OLATResource;

import java.util.List;

/**
 * @author Christian Guretzki
 * @author Martin Schraner
 */
public class CampusAdminController extends FormBasicController {

    private static final OLog log = Tracing.createLoggerFor(CampusAdminController.class);
    private static final int MAX_NUMBER_OF_SEMESTERS_TO_DISPLAY = 2;

    private TextElement repositoryEntryIdTextElement;
    private SingleSelection languagesSelection;
    private static String[] languages = { "DE", "EN", "FR", "IT" };
    private SingleSelection semestersSelection;
    private Long selectedCurrentSemesterId;
    private Semester currentSemester;
    private CampusCourseConfiguration campusCourseConfiguration;
    private DaoManager daoManager;

    public CampusAdminController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl, LAYOUT_BAREBONE);
        setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

        campusCourseConfiguration = (CampusCourseConfiguration) CoreSpringFactory.getBean(CampusCourseConfiguration.class);
        daoManager = (DaoManager) CoreSpringFactory.getBean(DaoManager.class);

        initForm(this.flc, this, ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        FormLayoutContainer templateFormLayoutContainer = FormLayoutContainer.createDefaultFormLayout("template", getTranslator());
        templateFormLayoutContainer.setFormTitle(translate("campus.admin.form.template.title"));
        formLayout.add(templateFormLayoutContainer);
        templateFormLayoutContainer.setRootForm(mainForm);

        // Languages selection
        languagesSelection = uifactory.addDropdownSingleselect("languagesSelection", "campus.admin.form.template.language", templateFormLayoutContainer, languages, languages, null);
        String defaultLangauge = "DE";
        languagesSelection.select(defaultLangauge, true);
        languagesSelection.addActionListener(FormEvent.ONCHANGE);

        // Repository entry id
        repositoryEntryIdTextElement = uifactory.addTextElement("repositoryEntryId", "campus.admin.form.repositoryEntry.id", 60, "", templateFormLayoutContainer);
        repositoryEntryIdTextElement.setValue(campusCourseConfiguration.getTemplateRepositoryEntryId(defaultLangauge).toString());

        // Semesters selection
        currentSemester = daoManager.getCurrentSemester();

        // currentSemester is null if this method is called without ever running a campus course import before
        // In this case do not add the semesters selection
        if (currentSemester != null) {

            FormLayoutContainer currentSemesterFormLayoutContainer = FormLayoutContainer.createDefaultFormLayout("currentSemester", getTranslator());
            currentSemesterFormLayoutContainer.setFormTitle(translate("campus.admin.form.currentSemester.title"));
            formLayout.add(currentSemesterFormLayoutContainer);
            currentSemesterFormLayoutContainer.setRootForm(mainForm);

            // Prepare semestersSelection
            List<Semester> semestersAll = daoManager.getSemestersInAscendingOrder();
            int numberOfSemestersToDisplay = Math.min(semestersAll.size(), MAX_NUMBER_OF_SEMESTERS_TO_DISPLAY);
            List<Semester> semesters = semestersAll.subList(semestersAll.size() - numberOfSemestersToDisplay, semestersAll.size());

            // Currently set currentSemester must always be selectable
            if (!semesters.contains(currentSemester)) {
                semesters.add(0, currentSemester);
            }
            String[] semesterNameYears = semesters.stream().map(Semester::getSemesterNameYear).toArray(String[]::new);
            String[] semesterIdsAsString = semesters.stream().map(semester -> semester.getId().toString()).toArray(String[]::new);

            semestersSelection = uifactory.addDropdownSingleselect("currentSemesterSelection", "campus.admin.form.currentSemester.selection", currentSemesterFormLayoutContainer, semesterIdsAsString, semesterNameYears, null);
            String defaultSemesterIdAsString = currentSemester.getId().toString();
            semestersSelection.select(defaultSemesterIdAsString, true);
            semestersSelection.addActionListener(FormEvent.ONCHANGE);
            selectedCurrentSemesterId = currentSemester.getId();
        }

        uifactory.addFormSubmitButton("save", formLayout);
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == languagesSelection) {
            Long templateCourseResourcableId = campusCourseConfiguration.getTemplateRepositoryEntryId(languagesSelection.getSelectedKey());
            if (templateCourseResourcableId != null) {
                repositoryEntryIdTextElement.setValue(templateCourseResourcableId.toString());
            }
        } else if (source == semestersSelection) {
            selectedCurrentSemesterId = Long.parseLong(semestersSelection.getSelectedKey());
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to clean up
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        if (!isNumber(repositoryEntryIdTextElement.getValue())) {
            repositoryEntryIdTextElement.setErrorKey("error.campus.admin.form.repositoryEntry.id", null);
            return false;
        }
        return true;
    }

    private boolean isNumber(String stringValue) {
        try {
            Long.parseLong(stringValue);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    protected void formOK(UserRequest ureq) {
        log.info("formOk: value=" + repositoryEntryIdTextElement.getValue());
        try {
            Long templateRepositoryEntryAsLong = Long.parseLong(repositoryEntryIdTextElement.getValue());
            // Validate given number by trying to load the course
            RepositoryService repositoryService = (RepositoryServiceImpl) CoreSpringFactory.getBean(RepositoryServiceImpl.class);
            OLATResource olatResource = repositoryService.loadRepositoryEntryResource(templateRepositoryEntryAsLong);
            if (olatResource == null) {
                throw new CorruptedCourseException("Provided template is not valid");
            }
            // Course is now validated and can be used as template => write property
            campusCourseConfiguration.saveTemplateRepositoryEntryId(templateRepositoryEntryAsLong, languagesSelection.getSelectedKey());
            // Save currentSemester
            if (currentSemester != null) {
                daoManager.setCurrentSemester(selectedCurrentSemesterId);
            }
        } catch (NumberFormatException ex) {
            this.showWarning("campus.admin.form.could.not.save");
        } catch (CorruptedCourseException ex) {
            this.showWarning("campus.admin.form.could.not.save.invalid.course");
        }

    }
}
