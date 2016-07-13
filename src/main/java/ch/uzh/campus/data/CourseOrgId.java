package ch.uzh.campus.data;

/**
 * @author Martin Schraner
 */
public class CourseOrgId extends Course {

    private String excl = "";
    private Long org1;
    private Long org2;
    private Long org3;
    private Long org4;
    private Long org5;
    private Long org6;
    private Long org7;
    private Long org8;
    private Long org9;

    public String getExcl() {
        return excl;
    }

    // Required by Spring
    public void setExcl(String excl) {
        this.excl = excl;
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
}
