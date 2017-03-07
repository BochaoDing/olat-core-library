package ch.uzh.extension.campuscourse;

import org.junit.After;
import org.olat.test.MockServletContextWebContextLoader;
import org.olat.test.OlatTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Martin Schraner
 */
@ContextConfiguration(loader = MockServletContextWebContextLoader.class, locations = {
        "classpath:org/olat/core/commons/persistence/_spring/testDatabaseCorecontext.xml"})
public abstract class CampusCourseTestCase extends OlatTestCase {

    @Override
    @After
    public void closeConnectionAfter() {
        dbInstance.rollback();
    }
}