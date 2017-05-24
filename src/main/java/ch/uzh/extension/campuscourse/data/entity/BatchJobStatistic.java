package ch.uzh.extension.campuscourse.data.entity;

import ch.uzh.extension.campuscourse.batchprocessing.CampusBatchStepName;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;

import javax.persistence.*;
import java.util.Date;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "ck_batch_job_statistic")
public abstract class BatchJobStatistic {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "step_name", nullable = false)
    private CampusBatchStepName campusBatchStepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BatchStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time", nullable = false)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time", nullable = false)
    private Date endTime;

    @Column(name = "read_count", nullable = false)
    private int readCount;

    @Column(name = "write_count", nullable = false)
    private int writeCount;

    @Column(name = "read_skip_count", nullable = false)
    private int readSkipCount;

    @Column(name = "write_skip_count", nullable = false)
    private int writeSkipCount;

    @Column(name = "process_skip_count", nullable = false)
    private int processSkipCount;

    @Column(name = "commit_count", nullable = false)
    private int commitCount;

    @Column(name = "rollback_count", nullable = false)
    private int rollbackCount;

	public BatchJobStatistic() {
	}

	public BatchJobStatistic(CampusBatchStepName campusBatchStepName, BatchStatus status, Date startTime, Date endTime,
							 int readCount, int writeCount, int readSkipCount, int writeSkipCount, int processSkipCount,
							 int commitCount, int rollbackCount) {
		this.campusBatchStepName = campusBatchStepName;
		this.status = status;
		this.startTime = startTime;
		this.endTime = endTime;
		this.readCount = readCount;
		this.writeCount = writeCount;
		this.readSkipCount = readSkipCount;
		this.writeSkipCount = writeSkipCount;
		this.processSkipCount = processSkipCount;
		this.commitCount = commitCount;
		this.rollbackCount = rollbackCount;
	}

	public BatchJobStatistic(CampusBatchStepName campusBatchStepName, StepExecution stepExecution) {
		setBatchJobStatistic(campusBatchStepName, stepExecution);
	}

	public void setBatchJobStatistic(CampusBatchStepName campusBatchStepName, StepExecution stepExecution) {
    	this.campusBatchStepName = campusBatchStepName;
		status = stepExecution.getStatus();
		readCount = stepExecution.getReadCount();
		readSkipCount = stepExecution.getReadSkipCount();
		writeCount = stepExecution.getWriteCount();
		writeSkipCount = stepExecution.getWriteSkipCount();
		processSkipCount = stepExecution.getProcessSkipCount();
		commitCount = stepExecution.getCommitCount();
		rollbackCount = stepExecution.getRollbackCount();
		startTime = stepExecution.getStartTime();
		endTime = stepExecution.getLastUpdated();
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public CampusBatchStepName getCampusBatchStepName() {
		return campusBatchStepName;
	}

	public void setCampusBatchStepName(CampusBatchStepName campusBatchStepName) {
		this.campusBatchStepName = campusBatchStepName;
	}

	public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public int getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(int writeCount) {
        this.writeCount = writeCount;
    }

    public int getReadSkipCount() {
        return readSkipCount;
    }

    public void setReadSkipCount(int readSkipCount) {
        this.readSkipCount = readSkipCount;
    }

    public int getWriteSkipCount() {
        return writeSkipCount;
    }

    public void setWriteSkipCount(int writeSkipCount) {
        this.writeSkipCount = writeSkipCount;
    }

    public int getProcessSkipCount() {
        return processSkipCount;
    }

    public void setProcessSkipCount(int processSkipCount) {
        this.processSkipCount = processSkipCount;
    }

    public int getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(int commitCount) {
        this.commitCount = commitCount;
    }

    public int getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(int rollbackCount) {
        this.rollbackCount = rollbackCount;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("campusBatchStepName", getCampusBatchStepName().getName());
        builder.append("startTime", getStartTime());
        builder.append("endTime", getEndTime());
        builder.append("readCount", getReadCount());
        builder.append("writeCount", getWriteCount());
        builder.append("readSkipCount", getReadSkipCount());
        builder.append("writeSkipCount", getWriteSkipCount());
        builder.append("processSkipCount", getProcessSkipCount());
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BatchJobStatistic that = (BatchJobStatistic) o;

        if (campusBatchStepName != that.campusBatchStepName) return false;
        return startTime != null ? startTime.equals(that.startTime) : that.startTime == null;
    }

    @Override
    public int hashCode() {
        int result = campusBatchStepName.hashCode();
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        return result;
    }
}
