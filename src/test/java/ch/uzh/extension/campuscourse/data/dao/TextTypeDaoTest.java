package ch.uzh.extension.campuscourse.data.dao;

import ch.uzh.extension.campuscourse.CampusCourseTestCase;
import ch.uzh.extension.campuscourse.data.entity.TextType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.junit.Assert.*;

/**
 * @author Martin Schraner
 */
@Component
public class TextTypeDaoTest extends CampusCourseTestCase {

	@Autowired
	private TextTypeDao textTypeDao;

	private TextType textType1;
	private TextType textType2;

	@Before
	public void before() {
		insertTestData();
	}

	@Test
	public void testGetTextTypeById()  {
		TextType textTypeFound = textTypeDao.getTextTypeById(999999991);
		assertNotNull(textTypeFound);
		assertEquals(textType1.getId(), textTypeFound.getId());
	}

	@Test
	public void testGetTextTypeByName() {
		TextType textTypeFound = textTypeDao.getTextTypeByName("type name 2");
		assertNotNull(textTypeFound);
		assertEquals(textType2.getId(), textTypeFound.getId());

		// Not existing name
		textTypeFound = textTypeDao.getTextTypeByName("gugus");
		assertNull(textTypeFound);
	}

	private void insertTestData() {
		textType1 = new TextType(999999991, "type name 1");
		textTypeDao.save(textType1);
		dbInstance.flush();
		textType2 = new TextType(999999992, "type name 2");
		textTypeDao.save(textType2);
		dbInstance.flush();
	}

}