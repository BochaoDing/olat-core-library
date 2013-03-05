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
package org.olat.modules.qpool.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.modules.qpool.model.SearchQuestionItemParams;

/**
 * 
 * Initial date: 25.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectionOfItemsSource implements QuestionItemsSource {
	
	private final Roles roles;
	private final Identity identity;
	private final QuestionPoolService qpoolService;
	private final QuestionItemCollection collection;
	
	public CollectionOfItemsSource(QuestionItemCollection collection, Identity identity, Roles roles) {
		this.roles = roles;
		this.identity = identity;
		this.collection = collection;
		qpoolService = CoreSpringFactory.getImpl(QuestionPoolService.class);
	}

	@Override
	public int getNumOfItems() {
		return qpoolService.countItemsOfCollection(collection);
	}

	@Override
	public ResultInfos<QuestionItem> getItems(String query, List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		SearchQuestionItemParams params = new SearchQuestionItemParams(identity, roles);
		params.setSearchString(query);
		return qpoolService.getItemsOfCollection(collection, params, firstResult, maxResults);
	}
}
