package ch.uzh.campus.service.core.impl;

import org.olat.core.util.Formatter;

/**
 * @author Martin Schraner
 */
public class CampusCourseTool {

    private static final int MAX_DISPLAYNAME_LENGTH = 140;

    public static String getTruncatedDisplayname(String title) {
        return Formatter.truncate(title, MAX_DISPLAYNAME_LENGTH);
    }
}
