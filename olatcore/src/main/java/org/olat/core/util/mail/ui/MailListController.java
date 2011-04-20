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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.core.util.mail.ui;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.manager.MailManager;
import org.olat.core.util.mail.model.DBMailImpl;
import org.olat.core.util.mail.model.DBMailRecipient;
import org.olat.core.util.mail.ui.MailDataModel.Columns;


/**
 * 
 * Description:<br>
 * Represent a list of mails.
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailListController extends BasicController implements Activateable {
	
	private static final String CMD_READ_TOGGLE = "creadt";
	private static final String CMD_READ = "cread";
	private static final String CMD_DELETE = "cdelselected";
	private static final String CMD_MARK_TOGGLE = "cmark";
	private static final String CMD_PROFILE = "cprofile";
	private static final String CMD_SEND_REAL_MAIL = "cfwd";
	private static final String CMD_MARK_READ = "creadselected";
	private static final String CMD_MARK_UNREAD = "cunreadselected";
	private static final String CMD_MARK_MARKED = "cmarkselected";
	private static final String CMD_MARK_UNMARKED = "cunmarkselected";
	private static final String MAIN_CMP = "mainCmp";

	private Link backLink;
	private final VelocityContainer mainVC;
	private MailController mailCtr;
	private MailListController metaMailCtr;
	private final TableController tableCtr;
	private final VelocityContainer tableVC;
	private DialogBoxController deleteConfirmationBox;
	
	
	private final boolean outbox;
	private final String metaId;
	private final MailManager mailManager;
	private final MailContextResolver contextResolver;
	
	public MailListController(UserRequest ureq, WindowControl wControl, boolean outbox, MailContextResolver resolver) {
		this(ureq, wControl, null, outbox, resolver);
	}
	
	private MailListController(UserRequest ureq, WindowControl wControl, String metaId, boolean outbox, MailContextResolver resolver) {
		super(ureq, wControl);
		setBasePackage(MailModule.class);
		this.outbox = outbox;
		this.metaId = metaId;
		this.contextResolver = resolver;
		
		mailManager = MailManager.getInstance();

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "MailBox");		
		tableConfig.setTableEmptyMessage(translate("mail.empty.box"));
		tableConfig.setMultiSelect(true);

		mainVC = createVelocityContainer("mails");
		tableVC = createVelocityContainer("mailsTable");
		
		String context = translate("mail.context");
		tableCtr = new TableController(tableConfig, ureq, wControl, Collections.<ShortName>emptyList(), null, context , null, false, getTranslator());

		//only for outbox
		if(outbox) {
			//context / recipients / subject / sendDate
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.context.i18nKey(), Columns.context.ordinal(), null,
					getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new MailContextCellRenderer(this, tableVC, getTranslator())));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.recipients.i18nKey(), Columns.recipients.ordinal(), null, getLocale()));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.subject.i18nKey(), Columns.subject.ordinal(), CMD_READ, getLocale()));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.sendDate.i18nKey(), Columns.sendDate.ordinal(), null, getLocale()));
		} else {
			//read / marked / context / from / subject / receivedDate
			CustomCellRenderer readRenderer = new BooleanCSSCellRenderer(getTranslator(), "b_mail_read", "b_mail_unread", "mail.read", "mail.unread");
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.read.i18nKey(), Columns.read.ordinal(), CMD_READ_TOGGLE, 
				getLocale(), ColumnDescriptor.ALIGNMENT_CENTER, readRenderer));
			CustomCellRenderer markRenderer = new BooleanCSSCellRenderer(getTranslator(), "b_mail_marked", "b_mail_unmarked", "mail.marked", "mail.unmarked");
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.marked.i18nKey(), Columns.marked.ordinal(), CMD_MARK_TOGGLE, 
					getLocale(), ColumnDescriptor.ALIGNMENT_CENTER, markRenderer));
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.context.i18nKey(), Columns.context.ordinal(), null,
					getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new MailContextCellRenderer(this, tableVC, getTranslator())));
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Columns.from.i18nKey(), Columns.from.ordinal(), null,
					getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new MailFromCellRenderer(this, tableVC, getTranslator())));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.subject.i18nKey(), Columns.subject.ordinal(), CMD_READ, getLocale()));
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(Columns.receivedDate.i18nKey(), Columns.receivedDate.ordinal(), null, getLocale()));
		}

		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_READ, "mail.action.open", translate("mail.action.open")));

		// only for inbox
		if (!outbox) {
			tableCtr.addMultiSelectAction("mail.action.read", CMD_MARK_READ);
			tableCtr.addMultiSelectAction("mail.action.unread", CMD_MARK_UNREAD);
			tableCtr.addMultiSelectAction("mail.action.mark", CMD_MARK_MARKED);
			tableCtr.addMultiSelectAction("mail.action.unmark", CMD_MARK_UNMARKED);			
		}
		tableCtr.addMultiSelectAction("mail.action.send.real", CMD_SEND_REAL_MAIL);
		tableCtr.addMultiSelectAction("delete", CMD_DELETE);
		
		reloadModel();
		
		int dateSort = outbox ? 3 : 5;
		tableCtr.setSortColumn(dateSort, false);

		listenTo(tableCtr);
		
		tableVC.put("tableCmp", tableCtr.getInitialComponent());
		if(outbox) {
			if(StringHelper.containsNonWhitespace(metaId)) {
				tableVC.contextPut("title", translate("mail.outbox.title"));
				tableVC.contextPut("description", translate("mail.outbox.meta"));
				
			} else {
				tableVC.contextPut("title", translate("mail.outbox.title"));
			}
		} else {
			tableVC.contextPut("title", translate("mail.inbox.title"));
		}
		
		mainVC.put(MAIN_CMP, tableVC);
		if(StringHelper.containsNonWhitespace(metaId)) {
			backLink = LinkFactory.createLinkBack(mainVC, this);
			mainVC.put("back", backLink);
		}

		putInitialPanel(mainVC);
	}
	
	private void replaceInModel(DBMailImpl mail) {
		MailDataModel dataModel = (MailDataModel)tableCtr.getTableDataModel();
		dataModel.replace(mail);
		tableCtr.modelChanged();
	}
	
	private void reloadModel() {
		List<DBMailImpl> mails;
		if(outbox) {
			if(StringHelper.containsNonWhitespace(metaId)) {
				mails = MailManager.getInstance().getEmailsByMetaId(metaId);
			} else {
				mails = MailManager.getInstance().getOutbox(getIdentity(), 0, 0);
			}
			
			//strip meta emails
			Set<String> metaIds = new HashSet<String>();
			for(Iterator<DBMailImpl> it=mails.iterator(); it.hasNext(); ) {
				DBMailImpl mail = it.next();
				if(StringHelper.containsNonWhitespace(mail.getMetaId())) {
					if(metaIds.contains(mail.getMetaId())) {
						it.remove();
					} else {
						metaIds.add(mail.getMetaId());
					}
				}
			}
		} else {
			mails = MailManager.getInstance().getInbox(getIdentity(), null, Boolean.TRUE, 0, 0);
		}
		
		//extract contexts
		Map<String, String> bpToContexts = new HashMap<String, String>();
		for(DBMailImpl mail:mails) {
			String businessPath = mail.getContext().getBusinessPath();
			if(StringHelper.containsNonWhitespace(businessPath) && !bpToContexts.containsKey(businessPath)) {
				String contextName = contextResolver.getName(businessPath, getLocale());
				bpToContexts.put(businessPath, contextName);
			}
		}
		
		if(!bpToContexts.isEmpty()) {
			List<ShortName> filters = new ArrayList<ShortName>();
			ShortName allContextFilter = new MailContextShortName("-");
			filters.add(allContextFilter);
			for(Map.Entry<String, String> entry:bpToContexts.entrySet()) {
				String businessPath = entry.getKey();
				String contextName = entry.getValue();
				filters.add(new MailContextShortName(contextName, businessPath));
			}
			tableCtr.setFilters(filters, allContextFilter);
		}
		
		Formatter formatter = Formatter.getInstance(getLocale());
		MailDataModel dataModel = new MailDataModel(mails, bpToContexts, getIdentity(), getTranslator(), formatter);
		tableCtr.setTableDataModel(dataModel);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == backLink) {
			if(mailCtr != null) {
				backFromMail();
			} else {
				fireEvent(ureq, event);
			}
		} else if (source instanceof Link && source.getComponentName().startsWith("bp_")) {
			String businessPath = (String)((Link)source).getUserObject();
			if(StringHelper.containsNonWhitespace(businessPath)) {
				contextResolver.open(ureq, getWindowControl(), businessPath);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				DBMailImpl mail = (DBMailImpl)tableCtr.getTableDataModel().getObject(rowid);
				if(CMD_READ.equals(actionid)) {
					if(outbox && StringHelper.containsNonWhitespace(mail.getMetaId()) && !mail.getMetaId().equals(metaId)) {
						selectMetaMail(ureq, mail.getMetaId());
					} else {
						selectMail(ureq, mail.getKey());
					}
				} else if (CMD_PROFILE.equals(actionid)) {
					DBMailRecipient from = mail.getFrom();
					if(from != null&& from.getRecipient() != null) {
						contextResolver.open(ureq, getWindowControl(), "[Identity:" + from.getRecipient().getKey() + "]");
					}
				} else if (CMD_MARK_TOGGLE.equals(actionid)) {
					mail = mailManager.toggleMarked(mail, getIdentity());
					replaceInModel(mail);
				} else if (CMD_READ_TOGGLE.equals(actionid)) {
					mail = mailManager.toggleRead(mail, getIdentity());
					replaceInModel(mail);
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				// Multiselect events
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				BitSet selectedMails = tmse.getSelection();
				if(selectedMails.isEmpty()){
					tableVC.setDirty(true);
					showWarning("mail.action.emtpy");
					return;					
				}
				String actionid = tmse.getAction();
				if (CMD_DELETE.equals(actionid)) {
					String title = translate("mail.confirm.delete.title");
					int selected = selectedMails.cardinality();
					String text;
					if (selected == 1) {
						text = translate("mail.confirm.delete.single.text");
					} else {
						text = translate("mail.confirm.delete.multi.text", selected + "");						
					}
					deleteConfirmationBox = activateYesNoDialog(ureq, title, text, deleteConfirmationBox);
					deleteConfirmationBox.setUserObject(selectedMails);
				} else if (CMD_SEND_REAL_MAIL.equals(actionid)) {
					for (int i=selectedMails.nextSetBit(0); i >= 0; i=selectedMails.nextSetBit(i+1)) {
						DBMailImpl mail = (DBMailImpl) tableCtr.getTableDataModel().getObject(i);						
						//TODO SR implement forward to users real mail address
						
					}				
					reloadModel();
				} else if (CMD_MARK_MARKED.equals(actionid) || CMD_MARK_UNMARKED.equals(actionid)) {
					for (int i=selectedMails.nextSetBit(0); i >= 0; i=selectedMails.nextSetBit(i+1)) {
						DBMailImpl mail = (DBMailImpl) tableCtr.getTableDataModel().getObject(i);
						mailManager.setMarked(mail, CMD_MARK_MARKED.equals(actionid), getIdentity());
					}				
					reloadModel();
				} else if (CMD_MARK_READ.equals(actionid) || CMD_MARK_UNREAD.equals(actionid)) {
					for (int i=selectedMails.nextSetBit(0); i >= 0; i=selectedMails.nextSetBit(i+1)) {
						DBMailImpl mail = (DBMailImpl) tableCtr.getTableDataModel().getObject(i);
						mailManager.setRead(mail, CMD_MARK_READ.equals(actionid), getIdentity());
					}				
					reloadModel();
				}
				
			} else if (TableController.EVENT_FILTER_SELECTED == event) {
				MailDataModel dataModel = (MailDataModel)tableCtr.getTableDataModel();
				MailContextShortName filter = (MailContextShortName)tableCtr.getActiveFilter();
				dataModel.filter(filter);
			} else if (TableController.EVENT_NOFILTER_SELECTED == event) {
				MailDataModel dataModel = (MailDataModel)tableCtr.getTableDataModel();
				dataModel.filter(null);
			}			
			
		} else if (source == mailCtr) {
			backFromMail();
			
		} else if (source == metaMailCtr) {
			removeAsListenerAndDispose(metaMailCtr);
			metaMailCtr = null;
			mainVC.put(MAIN_CMP, tableVC);
			
		} else if (source == deleteConfirmationBox) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				BitSet deleteMails = (BitSet)deleteConfirmationBox.getUserObject();
				for (int i=deleteMails.nextSetBit(0); i >= 0; i=deleteMails.nextSetBit(i+1)) {
					DBMailImpl mail = (DBMailImpl) tableCtr.getTableDataModel().getObject(i);
					boolean deleteMetaMail = outbox && !StringHelper.containsNonWhitespace(metaId);
					mailManager.delete(mail, getIdentity(), deleteMetaMail);
					// Do not remove from model to prevent concurrent modification
					// exception, instead just reload model afterwards
				}				
				reloadModel();
			}
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, String viewIdentifier) {
		if(!StringHelper.containsNonWhitespace(viewIdentifier) || "0".equals(viewIdentifier)) return;
		
		try {
			Long mailKey = Long.parseLong(viewIdentifier);
			selectMail(ureq, mailKey);
		} catch(NumberFormatException e) {
			//not a key
			logWarn("Cannot activate with this identifier: " + viewIdentifier, e);
		}
	}
	
	private void backFromMail() {
		removeAsListenerAndDispose(mailCtr);
		mailCtr = null;
		mainVC.put(MAIN_CMP, tableVC);
	}
	
	private void selectMetaMail(UserRequest ureq, String metaID) {
		metaMailCtr = new MailListController(ureq, getWindowControl(), metaID, outbox, contextResolver);
		listenTo(metaMailCtr);
		mainVC.put(MAIN_CMP, metaMailCtr.getInitialComponent());
	}
	
	private void selectMail(UserRequest ureq, Long mailKey) {
		DBMailImpl mail = mailManager.getMessageByKey(mailKey);
		selectMail(ureq, mail);
	}
	
	private void selectMail(UserRequest ureq, DBMailImpl mail) {
		removeAsListenerAndDispose(mailCtr);
		boolean back = !StringHelper.containsNonWhitespace(mail.getMetaId()) || !outbox;
		mailCtr = new MailController(ureq, getWindowControl(), mail, back);
		listenTo(mailCtr);
		mainVC.put(MAIN_CMP, mailCtr.getInitialComponent());
		
		if(mailManager.setRead(mail, Boolean.TRUE, getIdentity())) {
			reloadModel();
		}
	}
}
