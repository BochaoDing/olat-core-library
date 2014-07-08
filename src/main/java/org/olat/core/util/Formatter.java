/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;

/**
 * enclosing_type Description: <br>
 * A formatter to format locale-specific things (mainly dates and times)
 * 
 * @author Felix Jost
 */
public class Formatter {

	private static final DateFormat formatterDatetimeFilesystem = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss_SSS");
	private static final DateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static final Map<Locale,Formatter> localToFormatterMap = new HashMap<Locale,Formatter>();
	
	private final Locale locale;
	private final DateFormat shortDateFormat;
	private final DateFormat shortDateTimeFormat;
	private final DateFormat shortTimeFormat;
	private final DateFormat mediumTimeFormat;

	/**
	 * Constructor for Formatter.
	 */
	private Formatter(Locale locale) {
		this.locale = locale;
		shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		shortDateFormat.setLenient(false);
		mediumTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
		mediumTimeFormat.setLenient(false);
		shortDateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
		shortDateTimeFormat.setLenient(false);
		shortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		shortTimeFormat.setLenient(false);
	}

	/**
	 * get an instance of the Formatter given the locale
	 * 
	 * @param locale the locale which the formatter should use in its operations
	 * @return the instance of the Formatter
	 */
	public static Formatter getInstance(Locale locale) {
		Formatter formatter;
		if(localToFormatterMap.containsKey(locale)) {
			formatter = localToFormatterMap.get(locale);
		} else {
			formatter = new Formatter(locale);
			localToFormatterMap.put(locale, formatter);
		}
		return formatter;
	}

	/**
	 * formats the given date so it is friendly to read
	 * 
	 * @param d the date
	 * @return a String with the formatted date
	 */
	public String formatDate(Date d) {
		synchronized (shortDateFormat) {
			return shortDateFormat.format(d);
		}
	}
	
	public Date parseDate(String val) throws ParseException {
		synchronized (shortDateFormat) {
			return shortDateFormat.parse(val);
		}
	}
	
	/**
	 * formats the given time period so it is friendly to read
	 * 
	 * @param d the date
	 * @return a String with the formatted time
	 */
	public String formatTime(Date d) {
		synchronized (mediumTimeFormat) {
			return mediumTimeFormat.format(d);
		}
	}

	/**
	 * formats the given date so it is friendly to read
	 * 
	 * @param d the date
	 * @return a String with the formatted date and time
	 */
	public String formatDateAndTime(Date d) {
		if (d == null) return null;
		synchronized (shortDateTimeFormat) {
			return shortDateTimeFormat.format(d);
		}
	}

	/**
	 * Generate a simple date pattern that formats a date using the locale of the
	 * formatter
	 * 
	 * @return
	 */
	public String getSimpleDatePatternForDate() {
		Calendar cal = new GregorianCalendar();
		cal.set( 1999, Calendar.MARCH, 1, 0, 0, 0 );  		
		Date testDate = cal.getTime();
		String formattedDate = formatDate(testDate);
		formattedDate = formattedDate.replace("1999", "%Y");
		formattedDate = formattedDate.replace("99", "%Y");
		formattedDate = formattedDate.replace("03", "%m");
		formattedDate = formattedDate.replace("3", "%m");
		formattedDate = formattedDate.replace("01", "%d");
		formattedDate = formattedDate.replace("1", "%d");
		return formattedDate;
	}

	/**
	 * Generate a simple date pattern that formats a date with time using the
	 * locale of the formatter
	 * 
	 * @return
	 */
	public String getSimpleDatePatternForDateAndTime() {
		Calendar cal = new GregorianCalendar();
		cal.set( 1999, Calendar.MARCH, 1, 4, 5, 0 );  		
		Date testDate = cal.getTime();
		String formattedDate = formatDateAndTime(testDate);
		formattedDate = formattedDate.replace("1999", "%Y");
		formattedDate = formattedDate.replace("99", "%Y");
		formattedDate = formattedDate.replace("03", "%m");
		formattedDate = formattedDate.replace("3", "%m");
		formattedDate = formattedDate.replace("01", "%d");
		formattedDate = formattedDate.replace("1", "%d");
		formattedDate = formattedDate.replace("04", "%H");
		formattedDate = formattedDate.replace("4", "%H");
		formattedDate = formattedDate.replace("05", "%M");
		formattedDate = formattedDate.replace("5", "%M");
		if(formattedDate.endsWith("AM")) {
			formattedDate = formattedDate.replace("%H","%I").replace("AM", "%p");
		}
		return formattedDate;
	}


	
	/**
	 * Formats the given date with the ISO 8601 standard also known as 'datetime'
	 * See http://www.w3.org/TR/NOTE-datetime.html for more info.
	 * 
	 * @param d the date to be formatted
	 * @return a String with the formatted date and time
	 */
	public static String formatDatetime(Date d) {
		synchronized (formatDateTime) {
			return formatDateTime.format(d);
		}
	}
	
	/**
	 * Use this for naming files or directories with a timestamp. 
	 * As windows does not like ":" in filenames formatDateAndTime(d) does not work
	 * 
	 * @param d the date to be formatted
	 * @return a String with the formatted date and time
	 */
	public static String formatDatetimeFilesystemSave(Date d) {
		synchronized (formatterDatetimeFilesystem) {
			return formatterDatetimeFilesystem.format(d);
		}
	}

	/**
	 * formats the given time period so it is friendly to read
	 * 
	 * @param d the date
	 * @return a String with the formatted time
	 */
	public String formatTimeShort(Date d) {
		synchronized (shortTimeFormat) {
			return shortTimeFormat.format(d);
		}
	}

	/**
	 * Formats a duration in millis to "XXh YYm ZZs"
	 * @param millis
	 * @return formatted string
	 */
	public static String formatDuration(long millis) {
		long h = millis / (1000*60*60);
		long hmins = h*1000*60*60;
		long m = (millis - hmins)/(1000*60);
		long s = (millis - hmins - m*1000*60)/1000;
		return h + "h " + m + "m " + s + "s";
	}
	
	/**
	 * Format the given bytes to human readable format
	 * @param bytes the byte count
	 * @return human readable formatted bytes
	 */
	public static String formatBytes(long bytes) {
	    int unit = 1000;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = "kMGTPE".charAt(exp-1) + "";
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}	
	/**
	 * Escape " with \" in strings
	 * @param source
	 * @return escaped string
	 */
	public static StringBuilder escapeDoubleQuotes(String source) {
		StringBuilder sb = new StringBuilder(300);
		if (source != null) {
			int len = source.length();
			char[] cs = source.toCharArray();
			for (int i = 0; i < len; i++) {
				char c = cs[i];
				switch (c) {
					case '"':
						sb.append("&quot;");
						break;
					default:
						sb.append(c);
				}
			}
		}
		return sb;
	}
	
	/**
	 * Escape " with \" and ' with \' in strings
	 * @param source
	 * @return escaped string
	 * @deprecated use org.apache.commons.lang.StringEscapeUtils.escapeJavaScript() instead.
	 */
	public static StringBuilder escapeSingleAndDoubleQuotes(String source) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(300);
		int len = source.length();
		char[] cs = source.toCharArray();
		for (int i = 0; i < len; i++) {
			char c = cs[i];
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\'':
					sb.append("\\\'");
					break;
				default:
					sb.append(c);
			}
		}
		return sb;
	}

	/**
	 * replace a given String with a different String
	 * 
	 * @param source the source
	 * @param delim the String to replace
	 * @param replacement the replacement String
	 * @return StringBuilder
	 */
	public StringBuilder replace(String source, String delim, String replacement) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(300);
		StringTokenizer st = new StringTokenizer(source, delim);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			sb.append(tok);
			sb.append(replacement);
		}
		return sb;
	}

	/**
	 * truncates the supplied string to len-3 and replaces the last three positions
	 * with ...
	 * 
	 * @param source
	 * @param len
	 * @return truncated string
	 */
	public static String truncate(String source, int len) {
		if (source == null) return null;
		if (source.length() > len && len > 3) return truncate(source, len - 3, "...");
		else return source;
	}
	
	/**
	 * This returns a substring of a len length from the input string, as opposite to the 
	 * <code> truncate </code> method. This should be used for processing strings before writing.
	 * @param source
	 * @param len
	 * @return
	 */
	public static String truncateOnly(String source, int len) {
		if (source == null) return null;
		if (source.length() > len) return source.substring(0, len) ;
		else return source;
	}

	/**
	 * replaces all non ASCII characters in a string and also the most common special characters like by urlencode it
	 * "/" "\" ":" "*" "?" """ ' "<" ">" "|"
	 * @param source
	 * @return a string which is OS independant and save for using on any filesystem
	 */
	public static String makeStringFilesystemSave(String source) {
		try {
			source = removeUndesirableSubstrings(source);
			return URLEncoder.encode(source, "utf-8");
		} catch (UnsupportedEncodingException e) {
			//utf-8 should be supported
		}
		return "";
	}
	
	/**
	 * 
	 * @param source
	 * @return
	 */
	private static String removeUndesirableSubstrings(String source) {
		String returnString = source;
		
		//replace successive dots with only one, for now.
		while(returnString!=null && returnString.indexOf("..")>-1) {				
		  returnString = returnString.replaceAll("\\.\\.", "."); //replace recursive 2 dots with one
		} 
		
		return returnString;
	}
	
		/**
	 * truncates a String: useful to limit in GUI
	 * 
	 * @param source
	 * @param len length of the returned string; if negative, return n chars from
	 *          the end of the string, otherwise from the beginning of the string
	 * @param delim
	 * @return truncated string
	 */
	public static String truncate(String source, int len, String delim) {
		if (source == null) return null;
		int start, stop;
		int alen = source.length();
		if (len > 0) {
			if (alen <= len) return source;
			start = 0; //TODO effizienter
			stop = (len > alen ? alen : len);
			StringBuilder sb = new StringBuilder(source.substring(start, stop));
			if (alen > len) sb.append(delim);
			return sb.toString();
		}
		start = (len < -alen ? 0 : alen + len);
		stop = alen;
		StringBuilder sb = new StringBuilder(source.substring(start, stop));
		if (-alen <= len) sb.insert(0, delim);
		return sb.toString();
	}

	/**
	 * @return locale of this formatter
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param source
	 * @return escaped string
	 */
	public static StringBuilder escWithBR(String source) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(300);
		int len = source.length();
		char[] cs = source.toCharArray();
		for (int i = 0; i < len; i++) {
			char c = cs[i];
			switch (c) {
				case '"':
					sb.append("&quot;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '&':
					// check on &# first (entities -> don't escape)
					if (i < len - 1 && cs[i + 1] == '#') { // we have # as next char
						sb.append("&");
					} else {
						sb.append("&amp;");
					}
					break;
				//case '\r':sb.append("<br />"); break;
				case '\n':
					sb.append("<br />");
					break;
				default:
					sb.append(c);
			}
		}
		return sb;
	}
	

	/**
	 * @param source
	 * @return stripped string
	 */
	public static StringBuilder stripTabsAndReturns(String source) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(source.length() + (source.length() / 10));
		int len = source.length();
		char[] cs = source.toCharArray();
		for (int i = 0; i < len; i++) {
			char c = cs[i];
			switch (c) {
				case '\t':
					sb.append("    ");
					break;
				case '\n':
					sb.append("<br />");
					break;
				case '\r':
					sb.append("<br />");
					break;
				case '\u2028': // unicode linebreak
					sb.append("<br />");
					break;
				default:
					sb.append(c);
			}
		}
		return sb;
	}

	/**
	 * Wrapp given html code with a wrapper an add code to transform latex
	 * formulas to nice visual characters on the client side. The latex formulas
	 * must be within an HTML element that has the class 'math' attached.
	 * 
	 * @param htmlFragment A html element that might contain an element that has a
	 *          class 'math' with latex formulas
	 * @return
	 */
	public static String formatLatexFormulas(String htmlFragment) {
		if (htmlFragment == null) return "";
		// optimize, reduce jsmath calls on client
		if ((htmlFragment.contains("class='math'") || htmlFragment.contains("class=\"math\""))) {
			// add math wrapper
			String domid = "mw_" + CodeHelper.getRAMUniqueID();
			String elem = htmlFragment.contains("<div") ? "div" : "span";
			StringBuilder sb = new StringBuilder(htmlFragment.length() + 200);
			sb.append("<").append(elem).append(" id=\"").append(domid).append("\">");
			sb.append(htmlFragment);
			sb.append("</").append(elem).append(">");
			sb.append("\n<script type='text/javascript'>\n/* <![CDATA[ */\n setTimeout(function() { BFormatter.formatLatexFormulas('").append(domid).append("');}, 100);\n/* ]]> */\n</script>");
			return sb.toString();
		}			
		return htmlFragment;
	}
	
	
	// Pattern to find URL's in text
	private static final Pattern urlPattern = Pattern.compile("((mailto\\:|(news|(ht|f)tp(s?))\\://|www\\.)[-A-Za-z0-9+&@#/%?=~_|!:,\\.;]+[-A-Za-z0-9+&@#/%=~_|]*)");

	/**
	 * Search in given text fragment for URL's and surround them with clickable
	 * HTML link objects.
	 * 
	 * @param textFragment
	 * @return text with clickable links
	 */
	public static String formatURLsAsLinks(String textFragment) {
		Matcher matcher = urlPattern.matcher(textFragment); 		
		
		StringBuilder sb = new StringBuilder(128);
		int pos = 0;
		while (matcher.find()) {
			// Add text since last match and set end of current patch as new end
			// of this match
			sb.append(textFragment.substring(pos, matcher.start()));
			pos = matcher.end();
			// The URL is in group1, the other groups are ignored
			String url = matcher.group(1);
			// Fix URL's without protocol, assume http
			if (url.startsWith("www")) {
				url = "http://" + url; 
			}
			// Fix URL's at end of a sentence
			if (url.endsWith(",") || url.endsWith(".") || url.endsWith(":")) {
				url = url.substring(0, url.length()-1);
				pos--;
			}
			sb.append("<a href=\"");
			sb.append(url);
			sb.append("\"");
			if (url.startsWith("mailto")) {
				sb.append(" target=\"_blank\" class=\"b_link_mailto\"");				
			}
			// OpenOLAT URL's are opened in same window, all other URL's in separate window
			else if (!url.startsWith(Settings.getServerContextPathURI())) {
				sb.append(" target=\"_blank\" class=\"b_link_extern\"");				
			}
			else {
				sb.append(" class=\"b_link_forward\"");				
			}
			sb.append(">");
			sb.append(url);
			sb.append("</a>");
		}
		// Add rest of text
		sb.append(textFragment.substring(pos));
		//
		return sb.toString();
	}
	

	/* emoticon patterns */
	private static final Pattern angelPattern = Pattern.compile("(O\\:-*(\\)|3))");
	private static final Pattern angryPattern = Pattern.compile("(\\:-*(\\|\\||@))");
	private static final Pattern confusedPattern = Pattern.compile("(%-*\\))");
	private static final Pattern coolPattern = Pattern.compile("(8-*\\))");
	private static final Pattern grinPattern = Pattern.compile("(;-*\\))");
	private static final Pattern kissPattern = Pattern.compile("(\\:(\\^)*\\*)");
	private static final Pattern ohohPattern = Pattern.compile("(\\:-*O)");
	private static final Pattern sadPattern = Pattern.compile("(\\:-*\\()");
	private static final Pattern smilePattern = Pattern.compile("(\\:-*\\))");
	private static final Pattern tonguePattern = Pattern.compile("(\\:-*P)");
	private static final Pattern upPattern = Pattern.compile("(\\+(\\s|$))");
	private static final Pattern downPattern = Pattern.compile("(-(\\s|$))");
	
	private static final StringOutput emptyGifUrl = new StringOutput();
	static {
		StaticMediaDispatcher.renderStaticURI(emptyGifUrl, "images/transparent.gif");
	}
	/**
	 * Search in textFragment for emoticons such as :-) :-( etc and replace them
	 * with image tags that render a nice icon.
	 * 
	 * @param textFragment
	 * @return replaced text
	 */
	public static String formatEmoticonsAsImages(String textFragment) {
		
		Matcher matcher;
		matcher = confusedPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_confused' />");
		matcher = coolPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_cool' />");
		matcher = angryPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_angry' />");
		matcher = grinPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_grin' />");
		matcher = kissPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_kiss' />");
		matcher = ohohPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_ohoh' />");
		matcher = angelPattern.matcher(textFragment); // must be before smile pattern
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_angel' />");
		matcher = smilePattern.matcher(textFragment); 
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_smile' />");
		matcher = sadPattern.matcher(textFragment);
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_sad' />");
		matcher = tonguePattern.matcher(textFragment); 
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_tongue' />");
		matcher = upPattern.matcher(textFragment); 
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_up' />");
		matcher = downPattern.matcher(textFragment); 
		textFragment= matcher.replaceAll("<img src='" + emptyGifUrl + "' class='o_emoticons_down' />");
				
		return textFragment;
	}
	
	/**
	 * Round a double value to a double value with given number of 
	 * figures after comma
	 * @param value
	 * @param decimalPlace
	 * @return rounded double value
	 */
	public static double round(double value, int decimalPlace) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
    value = bd.doubleValue();
    return value;
	}

	/**
	 * Round a float value to a float value with given number of 
	 * figures after comma
	 * @param value
	 * @param decimalPlace
	 * @return rounded float value
	 */
	public static float round(float value, int decimalPlace) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP); 
    value = bd.floatValue();
    return value;
	}
	
	/**
	 * Format a float as string with given number of figures after
	 * comma
	 * @param value
	 * @param decimalPlace
	 * @return formatted string
	 */
	public static String roundToString(float value, int decimalPlace) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP); 
		return bd.toString();
	}
	
}
