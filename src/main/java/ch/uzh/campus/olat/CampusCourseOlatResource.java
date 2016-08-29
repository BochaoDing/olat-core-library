package ch.uzh.campus.olat;

import org.olat.core.id.Persistable;
import org.olat.resource.OLATResource;

import java.util.Date;

import static ch.uzh.campus.olat.CampusCourseBeanFactory.NOT_CREATED_CAMPUSKURS_RESOURCE_ID;
import static ch.uzh.campus.olat.CampusCourseBeanFactory.RESOURCEABLE_TYPE_NAME;

/**
 * Initial date: 2016-08-05<br />
 * @author sev26 (UZH)
 */
final class CampusCourseOlatResource implements OLATResource {

	private final Long key;
	private final String resourceableTypeName;

	public CampusCourseOlatResource(Long key, String resourceableTypeName) {
		assert key != null;
		assert resourceableTypeName != null;
		this.key = key;
		this.resourceableTypeName = resourceableTypeName;
	}

	@Override
	public Date getCreationDate() {
		return null;
	}

	@Override
	public String getResourceableTypeName() {
		return resourceableTypeName;
	}

	@Override
	public Long getResourceableId() {
		return NOT_CREATED_CAMPUSKURS_RESOURCE_ID;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return this == persistable ||
				(this.getClass() == persistable.getClass() && getKey().equals(persistable.getKey()));
	}
}