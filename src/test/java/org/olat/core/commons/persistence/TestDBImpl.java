package org.olat.core.commons.persistence;

import javax.persistence.EntityManagerFactory;
import java.util.Properties;

/**
 * DB implementation for tests, which does not commit.
 *
 * @author Martin Schraner
 */
public class TestDBImpl extends DBImpl {

	public TestDBImpl(Properties databaseProperties) {
		super(databaseProperties);
	}

    public TestDBImpl(EntityManagerFactory emf) {
        super(emf);
    }

    @Override
    public void commit() {
        flush();  // Instead of commit()
    }

    @Override
    public void intermediateCommit() {
        flush();  // Instead of commit()
        clear();  // Instead of closeSession()
    }

    @Override
    public void commitAndCloseSession() {
        flush();  // Instead of commit()
        clear();  // Instead of closeSession()
    }

    @Override
    public void closeSession() {
        flush();  // Instead of commit()
        clear();  // Instead of closeSession()
    }

    @Override
	public void commitTransactionAndCloseEntityManager() {
    	flush();  // Instead of commit()
		clear();  // Instead of closeSession()
	}

	@Override
	public void rollbackAndCloseSession() {
		rollbackTransactionAndCloseEntityManager();
	}
}