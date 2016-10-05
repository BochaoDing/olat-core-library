package ch.uzh.campus.service;

import org.olat.group.BusinessGroup;

/**
 * @author Martin Schraner
 */
public class CampusCourseGroups {

    private final BusinessGroup campusCourseGroupA;
    private final BusinessGroup campusCourseGroupB;

    public CampusCourseGroups(BusinessGroup campusCourseGroupA, BusinessGroup campusCourseGroupB) {
        this.campusCourseGroupA = campusCourseGroupA;
        this.campusCourseGroupB = campusCourseGroupB;
    }

    public BusinessGroup getCampusCourseGroupA() {
        return campusCourseGroupA;
    }

    public BusinessGroup getCampusCourseGroupB() {
        return campusCourseGroupB;
    }
}
