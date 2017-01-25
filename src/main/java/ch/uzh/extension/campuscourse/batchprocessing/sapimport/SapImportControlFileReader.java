package ch.uzh.extension.campuscourse.batchprocessing.sapimport;

import ch.uzh.extension.campuscourse.common.CampusCourseConfiguration;
import ch.uzh.extension.campuscourse.util.DateUtil;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Martin Schraner
 */
@Component
public class SapImportControlFileReader {

	private static final OLog LOG = Tracing.createLoggerFor(SapImportControlFileReader.class);
	private static final String CSV_SEPARATOR = ";";

	private final CampusCourseConfiguration campusCourseConfiguration;

	@Autowired
	public SapImportControlFileReader(CampusCourseConfiguration campusCourseConfiguration) {
		this.campusCourseConfiguration = campusCourseConfiguration;
	}

	Calendar getDateOfSyncOfSapImportFile(String sapImportFilename)  {
		try {
			List<String[]> lines = readSapImportControlFileIntoListOfStringArrays();
			for (String[] line : lines) {
				if (determineFilename(line).equals(sapImportFilename)) {
					return determineDateOfSync(line);
				}
			}
		} catch (IOException | ParseException e) {
			LOG.error(e.getMessage());
		}
		return null;
	}

	Set<String> getFilenamesOfImportableSapImportFilesWithCorrectSuffixNotOlderThanOneDay() throws IOException, ParseException {
		Set<String> filenamesOfImportableSapImportFilesWithCorrectSuffixNotOlderThanOneDay = new HashSet<>();
		List<String[]> lines = readSapImportControlFileIntoListOfStringArrays();
		for (String[] line : lines) {
			String filename = determineFilename(line);

			// Check suffix
			if (!filename.contains(campusCourseConfiguration.getSapImportFilesSuffix())) {
				continue;
			}

			// Check date of sync
			Calendar dateOfSync = determineDateOfSync(line);
			if (DateUtil.isMoreThanOneDayBefore(dateOfSync.getTime())) {
				LOG.warn("The date of sync of the sap import file " + filename + " is older than one day: " + dateOfSync.getTime());
				continue;
			}

			filenamesOfImportableSapImportFilesWithCorrectSuffixNotOlderThanOneDay.add(filename);
		}
		return filenamesOfImportableSapImportFilesWithCorrectSuffixNotOlderThanOneDay;
	}

	List<String> getAllFilenamesWithDateOfSync() throws IOException, ParseException {
		List<String> filenamesWithDateOfSync = new ArrayList<>();
		List<String[]> lines = readSapImportControlFileIntoListOfStringArrays();
		for (String[] line : lines) {
			filenamesWithDateOfSync.add(determineFilename(line) + "\t(date of sync: " + determineDateOfSync(line).getTime().toString() + ")");
		}
		return filenamesWithDateOfSync;
	}

	private List<String[]> readSapImportControlFileIntoListOfStringArrays() throws IOException {
		List<String[]> listOfLinesAsStringArray = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(campusCourseConfiguration.getSapImportControlFilenameWithPath()));
		String line;
		boolean isHeader = true;
		while ((line = br.readLine()) != null) {
			if (isHeader) {
				isHeader = false;
				continue;
			}
			String[] lineAsStringArray = line.split(CSV_SEPARATOR);
			listOfLinesAsStringArray.add(lineAsStringArray);
		}
		return listOfLinesAsStringArray;
	}

	private String determineFilename(String[] line) {
		return line[0];
	}

	private Calendar determineDateOfSync(String[] line) throws ParseException {
		Calendar dateOfSync = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		dateOfSync.setTime(sdf.parse(line[1]));
		return dateOfSync;
	}
}
