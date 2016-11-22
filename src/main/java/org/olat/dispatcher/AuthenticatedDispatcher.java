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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.dispatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.form.flexible.impl.InvalidRequestParameterException;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.exception.MsgFactory;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.StringHelper;
import org.olat.core.util.URIHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.session.UserSessionManager;
import org.olat.core.util.threadlog.UserBasedLogLevelManager;
import org.olat.login.LoginModule;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 */
public class AuthenticatedDispatcher implements Dispatcher {
	private static final OLog log = Tracing.createLoggerFor(AuthenticatedDispatcher.class);
	
	protected static final String AUTHDISPATCHER_ENTRYURL = "AuthDispatcher:entryUrl";
	protected static final String AUTHDISPATCHER_BUSINESSPATH = "AuthDispatcher:businessPath";
	
	protected static final String QUESTIONMARK = "?";
	protected static final String GUEST = "guest";
	protected static final String INVITATION = "invitation";
	protected static final String TRUE = "true";
	/** forces secure http connection to access olat if set to true **/
	private boolean forceSecureAccessOnly = false;
	private UserBasedLogLevelManager userBasedLogLevelManager = UserBasedLogLevelManager.getInstance();
	
	public AuthenticatedDispatcher(boolean forceSecureAccessOnly) {
		this.forceSecureAccessOnly = forceSecureAccessOnly;
	}

	/**
	 * Main method called by OpenOLATServlet. This processess all requests for
	 * authenticated users.
	 * 
	 * @param request
	 * @param response
	 * @param uriPrefix
	 */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		long startExecute = 0;
		if ( log.isDebug() ) {
			startExecute = System.currentTimeMillis();
		}

		log.audit("AuthenticatedDispatcher.execute(): starting");
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for 
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			//MODE could not be decoded
			//typically if robots with wrong urls hit the system
			//or user have bookmarks
			//or authors copy-pasted links to the content.
			//showing redscreens for non valid URL is wrong instead
			//a 404 message must be shown -> e.g. robots correct their links.
			if(log.isDebug()){
				log.debug("Bad Request "+request.getPathInfo());
			}
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}
		
		boolean auth = usess.isAuthenticated();
		log.audit("AuthenticatedDispatcher.execute(): auth=" + auth);

		if (!auth) {
			if (!ureq.isValidDispatchURI()) {
				// might be a direct jump request -> remember it if not logged in yet
				String reqUri = request.getRequestURI();
				String query = request.getQueryString();
				String allGet = reqUri + QUESTIONMARK + query;
				usess.putEntryInNonClearedStore(AUTHDISPATCHER_ENTRYURL, allGet);
			}
			String guestAccess = ureq.getParameter(GUEST);
			if (guestAccess == null || !CoreSpringFactory.getImpl(LoginModule.class).isGuestLoginEnabled()) {
				if(ServletUtil.acceptJson(request)) {
					try {//TODO xhr
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					} catch (IOException e) {
						log.error("Could not send error 401", e);
					}
				} else {
					DispatcherModule.redirectToDefaultDispatcher(response);
				}
				return;
			} else if (guestAccess.equals(TRUE)) {
				// try to log in as anonymous
				// use the language from the lang parameter if available, otherwise use the system default locale
				String guestLang = ureq.getParameter("language");
				if (guestLang == null) {
					// support for legacy lang parameter
					guestLang = ureq.getParameter("lang");
				}
				Locale guestLoc;
				if (guestLang == null) {
					guestLoc = I18nModule.getDefaultLocale();
				} else {
					guestLoc = I18nManager.getInstance().getLocaleOrDefault(guestLang);
				}
				int loginStatus = AuthHelper.doAnonymousLogin(ureq, guestLoc);
				if ( loginStatus != AuthHelper.LOGIN_OK) {
					if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
						DispatcherModule.redirectToServiceNotAvailable(response);
					}
					DispatcherModule.redirectToDefaultDispatcher(response); // error, redirect to login screen
					return;
				}
				// else now logged in as anonymous user, continue
			}
		}

		// authenticated!
		try {
			
			//kill session if not secured via SSL
			if (forceSecureAccessOnly && !request.isSecure()) {
				SessionInfo sessionInfo = usess.getSessionInfo();
				if (sessionInfo!=null) {
					HttpSession session = sessionInfo.getSession();
					if (session!=null) {
						try{
							session.invalidate();
						} catch(IllegalStateException ise) {
							// thrown when session already invalidated. fine. ignore.
						}
					}
				}
				DispatcherModule.redirectToDefaultDispatcher(response);
				return;
			}
			
			SessionInfo sessionInfo = usess.getSessionInfo();
			if (sessionInfo==null) {
				DispatcherModule.redirectToDefaultDispatcher(response);
				return;
			}
			
			if (userBasedLogLevelManager!=null) userBasedLogLevelManager.activateUsernameBasedLogLevel(sessionInfo.getLogin());
			
			sessionInfo.setLastClickTime();
			String origUrl = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_ENTRYURL);
			if (origUrl != null) {
				// we had a direct jump request
				// to avoid a endless redirect, remove the guest parameter if any
				// this can happen if a guest has cookies disabled
				String url = new URIHelper(origUrl).removeParameter(GUEST).toString();
				DispatcherModule.redirectTo(response, url);
			} else {
				String businessPath = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_BUSINESSPATH);
				if (businessPath != null) {
					processBusinessPath(businessPath, ureq, usess);
				} else if (ureq.isValidDispatchURI()) {
					// valid uri for dispatching (has timestamp, componentid and windowid)
					processValidDispatchURI(ureq, usess, request, response);
				} else {
					final String origUri = request.getRequestURI();
					String restPart = origUri.substring(uriPrefix.length());
					try {
						restPart = URLDecoder.decode(restPart, "UTF8");
					} catch (UnsupportedEncodingException e) {
						log.error("Unsupported encoding", e);
					}
					
					String[] split = restPart.split("/");
					if(restPart.startsWith("repo/go")) {
						businessPath = convertJumpInURL(restPart, ureq);
						processBusinessPath(businessPath, ureq, usess);
					} else if (split.length > 0 && split.length % 2 == 0) {
						businessPath = BusinessControlFactory.getInstance().formatFromURI(restPart);
						processBusinessPath(businessPath, ureq, usess);
					} else {
						log.error("Invalid URI in AuthenticatedDispatcher: " + request.getRequestURI());
					}
				}
			}
		} catch (InvalidRequestParameterException e) {
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IOException e1) {
				log.error("An exception occurred while handling the invalid request parameter exception...", e1);
			}
		} catch (Throwable th) {
			// Do not log as Warn or Error here, log as ERROR in MsgFactory => ExceptionWindowController throws an OLATRuntimeException 
			log.error("(@TODO REMOVE THIS ERROR FROM LOG) handleError in AuthenticatedDispatcher.execute(): throwable=" + th);
			log.debug("handleError in AuthenticatedDispatcher throwable=" + th);
			DispatcherModule.handleError();
			ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
			// the controller's window must be failsafe also
			msgcc.getWindow().dispatchRequest(ureq, true);
			// do not dispatch (render only), since this is a new Window created as
			// a result of another window's click.
		} finally {
			if (userBasedLogLevelManager!=null) userBasedLogLevelManager.deactivateUsernameBasedLogLevel();
			if ( log.isDebug() ) {
				long durationExecute = System.currentTimeMillis() - startExecute;
				log.debug("Perf-Test: durationExecute=" + durationExecute);
			}
		}
	}
	
	/**
	 * http://localhost:8080/olat/auth/repo/go?rid=819242&amp;par=77013818723561
	 * @param requestPart
	 * @param ureq
	 * @return
	 */
	private String convertJumpInURL(String requestPart, UserRequest ureq) {
		String repoId = ureq.getParameter("rid");
		String businessPath = "[RepositoryEntry:" + repoId + "]";
		String par = ureq.getParameter("par");
		if(StringHelper.containsNonWhitespace(par) && StringHelper.isLong(par)) {
			try {
				Long parLong = Long.parseLong(par);
				businessPath += "[Part:" + parLong + "]";
			} catch(NumberFormatException e) {
				//it can happen
			}
		}
		return businessPath;				
	}
	
	private void processValidDispatchURI(UserRequest ureq, UserSession usess, HttpServletRequest request, HttpServletResponse response) {
		Windows ws = Windows.getWindows(ureq);
		Window window = ws.getWindow(ureq);
		if (window == null) {
			//probably a 
			if(usess.isSavedSession() && !usess.getHistoryStack().isEmpty()) {
				DispatcherModule.redirectToDefaultDispatcher(response);
			} else {
				DispatcherModule.sendNotFound(request.getRequestURI(), response);
			}
		} else {
			long startDispatchRequest = 0;
			if (log.isDebug()) {
				startDispatchRequest = System.currentTimeMillis();
			}
			window.dispatchRequest(ureq);
			if ( log.isDebug() ) {
				long durationDispatchRequest = System.currentTimeMillis() - startDispatchRequest;
				log.debug("Perf-Test: window=" + window);
				log.debug("Perf-Test: durationDispatchRequest=" + durationDispatchRequest);
			}
		}
	}
	
	private boolean processBusinessPath(String businessPath, UserRequest ureq, UserSession usess) {
		WindowBackOffice windowBackOffice = Windows.getWindows(usess).getChiefController().getWindow().getWindowBackOffice();

		String wSettings = (String) usess.removeEntryFromNonClearedStore(WINDOW_SETTINGS);
		if(wSettings != null) {
			WindowSettings settings = WindowSettings.parse(wSettings);
			windowBackOffice.setWindowSettings(settings);
		}
		
		try {
			BusinessControl bc = null;
			String historyPointId = ureq.getHttpReq().getParameter("historyPointId");
			if(StringHelper.containsNonWhitespace(historyPointId)) {
				HistoryPoint point = ureq.getUserSession().getHistoryPoint(historyPointId);
				bc = BusinessControlFactory.getInstance().createFromContextEntries(point.getEntries());
			}
			if(bc == null) {
				bc = BusinessControlFactory.getInstance().createFromString(businessPath);
			}

			WindowControl wControl = windowBackOffice.getChiefController().getWindowControl();
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, wControl);
			NewControllerFactory.getInstance().launch(ureq, bwControl);	
			// render the window
			Window w = windowBackOffice.getWindow();
			log.audit("Ready to dispatch window request " + businessPath);
			w.dispatchRequest(ureq, true); // renderOnly
			log.audit("Finished to dispatch window request " + businessPath);
			return true;
		} catch (Exception e) {
			log.error("Error while processing business path " + businessPath, e);
			return false;
		}
	}
}