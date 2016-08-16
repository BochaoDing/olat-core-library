package ch.uzh.campus.olat.list;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.ui.list.RepositoryEntryRow;

import static ch.uzh.campus.olat.CampusCourseBeanFactory.AUTHOR_LECTURER_RESOURCEABLE_TYPE_NAME;
import static ch.uzh.campus.olat.CampusCourseBeanFactory.LECTURER_RESOURCEABLE_TYPE_NAME;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
public class CampusCourseRepositoryEntryRow extends RepositoryEntryRow {

	private final FormItem formItem;

	public CampusCourseRepositoryEntryRow(RepositoryEntryMyView entry,
										  Translator translator) {
		super(entry);

        if (AUTHOR_LECTURER_RESOURCEABLE_TYPE_NAME.equals(entry
				.getOlatResource().getResourceableTypeName())) {
			/**
			 * Link name must be unique!
			 */
			FormLink tmp = FormUIFactory.getInstance()
					.addFormLink("create_" + getKey(), "createCampusCourse",
							"list.course.create", null, null, Link.LINK);
			tmp.setUserObject(this);
			tmp.setCustomEnabledLinkCSS("o_create btn-block");
			tmp.setIconRightCSS("o_icon o_icon_create");
			formItem = tmp;
		} else if (LECTURER_RESOURCEABLE_TYPE_NAME.equals(entry
				.getOlatResource().getResourceableTypeName())) {
			/**
			 * TODO sev26
			 * The translation should occur during the rendering. However,
			 * this feature is not provided by OpenOLAT.
			 */
			formItem = FormUIFactory.getInstance().addStaticExampleText(
					"list.course.author.right.required",
					translator.translate("list.course.author.right.required"),
					null);
		} else {
			/**
			 * TODO sev26
			 * The translation should occur during the rendering. However,
			 * this feature is not provided by OpenOLAT.
			 */
			formItem = FormUIFactory.getInstance().addStaticExampleText(
					"list.course.progress", translator.translate(
							"list.course.progress"), null);
		}
	}

	public FormItem getFormItem() {
		return formItem;
	}

	@Override
	public Object getValueAt(int col) {
		/**
		 * TODO sev26
		 * Use the enums of the super class.
		 */
		switch(col) {
			case 0:
				return "";
			case 10:
				return getDisplayName();
			case 11:
				return getFormItem();
			case 14:
				return "";
		}
		return null;
	}
}
