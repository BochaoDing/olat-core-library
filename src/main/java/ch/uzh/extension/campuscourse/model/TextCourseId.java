package ch.uzh.extension.campuscourse.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
@Scope("prototype")
public class TextCourseId {

    private String type;
    private int lineSeq;
    private String line;
    private Date dateOfImport;
    private Long courseId;

    public TextCourseId() {
    }

    public TextCourseId(String type, int lineSeq, String line, Date dateOfImport, Long courseId) {
        this.type = type;
        this.lineSeq = lineSeq;
        this.line = line;
        this.dateOfImport = dateOfImport;
        this.courseId = courseId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLineSeq() {
        return lineSeq;
    }

    public void setLineSeq(int lineSeq) {
        this.lineSeq = lineSeq;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public Date getDateOfImport() {
        return dateOfImport;
    }

    public void setDateOfImport(Date dateOfImport) {
        this.dateOfImport = dateOfImport;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

}
