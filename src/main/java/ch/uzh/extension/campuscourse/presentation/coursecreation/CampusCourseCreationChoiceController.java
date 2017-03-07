package ch.uzh.extension.campuscourse.presentation.coursecreation;

import ch.uzh.extension.campuscourse.presentation.CampusCoursePresentationHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: 2016-07-13<br />
 * @author sev26 (UZH)
 */
public class CampusCourseCreationChoiceController extends FormBasicController {

	public interface CampusCourseCreationChoiceControllerListener {
		void onChoice(int selected, WindowControl windowControl,
					  UserRequest userRequest);
	}

	private static final String CC_CREATION_BY_TEMPLATE = "campus.course.creation.by.template";
	private static final String CC_CREATION_BY_COPYING = "campus.course.creation.by.copying";
	private static final String CC_CONTINUATION = "campus.course.continuation";

	private static final String[] campusCourseCreationKeys = new String[] {
			CC_CREATION_BY_TEMPLATE, CC_CREATION_BY_COPYING, CC_CONTINUATION };
	private static final String[] campusCourseCreationOptions =
			new String[campusCourseCreationKeys.length];

	private final SingleSelection campusCourseCreationRadioButtons;
	private final CampusCourseCreationChoiceControllerListener listener;

	public CampusCourseCreationChoiceController(CampusCourseCreationChoiceControllerListener listener,
												WindowControl windowControl,
												UserRequest userRequest) {
		super(userRequest, windowControl, FormBasicController.LAYOUT_VERTICAL);

		this.listener = listener;
		Translator translator = CampusCoursePresentationHelper.getTranslator(userRequest.getLocale());

		initForm(userRequest);

		for (int i = 0; i < campusCourseCreationKeys.length; i++) {
			campusCourseCreationOptions[i] = translator.translate(campusCourseCreationKeys[i]);
		}
		campusCourseCreationRadioButtons = FormUIFactory.getInstance()
				.addRadiosVertical("campus.course.creation.radio", flc,
						campusCourseCreationKeys, campusCourseCreationOptions);
		campusCourseCreationRadioButtons.setTranslator(translator);
		campusCourseCreationRadioButtons.select(campusCourseCreationKeys[0], true);
		campusCourseCreationRadioButtons.addActionListener(FormEvent.ONCLICK);
	}

	@Override
	protected void initForm(final FormItemContainer formLayout, Controller listener, UserRequest userRequest) {
		// "initForm(userRequest)" is not called.
	}

	@Override
	protected void formOK(UserRequest userRequest) {
	}

	@Override
	protected void doDispose() {
	}

	@Override
	protected void formInnerEvent(UserRequest userRequest, FormItem source, FormEvent event) {
		listener.onChoice(campusCourseCreationRadioButtons.getSelected(),
				getWindowControl(), userRequest);
	}
}
