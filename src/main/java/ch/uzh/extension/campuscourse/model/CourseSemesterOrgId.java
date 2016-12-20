package ch.uzh.extension.campuscourse.model;

import ch.uzh.extension.campuscourse.data.entity.Course;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Class to save the entries from the CSV file.
 * Cannot be inherited from Course, since "exclude" is a boolean in Course, but a String in this class.
 * (Inheritance and overwriting the setter of exclude did not work, since Spring ignored the overwritten setter.)
 *
 * @author Martin Schraner
 */
public class CourseSemesterOrgId {

    private static final String WHITESPACE = " ";

    private Long id;
    private String lvKuerzel;
    private String title;
    private String lvNr;
    private String eLearningSupported;
    private String language;
    private String category;
    private Date startDate;
    private Date endDate;
    private String vvzLink;
    private String semester;
    private String exclude;
    private Long org1;
    private Long org2;
    private Long org3;
    private Long org4;
    private Long org5;
    private Long org6;
    private Long org7;
    private Long org8;
    private Long org9;
    private Date dateOfImport;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLvKuerzel() {
        return lvKuerzel;
    }

    public void setLvKuerzel(String lvKuerzel) {
        this.lvKuerzel = lvKuerzel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLvNr() {
        return lvNr;
    }

    public void setLvNr(String lvNr) {
        this.lvNr = lvNr;
    }

    public String getELearningSupported() {
        return eLearningSupported;
    }

    public void setELearningSupported(String eLearningSupported) {
        this.eLearningSupported = eLearningSupported;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getVvzLink() {
        return vvzLink;
    }

    public void setVvzLink(String vvzLink) {
        this.vvzLink = vvzLink;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public Long getOrg1() {
        return org1;
    }

    public void setOrg1(Long org1) {
        this.org1 = org1;
    }

    public Long getOrg2() {
        return org2;
    }

    public void setOrg2(Long org2) {
        this.org2 = org2;
    }

    public Long getOrg3() {
        return org3;
    }

    public void setOrg3(Long org3) {
        this.org3 = org3;
    }

    public Long getOrg4() {
        return org4;
    }

    public void setOrg4(Long org4) {
        this.org4 = org4;
    }

    public Long getOrg5() {
        return org5;
    }

    public void setOrg5(Long org5) {
        this.org5 = org5;
    }

    public Long getOrg6() {
        return org6;
    }

    public void setOrg6(Long org6) {
        this.org6 = org6;
    }

    public Long getOrg7() {
        return org7;
    }

    public void setOrg7(Long org7) {
        this.org7 = org7;
    }

    public Long getOrg8() {
        return org8;
    }

    public void setOrg8(Long org8) {
        this.org8 = org8;
    }

    public Long getOrg9() {
        return org9;
    }

    public void setOrg9(Long org9) {
        this.org9 = org9;
    }

    public Date getDateOfImport() {
        return dateOfImport;
    }

    public void setDateOfImport(Date dateOfImport) {
        this.dateOfImport = dateOfImport;
    }

    public SemesterName getSemesterName() {
        String[] split = StringUtils.split(semester, WHITESPACE);
        return SemesterName.findByName(split[0]);
    }

    public Integer getSemesterYear() {
        Integer yyyy = null;
        String[] split = StringUtils.split(semester, WHITESPACE);
        if (split != null && split.length >= 2) {
            String yyyyAsString = (split[1] != null && split[1].length() >= 4) ? split[1].substring(0, 4) : "";
            try {
                yyyy = Integer.parseInt(yyyyAsString);
            } catch (NumberFormatException e) {
                yyyy = null;
            }
        }
        return yyyy;
    }

	public void merge(Course course) {
		course.setId(getId());
		course.setLvKuerzel(getLvKuerzel());
		course.setTitle(getTitle());
		course.setLvNr(getLvNr());
		course.setELearningSupported("X".equalsIgnoreCase(getELearningSupported()));
		course.setLanguage(getLanguage());
		course.setCategory(getCategory());
		course.setStartDate(getStartDate());
		course.setEndDate(getEndDate());
		course.setVvzLink(getVvzLink());
		course.setExclude("X".equalsIgnoreCase(getExclude()));
		course.setDateOfImport(getDateOfImport());
	}
}
