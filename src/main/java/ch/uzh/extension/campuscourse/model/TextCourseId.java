package ch.uzh.extension.campuscourse.model;

import java.util.Date;

public class TextCourseId {

    private String type;
    private int lineSeq;
    private String line;
    private Date dateOfImport;

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

    private Long courseId;


}
