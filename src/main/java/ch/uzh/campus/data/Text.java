package ch.uzh.campus.data;

import java.util.Date;

import javax.persistence.*;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;



/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author aabouc
 * @author lavinia
 * @author Martin Schraner
 */
@Entity
@Table(name = "ck_text")
@NamedQueries({ @NamedQuery(name = Text.DELETE_ALL_TEXTS, query = "delete from Text"),
        @NamedQuery(name = Text.GET_TEXTS_BY_COURSE_ID_AND_TYPE, query = "select t from Text t where t.course.id = :courseId and t.type = :type order by t.id, t.lineSeq asc"),
        @NamedQuery(name = Text.DELETE_TEXTS_BY_COURSE_ID, query = "delete from Text t where t.course.id = :courseId"),
        @NamedQuery(name = Text.GET_TEXTS_BY_COURSE_ID, query = "select t from Text t where t.course.id = :courseId"),
        @NamedQuery(name = Text.DELETE_TEXTS_BY_COURSE_IDS, query = "delete from Text t where t.course.id in :courseIds") })
public class Text {
    @Id   
    @GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
    private Long id;

    @Column(name = "type")
    private String type;
    
    @Column(name = "line_seq")
    private int lineSeq;
    
    @Column(name = "line")
    private String line;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date")
    private Date modifiedDate;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public static final String CONTENTS = "Veranstaltungsinhalt";
    public static final String INFOS = "Hinweise";
    public static final String MATERIALS = "Unterrichtsmaterialien";
    public static final String BREAK_TAG = "<br>";

    public static final String DELETE_ALL_TEXTS = "deleteAllTexts";
    public static final String DELETE_TEXTS_BY_COURSE_ID = "deleteTextsByCourseId";
    public static final String DELETE_TEXTS_BY_COURSE_IDS = "deleteTextsByCourseIds";
    public static final String GET_TEXTS_BY_COURSE_ID_AND_TYPE = "getTextsByCourseIdAndType";
    public static final String GET_TEXTS_BY_COURSE_ID ="getTextsByCourseId";

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

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
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
