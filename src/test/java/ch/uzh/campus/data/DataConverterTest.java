package ch.uzh.campus.data;

/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * Initial Date: Nov 3, 2014 <br>
 * 
 * @author aabouc
 * @author lavinia
 */
@ContextConfiguration(locations = {"classpath:ch/uzh/campus/data/_spring/mockDataContext.xml"})
public class DataConverterTest extends OlatTestCase {

    private DataConverter dataConverterTestObject;

    @Autowired
    private MockDataGenerator dataGenerator;

    private SapOlatUserDao mockSapOlatUserDao;

    private DelegationDao mockDelegationDao;

    private BaseSecurity mockBaseSecurity;

    private Identity id1, id2, id3;

    @Before
    public void setup() throws Exception {
        dataConverterTestObject = new DataConverter();

        mockSapOlatUserDao = mock(SapOlatUserDao.class);
        dataConverterTestObject.sapOlatUserDao = mockSapOlatUserDao;
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(100L)).thenReturn(null);
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(200L)).thenReturn(dataGenerator.getSapOlatUsers().get(1));

        DB mockDB = mock(DB.class);
        dataConverterTestObject.dBImpl = mockDB;
        doNothing().when(mockDB).intermediateCommit();

        id1 = mock(Identity.class);
        id2 = mock(Identity.class);
        id3 = mock(Identity.class);

        mockDelegationDao = mock(DelegationDao.class);
        dataConverterTestObject.delegationDao = mockDelegationDao;

        mockBaseSecurity = mock(BaseSecurity.class);
        dataConverterTestObject.baseSecurity = mockBaseSecurity;
        when(mockBaseSecurity.findIdentityByName(null)).thenReturn(null);
        when(mockBaseSecurity.findIdentityByName("olatUserName1")).thenReturn(id1);
        when(mockBaseSecurity.findIdentityByName("olatUserName2")).thenReturn(id2);

    }

    @Test
    public void testConvertStudentsToIdentities_Empty() {
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(2100L)).thenReturn(null);
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(2200L)).thenReturn(null);
        Course course = getCourseWithStudents();
        
        assertTrue(dataConverterTestObject.convertStudentsToIdentities(course.getStudentCourses()).isEmpty());
    }

    @Test
    public void testConvertStudentsToIdentities_NotEmpty() {
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(2100L)).thenReturn(dataGenerator.getSapOlatUsers().get(0));
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(2200L)).thenReturn(dataGenerator.getSapOlatUsers().get(1));
        
        Course course = getCourseWithStudents();
                   
        assertFalse(dataConverterTestObject.convertStudentsToIdentities(course.getStudentCourses()).isEmpty());
        assertEquals(2, dataConverterTestObject.convertStudentsToIdentities(course.getStudentCourses()).size());
        assertTrue(dataConverterTestObject.convertStudentsToIdentities(course.getStudentCourses()).contains(id1));
        assertTrue(dataConverterTestObject.convertStudentsToIdentities(course.getStudentCourses()).contains(id2));
    }

    /**
     * Adds students to the first course found.
     */
	private Course getCourseWithStudents() {
		Course course = new Course(dataGenerator.getCourseOrgIds().get(0));
		
        List<StudentIdCourseIdDateOfImport> studentIdCourseIdDateOfImports = dataGenerator.getStudentIdCourseIdDateOfImports();
        List<Long> studentIds = new ArrayList<>();
        for (StudentIdCourseIdDateOfImport studentIdCourseIdDateOfImport : studentIdCourseIdDateOfImports) {
        	if (course.getId().equals(studentIdCourseIdDateOfImport.getCourseId())) {
        		studentIds.add(studentIdCourseIdDateOfImport.getStudentId());
        	}
        }
        List<Student> students = dataGenerator.getStudents();
        List<StudentCourse> studentCoursesOfTheCourse = new ArrayList<>();
        for (Student student : students){
        	if (studentIds.contains(student.getId())) {
                StudentCourse studentCourse = new StudentCourse(student, course, new Date());
                studentCoursesOfTheCourse.add(studentCourse);
        	}
        }
        course.getStudentCourses().addAll(studentCoursesOfTheCourse);
		return course;
	}
	
	private Course getCourseWithLecturers() {
		Course course = new Course(dataGenerator.getCourseOrgIds().get(0));
		
        List<LecturerIdCourseIdDateOfImport> lecturerIdCourseIdDateOfImports = dataGenerator.getLecturerIdCourseIdDateOfImports();
        List<Long> lecturerIds = new ArrayList<>();
        for (LecturerIdCourseIdDateOfImport lecturerIdCourseIdDateOfImport : lecturerIdCourseIdDateOfImports) {
        	if (course.getId().equals(lecturerIdCourseIdDateOfImport.getCourseId())) {
        		lecturerIds.add(lecturerIdCourseIdDateOfImport.getLecturerId());
        	}
        }
        List<Lecturer> lecturers = dataGenerator.getLecturers();
        List<LecturerCourse> lecturerCoursesOfTheCourse = new ArrayList<>();
        for (Lecturer lecturer : lecturers){
        	if (lecturerIds.contains(lecturer.getPersonalNr())) {
                LecturerCourse lecturerCourse = new LecturerCourse(lecturer, course, new Date());
                lecturerCoursesOfTheCourse.add(lecturerCourse);
        	}
        }
        course.getLecturerCourses().addAll(lecturerCoursesOfTheCourse);
		return course;
	}

    @Test
    public void testConvertLecturersToIdentities_Empty() {
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(1100L)).thenReturn(null);
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(1200L)).thenReturn(null);
        Course course = getCourseWithLecturers();
        
        assertTrue(dataConverterTestObject.convertLecturersToIdentities(course.getLecturerCourses()).isEmpty());
    }

    
    @Test
    public void testConvertLecturersToIdentities_NotEmpty() {        
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(1100L)).thenReturn(dataGenerator.getSapOlatUsers().get(1));
        when(mockDelegationDao.getDelegationsByDelegator("olatUserName2")).thenReturn(dataGenerator.getDelegations());
        when(mockBaseSecurity.findIdentityByName(dataGenerator.getDelegations().get(0).getDelegatee())).thenReturn(id3);
        
        Course course = getCourseWithLecturers();
        
        assertFalse(dataConverterTestObject.convertLecturersToIdentities(course.getLecturerCourses()).isEmpty());
        assertEquals(2, dataConverterTestObject.convertLecturersToIdentities(course.getLecturerCourses()).size());
        
        when(id3.getStatus()).thenReturn(Identity.STATUS_DELETED);
        assertEquals(1, dataConverterTestObject.convertLecturersToIdentities(course.getLecturerCourses()).size());
        assertTrue(dataConverterTestObject.convertLecturersToIdentities(course.getLecturerCourses()).contains(id2));
    }

    @Test
    public void testconvertDelegateesToIdentities_Empty() {
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(100L)).thenReturn(null);
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(200L)).thenReturn(null);
        
        Course course = getCourseWithLecturers();
        
        assertTrue(dataConverterTestObject.convertDelegateesToIdentities(course.getLecturerCourses()).isEmpty());
    }

    @Test
    public void testGetDelegatees_Empty() {
        when(mockDelegationDao.getDelegationsByDelegator(id1.getName())).thenReturn(dataGenerator.getDelegations());
        when(mockBaseSecurity.findIdentityByName(dataGenerator.getDelegations().get(0).getDelegatee())).thenReturn(null);
        assertTrue(dataConverterTestObject.getDelegatees(id1).isEmpty());
    }

    @Test
    public void testGetDelegatees_NotEmpty() {
        when(mockDelegationDao.getDelegationsByDelegator(id1.getName())).thenReturn(dataGenerator.getDelegations());
        when(mockBaseSecurity.findIdentityByName(dataGenerator.getDelegations().get(0).getDelegatee())).thenReturn(id1);
        assertFalse(dataConverterTestObject.getDelegatees(id1).isEmpty());
    }
     
    @Test
    public void testConvertDelegateesToIdentities_Empty() {
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(1100L)).thenReturn(dataGenerator.getSapOlatUsers().get(0));
        when(mockDelegationDao.getDelegationsByDelegator("olatUserName1")).thenReturn(dataGenerator.getDelegations());
        when(mockBaseSecurity.findIdentityByName(dataGenerator.getDelegations().get(0).getDelegatee())).thenReturn(null);
        when(mockBaseSecurity.findIdentityByName(dataGenerator.getDelegations().get(1).getDelegatee())).thenReturn(null);
        
        Course course = getCourseWithLecturers();
        
        assertTrue(dataConverterTestObject.convertDelegateesToIdentities(course.getLecturerCourses()).isEmpty());
    }
   
    @Test
    public void testConvertDelegateesToIdentities_NotEmpty() {
        when(mockSapOlatUserDao.getSapOlatUserBySapUserId(1100L)).thenReturn(dataGenerator.getSapOlatUsers().get(0));
        when(mockDelegationDao.getDelegationsByDelegator("olatUserName1")).thenReturn(dataGenerator.getDelegations());
        when(mockBaseSecurity.findIdentityByName(dataGenerator.getDelegations().get(0).getDelegatee())).thenReturn(id1);
        when(mockBaseSecurity.findIdentityByName(dataGenerator.getDelegations().get(1).getDelegatee())).thenReturn(id2);

        Course course = getCourseWithLecturers();
        
        assertFalse(dataConverterTestObject.convertDelegateesToIdentities(course.getLecturerCourses()).isEmpty());
        assertEquals(2, dataConverterTestObject.convertDelegateesToIdentities(course.getLecturerCourses()).size());
        assertTrue(dataConverterTestObject.convertDelegateesToIdentities(course.getLecturerCourses()).contains(id1));
        assertTrue(dataConverterTestObject.convertDelegateesToIdentities(course.getLecturerCourses()).contains(id2));
    }
}

