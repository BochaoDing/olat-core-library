package org.olat.course.assessment.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.id.Persistable;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;

/**
 * 
 * Initial date: 16.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="courseassessmentmodetogroup")
@Table(name="o_as_mode_course_to_group")
public class AssessmentModeToGroupImpl implements Persistable, AssessmentModeToGroup {

	private static final long serialVersionUID = 7749398652513102007L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@ManyToOne(targetEntity=AssessmentModeImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_assessment_mode_id", nullable=false, insertable=true, updatable=false)
	private AssessmentMode assessmentMode;
	
	@ManyToOne(targetEntity=BusinessGroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private BusinessGroup businessGroup;

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}

	public void setAssessmentMode(AssessmentMode assessmentMode) {
		this.assessmentMode = assessmentMode;
	}

	@Override
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public void setBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -6765682 : getKey().hashCode();
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssessmentModeToGroupImpl) {
			AssessmentModeToGroupImpl mode = (AssessmentModeToGroupImpl)obj;
			return getKey() != null && getKey().equals(mode.getKey());	
		}
		return false;
	}
}