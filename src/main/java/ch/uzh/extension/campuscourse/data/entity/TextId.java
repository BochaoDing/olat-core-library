package ch.uzh.extension.campuscourse.data.entity;

import java.io.Serializable;

/**
 * @author Martin Schraner
 */
public class TextId implements Serializable {

	private Long course;
	private int textType;
	private int lineNumber;

	public TextId() {
	}

	public TextId(Long course, int textType, int lineNumber) {
		this.course = course;
		this.textType = textType;
		this.lineNumber = lineNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TextId textId = (TextId) o;

		if (textType != textId.textType) return false;
		if (lineNumber != textId.lineNumber) return false;
		return course.equals(textId.course);
	}

	@Override
	public int hashCode() {
		int result = course.hashCode();
		result = 31 * result + textType;
		result = 31 * result + lineNumber;
		return result;
	}
}
