package ch.uzh.campus.data;

import java.io.Serializable;

/**
 * @author Martin Schraner
 */
public class DelegatorDelegateeId implements Serializable {

    private Long delegator;
    private Long delegatee;

    public DelegatorDelegateeId() {
    }

    public DelegatorDelegateeId(Long delegator, Long delegatee) {
        this.delegator = delegator;
        this.delegatee = delegatee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DelegatorDelegateeId that = (DelegatorDelegateeId) o;

        if (!delegator.equals(that.delegator)) return false;
        return delegatee.equals(that.delegatee);

    }

    @Override
    public int hashCode() {
        int result = delegator.hashCode();
        result = 31 * result + delegatee.hashCode();
        return result;
    }
}
