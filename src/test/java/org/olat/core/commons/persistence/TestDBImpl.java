package org.olat.core.commons.persistence;

import java.util.Properties;

/**
 * @author Martin Schraner
 */
public class TestDBImpl extends DBImpl {

    /**
     * [used by spring]
     */
    public TestDBImpl(Properties databaseProperties) {
        super(databaseProperties);
    }

    @Override
    public void commit() {
        // Do not commit!
        getCurrentEntityManager().flush();
    }

    @Override
    public void intermediateCommit() {
        // Do not commit!
        getCurrentEntityManager().flush();
    }
}