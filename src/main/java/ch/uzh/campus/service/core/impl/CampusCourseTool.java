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

    public static String getShortSemestersOfDisplayname(String title) {
        int firstBlank = title.indexOf(" ");
        if (firstBlank <= 0) {
            return "";
        }
        return title.substring(0, firstBlank);
    }

    public static String getTruncatedDisplaynameWithoutShortSemesters(String title) {
        String truncatedDisplayname = getTruncatedDisplayname(title);
        int firstBlank = truncatedDisplayname.indexOf(" ");
        if (firstBlank == -1) {
            return truncatedDisplayname;
        }
        return truncatedDisplayname.substring(firstBlank + 1);
    }
}
