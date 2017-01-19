package ch.uzh.extension.campuscourse.data.entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.Date;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 */
@Entity
@Table(name = "ck_export")
@Repository
@Scope("prototype")
public class Export {

    public static final int SAP_IMPORT_FILES_TO_BE_PROCESSED = 7;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp", nullable = false)
    private Date creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "export_date", nullable = false)
    private Date exportDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExportDate() {
        return exportDate;
    }

    public void setExportDate(Date exportDate) {
        this.exportDate = exportDate;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("fileName", getFileName());
        builder.append("exportDate", getExportDate());
        builder.append("creationDate", getCreationDate());

        return builder.toString();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1239, 5475);
        builder.append(fileName);
        builder.append(exportDate);
        builder.append(creationDate);

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Export))
            return false;
        Export theOther = (Export) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.fileName, theOther.fileName);
        builder.append(this.exportDate, theOther.exportDate);
        builder.append(this.creationDate, theOther.creationDate);

        return builder.isEquals();
    }

}
