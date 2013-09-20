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

package org.olat.search.service.indexer.repository.course;

import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiPage;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.WikiPageDocument;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for Wiki course-node.
 * @author Christian Guretzki
 */
public class WikiCourseNodeIndexer extends FolderIndexer implements CourseNodeIndexer {
	private static final OLog log = Tracing.createLoggerFor(WikiCourseNodeIndexer.class);

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to search for certain documenttype and lucene have problems with '_' 
	public final static String TYPE = "type.course.node.wiki";

	private final static String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.WikiCourseNode";
	
	@Override
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) {
		if (log.isDebug()) log.debug("Index wiki...");
		String repoEntryName = "*name not available*";
    try {
  		RepositoryEntry repositoryEntry = courseNode.getReferencedRepositoryEntry();
  		if(repositoryEntry == null) return;
  		repoEntryName = repositoryEntry.getDisplayname();
			Wiki wiki = WikiManager.getInstance().getOrLoadWiki(courseNode.getReferencedRepositoryEntry().getOlatResource());
			// loop over all wiki pages
			List<WikiPage> wikiPageList = wiki.getAllPagesWithContent();
			for (WikiPage wikiPage : wikiPageList) {
			  try {
					SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
					courseNodeResourceContext.setBusinessControlFor(courseNode);
					courseNodeResourceContext.setDocumentType(TYPE);
			    courseNodeResourceContext.setTitle(courseNode.getShortTitle());
			    courseNodeResourceContext.setDescription(courseNode.getLongTitle());
					courseNodeResourceContext.setFilePath(wikiPage.getPageName());

					Document document = WikiPageDocument.createDocument(courseNodeResourceContext, wikiPage);
					indexWriter.addDocument(document);
				} catch (Exception e) {
					log.error("Error indexing wiki page:" + (wikiPage == null ? "null" : wikiPage.getPageName()), e);
				}
			}
		} catch (Exception e) {
			log.error("Error indexing wiki:" + repoEntryName, e);
		}
	}

	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
}
