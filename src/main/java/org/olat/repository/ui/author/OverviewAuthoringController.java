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
package org.olat.repository.ui.author;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewAuthoringController extends BasicController implements Activateable2 {
	
	private MainPanel mainPanel;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link favoriteLink, myEntriesLink, searchLink;
	private AuthorListController markedCtrl, myEntriesCtrl, searchEntriesCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public OverviewAuthoringController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		
		mainPanel = new MainPanel("authoringMainPanel");
		mainPanel.setDomReplaceable(false);
		mainVC = createVelocityContainer("overview");
		mainPanel.setContent(mainVC);

		boolean markEmpty = doOpenMark(ureq).isEmpty();
		if(markEmpty) {
			doOpenMyEntries(ureq);
		}
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		favoriteLink = LinkFactory.createLink("search.mark", mainVC, this);
		segmentView.addSegment(favoriteLink, !markEmpty);
		myEntriesLink = LinkFactory.createLink("search.my", mainVC, this);
		segmentView.addSegment(myEntriesLink, markEmpty);
		searchLink = LinkFactory.createLink("search.generic", mainVC, this);
		segmentView.addSegment(searchLink, markEmpty);

		putInitialPanel(mainPanel);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String segment = entry.getOLATResourceable().getResourceableTypeName();
		List<ContextEntry> subEntries = entries.subList(1, entries.size());
		if("Favorits".equals(segment)) {
			doOpenMark(ureq).activate(ureq, subEntries, entry.getTransientState());
			segmentView.select(favoriteLink);
		} else if("My".equals(segment)) {
			doOpenMyEntries(ureq).activate(ureq, subEntries, entry.getTransientState());
			segmentView.select(myEntriesLink);
		} else if("Search".equals(segment)) {
			doSearchEntries(ureq).activate(ureq, subEntries, entry.getTransientState());
			segmentView.select(searchLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == favoriteLink) {
					doOpenMark(ureq);
				} else if (clickedLink == myEntriesLink) {
					doOpenMyEntries(ureq);
				} else if (clickedLink == searchLink) {
					doSearchEntries(ureq);
				}
			}
		}
	}

	private AuthorListController doOpenMark(UserRequest ureq) {
		if(markedCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			searchParams.setMarked(Boolean.TRUE);

			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Favorits", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			markedCtrl = new AuthorListController(ureq, bwControl, "search.mark", searchParams);
			listenTo(markedCtrl);
		}
		
		addToHistory(ureq, markedCtrl);
		mainVC.put("segmentCmp", markedCtrl.getStackPanel());
		return markedCtrl;
	}
	
	private AuthorListController doOpenMyEntries(UserRequest ureq) {
		if(myEntriesCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());

			OLATResourceable ores = OresHelper.createOLATResourceableInstance("My", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			myEntriesCtrl = new AuthorListController(ureq, bwControl, "search.my", searchParams);
			listenTo(myEntriesCtrl);
		}

		addToHistory(ureq, myEntriesCtrl);
		mainVC.put("segmentCmp", myEntriesCtrl.getStackPanel());
		return myEntriesCtrl;
	}
	
	private AuthorListController doSearchEntries(UserRequest ureq) {
		if(searchEntriesCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());

			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			searchEntriesCtrl = new AuthorListController(ureq, bwControl, "search.generic", searchParams);
			listenTo(searchEntriesCtrl);
		}
		
		addToHistory(ureq, searchEntriesCtrl);
		mainVC.put("segmentCmp", searchEntriesCtrl.getStackPanel());
		return searchEntriesCtrl;
	}
}