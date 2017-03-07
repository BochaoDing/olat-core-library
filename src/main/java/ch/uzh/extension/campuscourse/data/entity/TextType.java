package ch.uzh.extension.campuscourse.data.entity;

import javax.persistence.*;

import static ch.uzh.extension.campuscourse.data.entity.TextType.GET_TEXT_TYPE_BY_NAME;

/**
 * @author Martin Schraner
 */
@Entity
@Table(name = "ck_text_type")
@NamedQueries({
		@NamedQuery(name = GET_TEXT_TYPE_BY_NAME, query = "select t from TextType t where name = :name")
})
public class TextType {

	public static final String CONTENTS = "Veranstaltungsinhalt";
	public static final String INFOS = "Hinweise";
	public static final String MATERIALS = "Unterrichtsmaterialien";
	public static final String GET_TEXT_TYPE_BY_NAME = "getTextTypeByName";

	@Id
	private int id;

	@Column(name = "name", nullable = false)
	private String name;

	public TextType() {
	}

	public TextType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
