package org.olat.core.commons.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;

/**
 * DB implementation for tests, which does not commit.
 *
 * @author Martin Schraner
 */
@Component("database")
@Primary
public class TestDBImpl extends DBImpl {

    /**
     * [used by spring]
     */
    @Autowired
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