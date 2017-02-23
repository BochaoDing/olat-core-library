package ch.uzh.extension.campuscourse.data.entity;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;


/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@Entity
@Table(name = "ck_text")
@IdClass(TextId.class)
@NamedQueries({ @NamedQuery(name = Text.GET_ALL_NOT_UPDATED_TEXTS_OF_CURRENT_IMPORT_PROCESS, query =
				"select new ch.uzh.extension.campuscourse.model.CourseIdTextTypeIdLineNumber(t.course.id, t.textType.id, t.lineNumber) from Text t where " +
				"t.dateOfLatestImport < :lastDateOfImport " +
				"and t.course.semester.id = :semesterIdOfCurrentImportProcess"),
        @NamedQuery(name = Text.GET_TEXTS_BY_COURSE_ID, query = "select t from Text t where " +
				"t.course.id = :courseId"),
		@NamedQuery(name = Text.GET_TEXTS_BY_COURSE_ID_AND_TEXT_TYPE_NAME, query = "select t from Text t where " +
				"t.course.id = :courseId " +
				"and t.textType.name = :textTypeName order by t.lineNumber asc"),
		@NamedQuery(name = Text.DELETE_TEXTS_BY_COURSE_IDS, query = "delete from Text t where " +
				"t.course.id in :courseIds"),
		@NamedQuery(name = Text.DELETE_TEXTS_BY_COURSE_ID_TEXT_TYPE_ID_LINE_NUMBER, query = "delete from Text t where " +
				"t.course.id = :courseId " +
				"and t.textType.id = :textTypeId " +
				"and t.lineNumber = :lineNumber"),
		@NamedQuery(name = Text.DELETE_ALL_TEXTS_TOO_FAR_IN_THE_PAST, query = "delete from Text t where " +
				"t.dateOfLatestImport < :nYearsInThePast"),
		@NamedQuery(name = Text.DELETE_ALL_TEXTS_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED, query = "delete from Text t where " +
				"t.dateOfLatestImport < :nYearsInThePast " +
				"and t.course.id not in :courseIdsToBeExcluded"),
})
public class Text {

	public static final String GET_ALL_NOT_UPDATED_TEXTS_OF_CURRENT_IMPORT_PROCESS = "getAllNotUpdatedTextsOfCurrentImportProcess";
	public static final String GET_TEXTS_BY_COURSE_ID ="getTextsByCourseId";
    public static final String GET_TEXTS_BY_COURSE_ID_AND_TEXT_TYPE_NAME = "getTextsByCourseIdAndTextTypeName";
    public static final String DELETE_TEXTS_BY_COURSE_IDS = "deleteTextsByCourseIds";
	public static final String DELETE_TEXTS_BY_COURSE_ID_TEXT_TYPE_ID_LINE_NUMBER = "deleteTextsByCourseIdTextTypeIdLineNumber";
	public static final String DELETE_ALL_TEXTS_TOO_FAR_IN_THE_PAST = "deleteAllTextsTooFarInThePast";
	public static final String DELETE_ALL_TEXTS_TOO_FAR_IN_THE_PAST_EXCEPT_FOR_COURSES_TO_BE_EXCLUDED = "deleteAllTextsTooFarInThePastExceptForCoursesToBeExcluded";

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "fk_course", nullable = false)
	private Course course;

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "fk_text_type", nullable = false)
	private TextType textType;

    @Id
    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @Column(name = "line", nullable = false)
	private String line;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_of_first_import", nullable = false)
	private Date dateOfFirstImport;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_latest_import", nullable = false)
    private Date dateOfLatestImport;

    public Text() {
    }

    public Text(Course course, TextType textType, int lineNumber, String line, Date dateOfLatestImport) {
    	this.course = course;
    	this.textType = textType;
		this.lineNumber = lineNumber;
        this.line = line;
        this.dateOfLatestImport = dateOfLatestImport;
    }

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineSeq) {
		this.lineNumber = lineSeq;
	}

	public TextType getTextType() {
		return textType;
	}

	public void setTextType(TextType textType) {
		this.textType = textType;
	}

	public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

	public Date getDateOfFirstImport() {
		return dateOfFirstImport;
	}

	public void setDateOfFirstImport(Date dateOfFirstImport) {
		this.dateOfFirstImport = dateOfFirstImport;
	}

	public Date getDateOfLatestImport() {
        return dateOfLatestImport;
    }

    public void setDateOfLatestImport(Date modifiedDate) {
        this.dateOfLatestImport = modifiedDate;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("courseId", course.getId());
        builder.append("textTypeId", textType.getId());
		builder.append("lineNumber", getLineNumber());
        builder.append("line", getLine());

        return builder.toString();
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Text text = (Text) o;

		if (lineNumber != text.lineNumber) return false;
		if (!course.equals(text.course)) return false;
		return textType.equals(text.textType);
	}

	@Override
	public int hashCode() {
		int result = course.hashCode();
		result = 31 * result + textType.hashCode();
		result = 31 * result + lineNumber;
		return result;
	}
}
