package ch.uzh.campus.olat;

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
				.addFormLink("create_" + this.getKey(), "create", "create", null, null, Link.LINK);
		createLink.setUserObject(this);
		createLink.setCustomEnabledLinkCSS("o_create btn-block");
		createLink.setIconRightCSS("o_icon o_icon_create");
	}

	public FormLink getCreateLink() {
		return createLink;
	}

	@Override
	public Object getValueAt(int col) {
		switch(col) {
			case 0: return getCreateLink();
		}
		return null;
	}
}
