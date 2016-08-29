package ch.uzh.campus.data;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

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
@NamedQueries({ @NamedQuery(name = Text.GET_IDS_OF_ALL_TEXTS, query = "select t.id from Text t"),
        @NamedQuery(name = Text.GET_TEXTS_BY_COURSE_ID_AND_TYPE, query = "select t from Text t where t.course.id = :courseId and t.type = :type order by t.lineSeq asc"),
        @NamedQuery(name = Text.GET_TEXT_IDS_BY_COURSE_ID, query = "select t.id from Text t where t.course.id = :courseId"),
        @NamedQuery(name = Text.GET_TEXTS_BY_COURSE_ID, query = "select t from Text t where t.course.id = :courseId"),
        @NamedQuery(name = Text.GET_TEXT_IDS_BY_COURSE_IDS, query = "select t.id from Text t where t.course.id in :courseIds"),
        @NamedQuery(name = Text.DELETE_ALL_TEXTS, query = "delete from Text t"),
        @NamedQuery(name = Text.DELETE_TEXTS_BY_COURSE_IDS, query = "delete from Text t where t.course.id in :courseIds")
})
public class Text {

    @Id   
    @GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "line_seq", nullable = false)
    private int lineSeq;
    
    @Column(name = "line", nullable = false)
    private String line;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_of_import", nullable = false)
    private Date dateOfImport;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public Text() {
    }

    public Text(String type, int lineSeq, String line, Date dateOfImport) {
        this.type = type;
        this.lineSeq = lineSeq;
        this.line = line;
        this.dateOfImport = dateOfImport;
    }

    static final String CONTENTS = "Veranstaltungsinhalt";
    static final String INFOS = "Hinweise";
    static final String MATERIALS = "Unterrichtsmaterialien";
    static final String BREAK_TAG = "<br>";

    static final String GET_IDS_OF_ALL_TEXTS = "getIdsOfAllTexts";
    static final String GET_TEXT_IDS_BY_COURSE_ID = "getTextIdsByCourseId";
    static final String GET_TEXT_IDS_BY_COURSE_IDS = "getTextIdsByCourseIds";
    static final String GET_TEXTS_BY_COURSE_ID_AND_TYPE = "getTextsByCourseIdAndType";
    static final String GET_TEXTS_BY_COURSE_ID ="getTextsByCourseId";
    static final String DELETE_ALL_TEXTS = "deleteAllTexts";
    static final String DELETE_TEXTS_BY_COURSE_IDS = "deleteTextsByCourseIds";

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

    public void setDateOfImport(Date modifiedDate) {
        this.dateOfImport = modifiedDate;
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
        builder.append("id", getId());
        builder.append("courseId", course.getId());
        builder.append("type", getType());
        builder.append("lineSeq", getLineSeq());
        builder.append("line", getLine());

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Text text = (Text) o;

        if (lineSeq != text.lineSeq) return false;
        if (!type.equals(text.type)) return false;
        return line.equals(text.line);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + lineSeq;
        result = 31 * result + line.hashCode();
        return result;
    }
}