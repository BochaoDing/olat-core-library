package ch.uzh.extension.campuscourse.model;

/**
 * @author Martin Schraner
 */
public enum SemesterName {

    FRUEHJAHRSSEMESTER("Frühjahrssemester", "FS", 1),
    HERBSTSEMESTER("Herbstsemester", "HS", 2);

    private String name;
    private String shortName;
    private Integer sequenceWithinYear;

    SemesterName(String name, String shortName, Integer sequenceWithinYear) {
        this.name = name;
        this.shortName = shortName;
        this.sequenceWithinYear = sequenceWithinYear;
    }

    public String getShortName() {
        return shortName;
    }

    public Integer getSequenceWithinYear() {
        return sequenceWithinYear;
    }

    @Override
    public String toString() {
        return name;
    }

    public static SemesterName findByName(String name) {
        for (SemesterName semesterName : values()) {
            if (semesterName.toString().equals(name)) {
                return semesterName;
            }
        }
        return null;
    }
}
