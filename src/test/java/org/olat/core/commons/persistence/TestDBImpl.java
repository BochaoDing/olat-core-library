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
        flush();
    }

    @Override
    public void intermediateCommit() {
        // Do not commit!
        flush();
    }

    @Override
    public void commitAndCloseSession() {
        // Do not commit!
        flush();
    }

    @Override
    public void closeSession() {
        // Do not commit!
        flush();
    }
}