package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.data.entity.TextType;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;

/**
 * @author Martin Schraner
 */
@Repository
public class TextTypeDao {

	private static final OLog LOG = Tracing.createLoggerFor(TextTypeDao.class);

	private final DB dbInstance;

	@Autowired
	public TextTypeDao(DB dbInstance) {
		this.dbInstance = dbInstance;
	}

	public void save(TextType textType) {
		dbInstance.saveObject(textType);
	}

	TextType getTextTypeById(int id) {
		return dbInstance.getCurrentEntityManager().find(TextType.class, id);
	}

	TextType getTextTypeByName(String name) {
		try {
			return dbInstance.getCurrentEntityManager()
					.createNamedQuery(TextType.GET_TEXT_TYPE_BY_NAME, TextType.class)
					.setParameter("name", name)
					.getSingleResult();
		} catch (NoResultException e) {
			LOG.warn("No textType found with name '" + name + "'.");
			return null;
		}

	}
}
