package ch.uzh.extension.campuscourse.model;

/**
 * @author Martin Schraner
 */
public abstract class JoinTableIds {

    long firstReference;
    long secondReference;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinTableIds that = (JoinTableIds) o;

        if (firstReference != that.firstReference) return false;
        return secondReference == that.secondReference;

    }

    @Override
    public int hashCode() {
        int result = (int) (firstReference ^ (firstReference >>> 32));
        result = 31 * result + (int) (secondReference ^ (secondReference >>> 32));
        return result;
    }
}
