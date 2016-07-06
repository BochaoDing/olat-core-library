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
	private final FormLink createByCopying;
	private final FormLink createByContinuation;

	public CampusRepositoryEntryRow(RepositoryEntryMyView entry) {
		super(entry);

		createLink = FormUIFactory.getInstance()
				.addFormLink("create_" + this.getKey(), "create", "create", null, null, Link.LINK);
		createLink.setUserObject(this);
		createLink.setCustomEnabledLinkCSS("o_create btn-block");
		createLink.setIconRightCSS("o_icon o_icon_create");

		createByCopying = FormUIFactory.getInstance()
				.addFormLink("create_by_copying_" + this.getKey(), "create_by_copying", "create_by_copying", null, null, Link.LINK);
		createByCopying.setUserObject(this);
		createByCopying.setCustomEnabledLinkCSS("o_create_by_copying btn-block");
		createByCopying.setIconRightCSS("o_icon o_icon_create_by_copying");

		createByContinuation = FormUIFactory.getInstance()
				.addFormLink("continue_" + this.getKey(), "continue", "continue", null, null, Link.LINK);
		createByContinuation.setUserObject(this);
		createByContinuation.setCustomEnabledLinkCSS("o_continue btn-block");
		createByContinuation.setIconRightCSS("o_icon o_icon_continue");
	}

	public FormLink getCreateLink() {
		return createLink;
	}

	public FormLink getCreateByCopying() {
		return createByCopying;
	}

	public FormLink getCreateByContinuation() {
		return createByContinuation;
	}

	@Override
	public Object getValueAt(int col) {
		switch(col) {
			case 0:
				return getCreateLink();
			case 3:
				return getCreateByCopying();
			case 4:
				return getCreateByContinuation();
		}
		return null;
	}
}
