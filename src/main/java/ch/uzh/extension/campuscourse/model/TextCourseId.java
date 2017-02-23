package ch.uzh.extension.campuscourse.model;

import ch.uzh.extension.campuscourse.data.entity.Text;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
@Scope("prototype")
public class TextCourseId {

	private Long courseId;
	private int textTypeId;
    private String textTypeName;
    private int lineNumber;
    private String line;
    private Date dateOfLatestImport;


    public TextCourseId() {
    }

    public TextCourseId(Long courseId, int textTypeId, String textTypeName, int lineNumber, String line, Date dateOfLatestImport) {
		this.courseId = courseId;
		this.textTypeId = textTypeId;
		this.textTypeName = textTypeName;
        this.lineNumber = lineNumber;
        this.line = line;
        this.dateOfLatestImport = dateOfLatestImport;
    }

	public Long getCourseId() {
		return courseId;
	}

	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}

	public int getTextTypeId() {
		return textTypeId;
	}

	public void setTextTypeId(int textTypeId) {
		this.textTypeId = textTypeId;
	}

	public String getTextTypeName() {
		return textTypeName;
	}

	public void setTextTypeName(String textTypeName) {
		this.textTypeName = textTypeName;
	}

	public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public Date getDateOfLatestImport() {
        return dateOfLatestImport;
    }

    public void setDateOfLatestImport(Date dateOfLatestImport) {
        this.dateOfLatestImport = dateOfLatestImport;
    }

	public void mergeImportedAttributesInto(Text textToBeUpdated) {
		// all imported attributes, except ids
		textToBeUpdated.setLine(getLine());
		textToBeUpdated.setDateOfLatestImport(getDateOfLatestImport());
	}

}
