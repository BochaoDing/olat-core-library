/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package ch.uzh.campus.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Initial Date: 19.06.2012 <br>
 * 
 * @author aabouc
 */
@Entity
@Table(name = "ck_skip_item")
public class SkipItem {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id;

    @Column(name = "type")
    private String type;

    @Column(name = "item")
    private String item;

    @Column(name = "msg")
    private String msg;

    @Column(name = "job_execution_id")
    private Long jobExecutionId;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "step_execution_id")
    private Long stepExecutionId;

    @Column(name = "step_name")
    private String stepName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "step_start_time")
    private Date stepStartTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public Long getStepExecutionId() {
        return stepExecutionId;
    }

    public void setStepExecutionId(Long stepExecutionId) {
        this.stepExecutionId = stepExecutionId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public Date getStepStartTime() {
        return stepStartTime;
    }

    public void setStepStartTime(Date stepStartTime) {
        this.stepStartTime = stepStartTime;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("type", getType());
        builder.append("item", getItem());
        builder.append("msg", getMsg());
        builder.append("stepStartTime", getStepStartTime());

        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(type);
        builder.append(item);
        builder.append(stepStartTime);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SkipItem))
            return false;
        SkipItem theOther = (SkipItem) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.type, theOther.type);
        builder.append(this.item, theOther.item);
        builder.append(this.stepStartTime, theOther.stepStartTime);

        return builder.isEquals();
    }

}