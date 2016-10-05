package ch.uzh.campus.presentation;

import ch.uzh.campus.CampusCourseConfiguration;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CorruptedCourseException;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryServiceImpl;
import org.olat.resource.OLATResource;

/**
 * @author Christian Guretzki
 */
public class CampusAdminController extends FormBasicController {

    private static final OLog log = Tracing.createLoggerFor(CampusAdminController.class);
    private TextElement repositoryEntryIdTextElement;
    private SingleSelection languagesSelection;
    private static String[] languages = { "DE", "EN", "FR", "IT" };
    private static String DEFAULT_LANGUAGE = "DE";
    private CampusCourseConfiguration campusCourseConfiguration;
    private Long templateRepositoryEntryId;

    public CampusAdminController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        campusCourseConfiguration = (CampusCourseConfiguration) CoreSpringFactory.getBean(CampusCourseConfiguration.class);
        templateRepositoryEntryId = campusCourseConfiguration.getTemplateRepositoryEntryId(DEFAULT_LANGUAGE);
        initForm(this.flc, this, ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("header.campus.admin");

        languagesSelection = uifactory.addDropdownSingleselect("languagesSelection", "campus.admin.form.template.language", formLayout, languages, languages, null);
        languagesSelection.select(DEFAULT_LANGUAGE, true);
        languagesSelection.addActionListener(FormEvent.ONCHANGE);

        repositoryEntryIdTextElement = uifactory.addTextElement("repositoryEntryId", "campus.admin.form.repositoryEntry.id", 60, "", formLayout);
        repositoryEntryIdTextElement.setValue(templateRepositoryEntryId.toString());

        uifactory.addFormSubmitButton("save", formLayout);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == languagesSelection) {
            Long templateCourseResourcableId = campusCourseConfiguration.getTemplateRepositoryEntryId(languagesSelection.getSelectedKey());
            if (templateCourseResourcableId != null) {
                repositoryEntryIdTextElement.setValue(templateCourseResourcableId.toString());
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to clean up
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
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
    protected void formOK(final UserRequest ureq) {
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
        } catch (NumberFormatException ex) {
            this.showWarning("campus.admin.form.could.not.save");
        } catch (CorruptedCourseException ex) {
            this.showWarning("campus.admin.form.could.not.save.invalid.course");
        }

    }
}
