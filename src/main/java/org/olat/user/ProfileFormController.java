/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.user;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.registration.TemporaryKeyImpl;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;

import de.bps.olat.user.ChangeEMailController;

/**
 * Provides a controller which lets the user edit their user profile and choose
 * the fields which are made publicly visible.
 * 
 * @author twuersch
 * 
 */
public class ProfileFormController extends FormBasicController {

	private static final String usageIdentifier= ProfileFormController.class.getCanonicalName();
	private static final String SEPARATOR = "\n____________________________________________________________________\n";

	private final Map<String, FormItem> formItems = new HashMap<String, FormItem>();
	private final Map<String, String> formContext = new HashMap<String, String>();
	private RichTextElement textAboutMe;

	private Identity identityToModify;
	private DialogBoxController dialogCtr;
	
	private FormLink deletePortrait;
	private FileElement portraitUpload;
	
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private boolean emailChanged = false;
	private String changedEmail;
	private String currentEmail;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RegistrationManager rm;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private HomePageConfigManager hpcm;
	@Autowired
	private DisplayPortraitManager dps;
	
	/**
	 * Create this controller with the request's identity as none administrative
	 * user. This constructor can be use in after login interceptor.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public ProfileFormController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, ureq.getIdentity(), false);
	}

	/**
	 * Creates this controller.
	 * 
	 * @param ureq The user request.
	 * @param wControl The window control.
	 * @param conf The homepage configuration (decides which user profile fields
	 *          are visible for everyone).
	 * @param identity The identity of the user.
	 * @param isAdministrativeUser true: user is editing another users profile as
	 *          user manager; false: use is editing his own profile
	 */
	public ProfileFormController(UserRequest ureq, WindowControl wControl,
			Identity identityToModify, boolean isAdministrativeUser) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.identityToModify = identityToModify;
		this.isAdministrativeUser = isAdministrativeUser;
		
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifier, isAdministrativeUser);
		initForm(ureq);
	}
	
	public Identity getIdentityToModify() {
		return identityToModify;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose.
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		User user = identityToModify.getUser();

		// show a form element for each property handler 
		boolean first = true;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) {
				continue;
			}
			
			// add spacer if necessary (i.e. when group name changes)
			String group = userPropertyHandler.getGroup();
			String formId = "group." + group;
			FormLayoutContainer groupContainer = (FormLayoutContainer)formLayout.getFormComponent(formId);
			if(groupContainer == null) {
				groupContainer = FormLayoutContainer.createDefaultFormLayout(formId, getTranslator());
				groupContainer.setFormTitle(translate("form.group." + group));
				if(first) {
					groupContainer.setFormContextHelp("org.olat.user","home-vcard.html","help.hover.vcard");
					first = false;
				}
				formItems.put(formId, groupContainer);
				formLayout.add(groupContainer);
			}
			
			// add input field to container
			FormItem formItem = userPropertyHandler.addFormItem(getLocale(), user, usageIdentifier, isAdministrativeUser, groupContainer);
			String propertyName = userPropertyHandler.getName();
			formItems.put(propertyName, formItem);
			
			if (formItem instanceof TextElement) {
				// it's a text field, so get the value of this property into the text field
				TextElement textElement = (TextElement)formItem;
				textElement.setValue(user.getProperty(propertyName, getLocale()));
			} else if (formItem instanceof MultipleSelectionElement) {
				// it's a checkbox, so set the box to checked if the corresponding property is set to "true"
				MultipleSelectionElement checkbox = (MultipleSelectionElement)formItem;
				String value = user.getProperty(propertyName, getLocale());
				if (value != null) {
					checkbox.select(propertyName, value.equals("true"));
				} else {
					// assume "false" if the property is not present
					checkbox.select(propertyName, false);
				}
			}
			
			// special case for email field
			if (userPropertyHandler.getName().equals("email")) {
				String key = user.getProperty("emchangeKey", null);
				TemporaryKeyImpl tempKey = rm.loadTemporaryKeyByRegistrationKey(key);
				if (tempKey != null) {
					XStream xml = XStreamHelper.createXStreamInstance();
					@SuppressWarnings("unchecked")
					HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(tempKey.getEmailAddress());
					formItem.setExampleKey("email.change.form.info", new String[] {mails.get("changedEMail")});
				}
			}
		}
		
		// add the "about me" text field.
		FormLayoutContainer groupContainer = FormLayoutContainer.createDefaultFormLayout("group.about", getTranslator());
		groupContainer.setFormTitle(translate("form.group.about"));
		formLayout.add(groupContainer);
		
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify.getName());
		textAboutMe = uifactory.addRichTextElementForStringData("form.text", "form.text",
				conf.getTextAboutMe(), 10, -1, false, null, null, groupContainer,
				ureq.getUserSession(), getWindowControl());
		textAboutMe.setMaxLength(10000);
		
		//upload image
		groupContainer = FormLayoutContainer.createDefaultFormLayout("portraitupload", getTranslator());
		groupContainer.setFormTitle(translate("ul.header"));
		groupContainer.setFormContextHelp("org.olat.user","home-picture.html","chelp.home-picture.hover");
		formLayout.add(groupContainer);

		deletePortrait = uifactory.addFormLink("command.delete", groupContainer, Link.BUTTON);

		File portraitFile = dps.getBigPortrait(identityToModify.getName());
		// Init upload controller
		Set<String> mimeTypes = new HashSet<String>();
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");

		portraitUpload = uifactory.addFileElement("ul.select", "ul.select", groupContainer);
		portraitUpload.setMaxUploadSizeKB(10000, null, null);
		portraitUpload.setPreview(ureq.getUserSession(), true);
		portraitUpload.addActionListener(FormEvent.ONCHANGE);
		if(portraitFile != null) {
			portraitUpload.setInitialFile(portraitFile);
		}
		portraitUpload.limitToMimeType(mimeTypes, null, null);
		
		// Create submit and cancel buttons
		FormLayoutContainer buttonLayoutWrappper = FormLayoutContainer.createDefaultFormLayout("buttonLayoutWrappper", getTranslator());
		formLayout.add(buttonLayoutWrappper);
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayoutInner", getTranslator());
		buttonLayoutWrappper.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	/**
	 * Stores the data from the form into a) the user's home page configuration
	 * and b) the user's properties.
	 * 
	 * @param config The user's home page configuration (i.e. flags for publicly
	 *          visible fields).
	 * @param identity The user's identity
	 */
	public void updateFromFormData() {
		User user = identityToModify.getUser();
		// For each user property...
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			// ...get the value from the form field and store it into the user
			// property...
			FormItem formItem = formItems.get(userPropertyHandler.getName());
			if(formItem.isEnabled()) {
				userPropertyHandler.updateUserFromFormItem(user, formItem);
			}
		}
		// Store the "about me" text.
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify.getName());
		conf.setTextAboutMe(textAboutMe.getValue());
		hpcm.saveConfigTo(identityToModify.getName(), conf);
	}
	
	public Identity updateIdentityFromFormData(Identity identity) {
		identityToModify = identity;
		User user = identityToModify.getUser();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem formItem = formItems.get(userPropertyHandler.getName());
			if(formItem.isEnabled()) {
				userPropertyHandler.updateUserFromFormItem(user, formItem);
			}
		}
		return identityToModify;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		formContext.put("username", identityToModify.getName());
		
		User user = identityToModify.getUser();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem formItem = formItems.get(userPropertyHandler.getName());
			if(formItem.isEnabled()) {
				formItem.clearError();
				allOk &= userPropertyHandler.isValid(user, formItem, formContext);
			}
		}
		
		try {
			String aboutMe = textAboutMe.getValue();
			if(aboutMe.length() > 10000) {
				textAboutMe.setErrorKey("input.toolong", new String[] {"10000"});
				allOk = false;
			} else {
				textAboutMe.clearError();
			}
		} catch (Exception e) {
			textAboutMe.setErrorKey("input.toolong", new String[] {"10000"});
			allOk = false;
		}
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogCtr) {
			dialogCtr.dispose();
			dialogCtr = null;
			if (DialogBoxUIFactory.isYesEvent(event)) {
				if (changedEmail != null) {
					createChangeEmailWorkflow(ureq);
				}
			}
			fireEvent(ureq, Event.FAILED_EVENT);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		 if (source == portraitUpload) {
			if (portraitUpload.isUploadSuccess()) {
				deletePortrait.setVisible(true);
					flc.setDirty(true);
			}
		} else if (source == deletePortrait) {
			File img = dps.getBigPortrait(identityToModify.getName());
			if(portraitUpload.getUploadFile() != null) {
				portraitUpload.reset();
				if(img == null) {
					deletePortrait.setVisible(false);
				} else {
					deletePortrait.setVisible(true);
				}
			} else if(img != null) {
				dps.deletePortrait(identityToModify);
				deletePortrait.setVisible(false);
				portraitUpload.setInitialFile(null);
				notifyPortraitChanged();
			}
			flc.setDirty(true);
		}

		super.formInnerEvent(ureq, source, event);
	}
	
	private void notifyPortraitChanged() {
		ProfileEvent newPortraitEvent = new ProfileEvent("changed-portrait", identityToModify.getKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("portrait", getIdentity().getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(newPortraitEvent, ores);
	}

	@Override
	protected void formOK(final UserRequest ureq) {
		User user = identityToModify.getUser();
		// update each user field
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem formItem = formItems.get(userPropertyHandler.getName());
			if(formItem.isEnabled()) {
				userPropertyHandler.updateUserFromFormItem(user, formItem);
			}
		}
		
		File uploadedImage = portraitUpload.getUploadFile();
		String uploadedFilename = portraitUpload.getUploadFileName();
		if(uploadedImage != null) {
			dps.setPortrait(uploadedImage, uploadedFilename, identityToModify.getName());
			notifyPortraitChanged();
		}
		
		// Store the "about me" text.
		HomePageConfig conf = hpcm.loadConfigFor(identityToModify.getName());
		conf.setTextAboutMe(textAboutMe.getValue());
		hpcm.saveConfigTo(identityToModify.getName(), conf);

		// fire the appropriate event
		fireEvent(ureq, Event.DONE_EVENT);

		// update the user profile data
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(
			OresHelper.createOLATResourceableInstance(Identity.class, identityToModify.getKey()), new SyncerExecutor() {
			@Override
			public void execute() {
				UserManager um = UserManager.getInstance();
				identityToModify = (Identity) DBFactory.getInstance().loadObject(identityToModify);
				currentEmail = identityToModify.getUser().getProperty("email", null);

				identityToModify = updateIdentityFromFormData(identityToModify);
				changedEmail = identityToModify.getUser().getProperty("email", null);
				if (!currentEmail.equals(changedEmail)) {
					// allow an admin to change email without verification workflow. usermanager is only permitted to do so, if set by config.
					if ( !(ureq.getUserSession().getRoles().isOLATAdmin()
							|| (BaseSecurityModule.USERMANAGER_CAN_BYPASS_EMAILVERIFICATION && ureq.getUserSession().getRoles().isUserManager() ))) {
						emailChanged = true;
						// change email address to old address until it is verified
						identityToModify.getUser().setProperty("email", currentEmail);
					} else {
						String key = identityToModify.getUser().getProperty("emchangeKey", null);
						TemporaryKeyImpl tempKey = rm.loadTemporaryKeyByRegistrationKey(key);
						if (tempKey != null) {
							rm.deleteTemporaryKey(tempKey);
						}		
					}
				}
				if (!um.updateUserFromIdentity(identityToModify)) {
					getWindowControl().setInfo(translate("profile.unsuccessful"));
					// reload user data from db
					identityToModify = BaseSecurityManager.getInstance().loadIdentityByKey(identityToModify.getKey());
				}
				
				OLATResourceable modRes = OresHelper.createOLATResourceableInstance(Identity.class, identityToModify.getKey());
				CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(new MultiUserEvent("changed"), modRes);
				
				if (!emailChanged) {
					fireEvent(ureq, Event.FAILED_EVENT);
				}
			}
		});
		
		if (emailChanged) {
			removeAsListenerAndDispose(dialogCtr);

			String changerEMail = ureq.getIdentity().getUser().getProperty("email", ureq.getLocale());
			String dialogText = "";
			if(changerEMail != null && changerEMail.length() > 0 && changerEMail.equals(currentEmail)) {
				dialogText = translate("email.change.dialog.text");
			} else {
				dialogText = translate("email.change.dialog.text.usermanager");
			}
			dialogCtr = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("email.change.dialog.title"), dialogText);
			listenTo(dialogCtr);
			dialogCtr.activate();
		}
	}
	
	private void createChangeEmailWorkflow(UserRequest ureq) {
		// send email
		changedEmail = changedEmail.trim();
		String body = null;
		String subject = null;
		// get remote address
		String ip = ureq.getHttpReq().getRemoteAddr();
		String today = DateFormat.getDateInstance(DateFormat.LONG, ureq.getLocale()).format(new Date());
		// mailer configuration
		String serverpath = Settings.getServerContextPathURI();
		String servername = ureq.getHttpReq().getServerName();

		logDebug("this servername is " + servername + " and serverpath is " + serverpath, null);
		// load or create temporary key
		Map<String, String> mailMap = new HashMap<String, String>();
		mailMap.put("currentEMail", currentEmail);
		mailMap.put("changedEMail", changedEmail);
		
		XStream xml = new XStream();
		String serMailMap = xml.toXML(mailMap);
		
		TemporaryKey tk = loadCleanTemporaryKey(serMailMap);				
		if (tk == null) {
			tk = rm.createTemporaryKeyByEmail(serMailMap, ip, RegistrationManager.EMAIL_CHANGE);
		} else {
			rm.deleteTemporaryKeyWithId(tk.getRegistrationKey());
			tk = rm.createTemporaryKeyByEmail(serMailMap, ip, RegistrationManager.EMAIL_CHANGE);
		}
		
		// create date, time string
		Calendar cal = Calendar.getInstance();
		cal.setTime(tk.getCreationDate());
		cal.add(Calendar.DAY_OF_WEEK, ChangeEMailController.TIME_OUT);
		String time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(cal.getTime());
		// create body and subject for email
		String link = serverpath + "/dmz/emchange/index.html?key=" + tk.getRegistrationKey() + "&lang=" + ureq.getLocale().getLanguage();
		if(Settings.isDebuging()) {
			logInfo(link, null);
		}
		body = translate("email.change.body", new String[] { link, time, currentEmail, changedEmail })
				+ SEPARATOR + translate("email.change.wherefrom", new String[] { serverpath, today, ip });
		subject = translate("email.change.subject");
		// send email
		try {
			
			MailBundle bundle = new MailBundle();
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setTo(changedEmail);
			bundle.setContent(subject, body);

			MailerResult result = mailManager.sendMessage(bundle);
			boolean isMailSent = result.isSuccessful();
			if (isMailSent) {
				tk.setMailSent(true);
				// set key
				User user = this.identityToModify.getUser();
				user.setProperty("emchangeKey", tk.getRegistrationKey());
				UserManager.getInstance().updateUser(user);
				getWindowControl().setInfo(translate("email.sent"));
			} else {
				tk.setMailSent(false);
				rm.deleteTemporaryKeyWithId(tk.getRegistrationKey());
				getWindowControl().setError(translate("email.notsent"));
			}
		} catch (Exception e) {
			rm.deleteTemporaryKeyWithId(tk.getRegistrationKey());
			getWindowControl().setError(translate("email.notsent"));
		}
	}

	/**
	 * Load and clean temporary keys with action "EMAIL_CHANGE".
	 * @param serMailMap
	 * @return
	 */
	private TemporaryKey loadCleanTemporaryKey(String serMailMap) {
		TemporaryKey tk = rm.loadTemporaryKeyByEmail(serMailMap);
		if (tk == null) {
			XStream xml = new XStream();
			@SuppressWarnings("unchecked")
			Map<String, String> mails = (Map<String, String>) xml.fromXML(serMailMap);
			String currentEMail = mails.get("currentEMail");
			List<TemporaryKey> tks = rm.loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
			if (tks != null) {
				synchronized (tks) {
					tks = rm.loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
					int countCurrentEMail = 0;
					for (TemporaryKey temporaryKey : tks) {
						@SuppressWarnings("unchecked")
						Map<String, String> tkMails = (Map<String, String>) xml.fromXML(temporaryKey.getEmailAddress());
						if (tkMails.get("currentEMail").equals(currentEMail)) {
							if (countCurrentEMail > 0) {
								// clean
								rm.deleteTemporaryKeyWithId(temporaryKey.getRegistrationKey());
							} else {
								// load
								tk = temporaryKey;
							}
							countCurrentEMail++;
						}
					}
				}
			}
		}
		return tk;
	}

	/**
	 * Sets the dirty mark for this form.
	 * 
	 * @param isDirtyMarking <code>true</code> sets this form dirty.
	 */
	public void setDirtyMarking(boolean isDirtyMarking) {
		mainForm.setDirtyMarking(isDirtyMarking);
	}
}
