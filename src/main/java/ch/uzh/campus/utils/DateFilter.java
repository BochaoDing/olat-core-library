package ch.uzh.campus.utils;

import java.util.Date;

/**
 * A Filter that represents a time interval.
 * 
 * Initial Date: 02.03.2012 <br>
 * 
 * @author lavinia
 */
public class DateFilter {

    private Date fromDate;
    private Date toDate;

    public DateFilter(Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

}
