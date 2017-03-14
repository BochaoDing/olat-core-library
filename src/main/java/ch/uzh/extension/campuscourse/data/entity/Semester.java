package ch.uzh.extension.campuscourse.data.entity;

import ch.uzh.extension.campuscourse.model.SemesterName;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

import static ch.uzh.extension.campuscourse.data.entity.Semester.*;

/**
 * Initial Date: 07.12.2012 <br>
 *
 * @author aabouc
 * @author Martin Schraner
 */
@Entity
@Table(name = "ck_semester")
@NamedQueries({
        @NamedQuery(name = GET_ALL_SEMESTERS, query = "select s from Semester s"),
        @NamedQuery(name = GET_SEMESTER_BY_SEMESTER_NAME_AND_YEAR, query = "select s from Semester s where s.semesterName = :semesterName and s.year = :year"),
        @NamedQuery(name = GET_CURRENT_SEMESTER, query = "select s from Semester s where s.currentSemester = true")
        })
public class Semester implements Comparable<Semester>  {

    public static final String GET_ALL_SEMESTERS = "getAllSemesters";
    public static final String GET_SEMESTER_BY_SEMESTER_NAME_AND_YEAR = "getSemesterBySemesterNameAndYear";
    public static final String GET_CURRENT_SEMESTER = "getCurrentSemester";

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
			@Parameter(name="sequence_name", value="hibernate_unique_key"),
			@Parameter(name="force_table_use", value="true"),
			@Parameter(name="optimizer", value="legacy-hilo"),
			@Parameter(name="value_column", value="next_hi"),
			@Parameter(name="increment_size", value="32767"),
			@Parameter(name="initial_value", value="32767")
	})
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private SemesterName semesterName;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "current_semester", nullable = false)
    private boolean currentSemester;

    public Semester() {
    }

    public Semester(SemesterName semesterName, Integer year, boolean currentSemester) {
        this.semesterName = semesterName;
        this.year = year;
        this.currentSemester = currentSemester;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SemesterName getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(SemesterName semesterName) {
        this.semesterName = semesterName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public boolean isCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(boolean currentSemester) {
        this.currentSemester = currentSemester;
    }

    @Transient
    public String getSemesterNameYear() {
        return semesterName.toString() + " " + year;
    }

    @Transient
    String getShortYearShortSemesterName() {
        return Integer.toString(year).substring(2) + semesterName.getShortName();
    }

    @Transient
    Integer getSequenceWithinYear() {
        return semesterName.getSequenceWithinYear();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", getId());
        builder.append("name", getSemesterName());
        builder.append("year", getYear());
        builder.append("currentSemester", isCurrentSemester());

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Semester semester = (Semester) o;

        if (semesterName != semester.semesterName) return false;
        return year.equals(semester.year);

    }

    @Override
    public int hashCode() {
        int result = semesterName.hashCode();
        result = 31 * result + year.hashCode();
        return result;
    }

    @Override
    public int compareTo(Semester otherSemester) {
        int result = year.compareTo(otherSemester.year);
        if (result == 0) {
            result = getSequenceWithinYear().compareTo(otherSemester.getSequenceWithinYear());
        }
        return result;
    }
}

