package ch.uzh.campus.data;


import org.apache.commons.lang.builder.ToStringBuilder;

public class TextCourseId extends Text {

    private Long courseId;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("courseId", getCourseId());
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

        if (getLineSeq() != text.getLineSeq()) return false;
        if (!getType().equals(text.getType())) return false;
        return getLine().equals(text.getLine());

    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getLineSeq();
        result = 31 * result + getLine().hashCode();
        return result;
    }
}
