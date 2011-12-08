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
* frentix GmbH, Switzerland, http://www.frentix.com
* <p>
*/

package org.olat.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * Description:<br>
 * Only a map which logged the current work of threads
 * 
 * <P>
 * Initial Date:  6 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class WorkThreadInformations {
	
	private static final Map<String,String> works = new HashMap<String,String>();
	
	public synchronized static void set(String message) {
		String threadName = Thread.currentThread().getName();
		if(StringHelper.containsNonWhitespace(message)) {
			works.put(threadName, message);
		} else {
			works.remove(threadName);
		}
	}
	
	public synchronized static void unset() {
		String threadName = Thread.currentThread().getName();
		works.remove(threadName);
	}
	
	public synchronized static String get(String threadName) {
		return works.get(threadName);
	}
	
	public synchronized static void currentThreadNames(List<String> threadNames) {
		for(Iterator<String> threadNameIt=works.keySet().iterator(); threadNameIt.hasNext(); ) {
			if(!threadNames.contains(threadNameIt.next())) {
				threadNameIt.remove();
			}
		}
	}
}
