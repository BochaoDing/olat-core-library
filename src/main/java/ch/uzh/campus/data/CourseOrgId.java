package ch.uzh.campus.data;

import java.util.Date;

/**
 * Class to save the entries from the CSV file.
 * Cannot be inherited from Course, since "exclude" is a boolean in Course, but a String in this class.
 * (Inheritance and overwriting the setter of exclude did not work, since Spring ignored the overwritten setter.)
 *
 * @author Martin Schraner
 */
public class CourseOrgId {

    private Long id;
    private Long resourceableId;
    private String shortTitle;
    private String title;
    private String vstNr;
    private String isELearning;
    private String language;
    private String category;
    private Date startDate;
    private Date endDate;
    private String vvzLink;
    private String semester;
    private String shortSemester;
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

    public Long getResourceableId() {
        return resourceableId;
    }

    public void setResourceableId(Long resourceableId) {
        this.resourceableId = resourceableId;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVstNr() {
        return vstNr;
    }

    public void setVstNr(String vstNr) {
        this.vstNr = vstNr;
    }

    public String getIsELearning() {
        return isELearning;
    }

    public void setIsELearning(String isELearning) {
        this.isELearning = isELearning;
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

    public String getShortSemester() {
        return shortSemester;
    }

    public void setShortSemester(String shortSemester) {
        this.shortSemester = shortSemester;
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
}
