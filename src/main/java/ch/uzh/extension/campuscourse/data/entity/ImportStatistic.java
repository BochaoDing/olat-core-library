package ch.uzh.extension.campuscourse.data.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;


/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_import_statistic")
@NamedQueries({
        @NamedQuery(name = ImportStatistic.GET_LAST_COMPLETED_IMPORT_STATISTIC, query = "select s from ImportStatistic s where s.stepName like 'import_%' and s.status='COMPLETED'"
        + " and s.startTime >= (select max(s2.startTime) from ImportStatistic s2 where s2.stepName='import_controlFile')"),
        @NamedQuery(name = ImportStatistic.GET_IMPORT_STATISTICS_OF_TODAY, query = "select s from ImportStatistic s where s.stepName like 'import_%' and s.status='COMPLETED'"
        + " and s.startTime >= :midnight")
})
public class ImportStatistic {

    public static final String GET_LAST_COMPLETED_IMPORT_STATISTIC = "getLastCompletedImportStatistic";
    public static final String GET_IMPORT_STATISTICS_OF_TODAY = "getImportStatisticOfToday";

    public ImportStatistic() {
    }

    public ImportStatistic(long stepId, String stepName, String status, Date startTime, Date endTime, int readCount,
                           int writeCount, int readSkipCount, int writeSkipCount, int processSkipCount, int commitCount,
                           int rollbackCount) {
        this.stepId = stepId;
        this.stepName = stepName;
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

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id;

    @Column(name = "step_id")
    private long stepId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "status", nullable = false)
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "read_count", nullable = false)
    private int readCount;

    @Column(name = "write_count", nullable = false)
    private int writeCount;

    @Column(name = "read_skip_count")
    private int readSkipCount;

    @Column(name = "write_skip_count")
    private int writeSkipCount;

    @Column(name = "process_skip_count")
    private int processSkipCount;

    @Column(name = "commit_count")
    private int commitCount;

    @Column(name = "rollback_count")
    private int rollbackCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getStepId() {
        return stepId;
    }

    public void setStepId(long stepId) {
        this.stepId = stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
        builder.append("stepId", getStepId());
        builder.append("stepName", getStepName());
        builder.append("startTime", getStartTime());
        builder.append("endTime", getEndTime());
        builder.append("readCount", getReadCount());
        builder.append("writeCount", getWriteCount());
        builder.append("readSkipCount", getReadSkipCount());
        builder.append("writeSkipCount", getWriteSkipCount());
        builder.append("processkipCount", getProcessSkipCount());
        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(stepId);
        builder.append(stepName);
        builder.append(startTime);
        builder.append(endTime);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ImportStatistic))
            return false;
        ImportStatistic theOther = (ImportStatistic) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.stepId, theOther.stepId);
        builder.append(this.stepName, theOther.stepName);
        builder.append(this.startTime, theOther.startTime);
        builder.append(this.endTime, theOther.endTime);

        return builder.isEquals();
    }

}
