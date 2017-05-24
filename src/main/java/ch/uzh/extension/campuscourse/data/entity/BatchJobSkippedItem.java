package ch.uzh.extension.campuscourse.data.entity;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.Date;

/**
 * Initial Date: 19.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_batch_job_skipped_item")
public class BatchJobSkippedItem {

	public enum TypeOfBatchProcess {READ, PROCESS, WRITE}

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
			@Parameter(name="sequence_name", value="hibernate_unique_key"),
			@Parameter(name="force_table_use", value="true"),
			@Parameter(name="optimizer", value="legacy-hilo"),
			@Parameter(name="value_column", value="next_hi"),
			@Parameter(name="increment_size", value="32767"),
			@Parameter(name="initial_value", value="32767")
	})
    private Long id;

	@Column(name = "job_name", nullable = false)
	private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_name", nullable = false)
    private CampusBatchStepName campusBatchStepName;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private TypeOfBatchProcess typeOfBatchProcess;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "step_start_time", nullable = false)
	private Date stepStartTime;

    @Column(name = "item")
    private String item;

    @Column(name = "error_message")
    private String errorMessage;

	public BatchJobSkippedItem() {
	}

	public BatchJobSkippedItem(String jobName, CampusBatchStepName campusBatchStepName, TypeOfBatchProcess typeOfBatchProcess, Date stepStartTime, String item, String errorMessage) {
		this.jobName = jobName;
		this.campusBatchStepName = campusBatchStepName;
		this.typeOfBatchProcess = typeOfBatchProcess;
		this.stepStartTime = stepStartTime;
		this.item = item;
		this.errorMessage = errorMessage;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public CampusBatchStepName getCampusBatchStepName() {
		return campusBatchStepName;
	}

	public void setCampusBatchStepName(CampusBatchStepName campusBatchStepName) {
		this.campusBatchStepName = campusBatchStepName;
	}

	public TypeOfBatchProcess getTypeOfBatchProcess() {
		return typeOfBatchProcess;
	}

	public void setTypeOfBatchProcess(TypeOfBatchProcess typeOfBatchProcess) {
		this.typeOfBatchProcess = typeOfBatchProcess;
	}

	public Date getStepStartTime() {
		return stepStartTime;
	}

	public void setStepStartTime(Date stepStartTime) {
		this.stepStartTime = stepStartTime;
	}

	public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String msg) {
        this.errorMessage = msg;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("jobName", getJobName());
		builder.append("campusBatchStepName", getCampusBatchStepName().getName());
		builder.append("type", getTypeOfBatchProcess());
		builder.append("stepStartTime", getStepStartTime());
        builder.append("item", getItem());
        builder.append("msg", getErrorMessage());

        return builder.toString();
    }
}
