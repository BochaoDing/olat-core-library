package ch.uzh.campus.service.data;

import org.olat.group.BusinessGroup;

/**
 * @author Martin Schraner
 */
public class CampusGroups {

    private final BusinessGroup campusGroupA;
    private final BusinessGroup campusGroupB;

    public CampusGroups(BusinessGroup campusGroupA, BusinessGroup campusGroupB) {
        this.campusGroupA = campusGroupA;
        this.campusGroupB = campusGroupB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CampusGroups that = (CampusGroups) o;

        return campusGroupA != null ? campusGroupA.equals(that.campusGroupA) : that.campusGroupA == null
                && (campusGroupB != null ? campusGroupB.equals(that.campusGroupB) : that.campusGroupB == null);

    }

    @Override
    public int hashCode() {
        int result = campusGroupA != null ? campusGroupA.hashCode() : 0;
        result = 31 * result + (campusGroupB != null ? campusGroupB.hashCode() : 0);
        return result;
    }

    public BusinessGroup getCampusGroupA() {
        return campusGroupA;
    }

    public BusinessGroup getCampusGroupB() {
        return campusGroupB;
    }
}
