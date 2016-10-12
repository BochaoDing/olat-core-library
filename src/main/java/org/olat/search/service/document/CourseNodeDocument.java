package org.olat.search.service.document;

import org.apache.lucene.document.Document;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.nodes.CourseNode;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class CourseNodeDocument extends OlatDocument {

	private static final long serialVersionUID = -2035945166792451137L;
	private static final OLog log = Tracing.createLoggerFor(CourseNodeDocument.class);

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE = "type.course.node";
	
	private CourseNodeDocument() {
		super();
	}
	
	public static Document createDocument(SearchResourceContext searchResourceContext, CourseNode courseNode) {
		CourseNodeDocument courseNodeDocument = new CourseNodeDocument();	

		// Set all know attributes
		courseNodeDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		if (searchResourceContext.getDocumentType() != null && !searchResourceContext.getDocumentType().equals("") ) {
			courseNodeDocument.setDocumentType(searchResourceContext.getDocumentType());
		} else {
		  courseNodeDocument.setDocumentType(TYPE);
		}
		courseNodeDocument.setCssIcon("o_course_icon");
		courseNodeDocument.setTitle(courseNode.getShortTitle());
		courseNodeDocument.setDescription(courseNode.getLongTitle());
		// Get dates from paraent object via context because course node has no dates 
		courseNodeDocument.setCreatedDate(searchResourceContext.getCreatedDate());
		courseNodeDocument.setLastChange(searchResourceContext.getLastModified());
		courseNodeDocument.setParentContextType(searchResourceContext.getParentContextType());
		courseNodeDocument.setParentContextName(searchResourceContext.getParentContextName());
    // unused course-node attributtes
		//	courseNode.getShortYearShortSemesterName();
    //	courseNode.getType();

		if (log.isDebug()) log.debug(courseNodeDocument.toString());
		return courseNodeDocument.getLuceneDocument();
	}

}
