package ch.uzh.campus.service.core.impl.creator;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.PublishProcess;
import org.olat.course.tree.CourseEditorTreeModel;



/**
 * Initial Date: 11.06.2012 <br>
 * 
 * @author cg
 */
public class CoursePublishHelper {

    public static void publish(ICourse course, Locale locale, Identity publisherIdentity, List<String> publishNodeIds) {
        CourseFactory.openCourseEditSession(course.getResourceableId());
        CourseEditorTreeModel cetm = course.getEditorTreeModel();
        PublishProcess pp = PublishProcess.getInstance(course, cetm, locale);
        pp.createPublishSetFor(publishNodeIds);
        //StatusDescription[] sds = pp.testPublishSet(locale); //not used
        // OLD Comment, exist before campuskurs-refactoring
        // final boolean isValid = sds.length == 0;
        // if (!isValid) {
        // // no error and no warnings -> return immediate
        // }
        pp.applyPublishSet(publisherIdentity, locale);
        CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
    }

}

