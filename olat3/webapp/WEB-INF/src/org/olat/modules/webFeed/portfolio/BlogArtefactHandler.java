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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.modules.webFeed.portfolio;

import java.io.InputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
import org.olat.portfolio.EPAbstractHandler;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.search.service.SearchResourceContext;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Description:<br>
 * Artefact handler for blog entry
 * 
 * <P>
 * Initial Date:  3 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BlogArtefactHandler extends EPAbstractHandler<BlogArtefact> {

	@Override
	public String getType() {
		return BlogArtefact.TYPE;
	}

	@Override
	public BlogArtefact createArtefact() {
		return new BlogArtefact();
	}
	
	/**
	 * @see org.olat.portfolio.EPAbstractHandler#prefillArtefactAccordingToSource(org.olat.portfolio.model.artefacts.AbstractArtefact, java.lang.Object)
	 */
	@Override
	public void prefillArtefactAccordingToSource(AbstractArtefact artefact, Object source) {
		super.prefillArtefactAccordingToSource(artefact, source);
		if (source instanceof Feed) {
			Feed feed = (Feed)source;
			String subPath = getItemUUID(artefact.getBusinessPath());
			for(Item item:feed.getItems()) {
				if(subPath.equals(item.getGuid())) {
					prefillBlogArtefact(artefact, feed, item);
				}
			}
			artefact.setSignature(70); 
		}
	}

	private void prefillBlogArtefact(AbstractArtefact artefact, Feed feed, Item item) {
		VFSContainer itemContainer = FeedManager.getInstance().getItemContainer(item, feed);
		artefact.setFileSourceContainer(itemContainer);
		artefact.setTitle(item.getTitle());
		artefact.setDescription(item.getDescription());

		VFSLeaf itemXml = (VFSLeaf)itemContainer.resolve(BlogArtefact.BLOG_FILE_NAME);
		if(itemXml != null) {
			InputStream in = itemXml.getInputStream();
			String xml = FileUtils.load(in, "UTF-8");
			artefact.setFulltextContent(xml);
			FileUtils.closeSafely(in);
		}
	}

	@Override
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode) {
		BlogArtefactDetailsController ctrl = new BlogArtefactDetailsController(ureq, wControl, (BlogArtefact)artefact, readOnlyMode);
		return ctrl;
	}
	
	@Override
	protected void getContent(AbstractArtefact artefact, StringBuilder sb, SearchResourceContext context, EPFrontendManager ePFManager) {
		String content = ePFManager.getArtefactFullTextContent(artefact);
		if(content != null) {
			try {
				XStream xstream = XStreamHelper.createXStreamInstance();
				xstream.alias("item", Item.class);
				Item item = (Item)xstream.fromXML(content);
				
				String mapperBaseURL = "";
				Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(mapperBaseURL);
				sb.append(mediaUrlFilter.filter(item.getDescription())).append(" ")
					.append(mediaUrlFilter.filter(item.getContent()));
			} catch (Exception e) {
				log.warn("Cannot read an artefact of type blog while idnexing", e);
			}
		}
	}

	private String getItemUUID(String businessPath) {
		int start = businessPath.lastIndexOf("item=");
		int stop = businessPath.lastIndexOf(":0]");
		if(start < stop && start > 0 && stop > 0) {
			return businessPath.substring(start + 5, stop);
		} else {
			return null;
		}
	}
}