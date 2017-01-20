package ch.uzh.extension.campuscourse.batchprocessing;

/**
 * This enumeration defines the different steps involving in the campus batch processing.<br>
 * 
 * Initial Date: 13.07.2012 <br>
 * 
 * @author aabouc
 */
public enum CampusBatchStepName {

    /**
     * This step describes the import of the data of SAP courses (LV).
     */
    IMPORT_COURSES("importCourses"),
    /**
     * This step describes the import of the Students data.
     */
    IMPORT_STUDENTS("importStudents"),
    /**
     * This step describes the import of the lecturers data.
     */
    IMPORT_LECTURERS("importLecturers"),
    /**
     * This step describes the import of the contents, materials, etc of the courses data
     */
    IMPORT_TEXTS("importTexts"),
    /**
     * This step describes the import of the events (Termine) data
     */
    IMPORT_EVENTS("importEvents"),
	/**
	 * This step describes the import of the organizations data
	 */
	IMPORT_ORGS("importOrgs"),
    /**
     * This step describes the import of the relationships data (lecturers - courses)
     */
    IMPORT_LECTURER_COURSES("importLecturerCourses"),
    /**
     * This step describes the import of the relationships data (students - courses)
     */
    IMPORT_STUDENT_COURSES("importStudentCourses"),
    /**
     * This step describes the synchronization of courses and participants
     */
    CAMPUS_COURSE_SYNCHRONIZATION("campusCourseSynchronization"),
    /**
     * This step describes the user mapping of the students imported by SAP
     */
    STUDENT_MAPPING("studentMapping"),
    /**
     * This step describes the user mapping of the lecturers imported by SAP
     */
    LECTURER_MAPPING("lecturerMapping");

	private String name;

	CampusBatchStepName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static CampusBatchStepName findByName(String name) {
		for (CampusBatchStepName campusBatchStepName : values()) {
			if (campusBatchStepName.toString().equals(name)) {
				return campusBatchStepName;
			}
		}
		return null;
	}
}
