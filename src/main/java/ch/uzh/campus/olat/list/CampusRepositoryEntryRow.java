package ch.uzh.campus.olat.list;

import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.Link;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.ui.list.RepositoryEntryRow;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
public class CampusRepositoryEntryRow extends RepositoryEntryRow {

	private final FormLink createLink;

	public CampusRepositoryEntryRow(RepositoryEntryMyView entry) {
		super(entry);

		createLink = FormUIFactory.getInstance()
				.addFormLink("create_" + getKey(), "createCampusCourse",
						"list.create.course", null,
						null, Link.LINK);
		createLink.setUserObject(this);
		createLink.setCustomEnabledLinkCSS("o_create btn-block");
		createLink.setIconRightCSS("o_icon o_icon_create");
	}

	public FormLink getCreateLink() {
		return createLink;
	}

	@Override
	public Object getValueAt(int col) {
		/**
		 * TODO sev26
		 * Use the enums of the super class.
		 */
		switch(col) {
			case 0:
				return getCreateLink();
			case 10:
				return getDisplayName();
			case 11:
				return getCreateLink();
			case 14:
				return "Not ready.";
		}
		return null;
	}
}
