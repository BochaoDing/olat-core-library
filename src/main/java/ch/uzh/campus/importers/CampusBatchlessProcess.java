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
 * <p>
 */
package ch.uzh.campus.importers;

import ch.uzh.campus.data.Export;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class serves as a generic targetObject for the quartz MethodInvokingJobDetailFactoryBean. <br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusBatchlessProcess {

	private static final OLog LOG = Tracing.createLoggerFor(CampusBatchlessProcess.class);

    private Map<String, String> parameters;
    private Map<String, Importer> importers;

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setImporters(Map<String, Importer> importers) {
        this.importers = importers;
    }

    @PostConstruct
    public void init() {
        LOG.info("JobParameters: [" + parameters + "]");
    }

    /**
     * Delegates the actual launching of the given job to the given jobLauncher <br>
     * only in the case that the process is in the enabled status.
     * 
     * @param status
     *            the status indicating whether the job is enabled or disabled
     * @param campusProcess
     *            the name of the process
     */
    public void process(String status, String campusProcess) throws Exception {
        LOG.info("THE " + campusProcess + " IS: [" + status + "]");

        // Check that job is enabled and correct parameters were passed with spring
        if (status.equals("enabled") && checkParameters()) {
            // Check existence of export folder and descriptor file
            String csvFolderPath = parameters.get("dataDir").replaceFirst("file:", "");
            String csvExportFilePath = csvFolderPath + File.separator + parameters.get("exportResource");
            if (!Files.exists(Paths.get(csvFolderPath))) {
                LOG.error("Folder " + csvFolderPath + " cannot be found");
            } else if (!Files.exists(Paths.get(csvExportFilePath))) {
                LOG.error("Export descriptor " + csvExportFilePath + " cannot be found");
            } else {
                // Check integrity of CSV export descriptor file and fail fast if it is corrupt
                try {
                    // Gather files to be exported - should be the same number as in the white list
                    Map<String, Export> filesToImport = getFilesToImport(importers, csvFolderPath, csvExportFilePath);
                    // If exception is not thrown by this moment, files are ready for export
                    importFiles(importers, filesToImport);
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    private void importFiles(Map<String, Importer> importerMap, Map<String, Export> filesToImport) {
        int i = 1;
        for (Map.Entry<String, Importer> importerEntry : importerMap.entrySet()) {
            final String stepName = importerEntry.getKey();
            LOG.info("Processing file " + stepName);
            String filePath = filesToImport.get(stepName).getFileName();
            Importer importer = importerEntry.getValue();
            importer.setStepId(i++);
            importer.setStepName(stepName);
            importer.process(filePath);
            // Only proceed if step was successful
            if (importer.getStepStatus().equals(Importer.STATUS_FAILED)) {
                LOG.error("CampusKurs import procedure ended prematurely on step " + stepName);
                return;
            }
        }
    }

    private boolean checkParameters() {
        boolean result = false;

        if (parameters == null) {
            LOG.error("No parameters given for CampusBatchlessProcess, check campusJobSchedulerContext.xml");
        } else if (!parameters.containsKey("dataDir")) {
            LOG.error("Parameter dataDir is not specified for CampusBatchlessProcess, check campusJobSchedulerContext.xml");
        } else if (!parameters.containsKey("exportResource")) {
            LOG.error("Parameter exportResource is not specified for CampusBatchlessProcess, check campusJobSchedulerContext.xml");
        } else if (!parameters.containsKey("fileNameSuffix")) {
            LOG.error("Parameter fileNameSuffix is not specified for CampusBatchlessProcess, check campusJobSchedulerContext.xml");
        } else if (!parameters.containsKey("fileNamePrefix")) {
            LOG.error("Parameter fileNamePrefix is not specified for CampusBatchlessProcess, check campusJobSchedulerContext.xml");
        } else {
            result = true;
        }

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }

        return result;
    }

    private Map<String, Export> getFilesToImport(Map<String, Importer> importerMap, String csvFolderPath, String csvExportFilePath) throws Exception {
        Map<String, Export> filesToExport = new HashMap<String, Export>();

        int line = 1;
        // read CSVs with "com.opencsv.CSVReader" library, skipping the first line
        CampusCSVReader reader = new CampusCSVReader(csvExportFilePath, line);
        List myEntries = reader.readAll();
        LOG.info("List of files contains " + myEntries.size() + " entries");
        String fileSuffix = parameters.get("fileNameSuffix");
        String filePrefix = parameters.get("fileNamePrefix");
        for (Object myEntry : myEntries) {
            line++;
            String[] csvFileInfo = (String[]) myEntry;
            if (csvFileInfo.length != 2) {
                throw new Exception("Export descriptor is corrupt" + csvExportFilePath + " at line " + line);
            }
            try {
                String csvFilePath = csvFolderPath + File.separator + csvFileInfo[0];
                // Only take in processing the files with expected names
                if (csvFilePath.endsWith(fileSuffix)) {
                    // Check prefix too
                    String[] nameParts = csvFilePath.replace(fileSuffix, "").split(filePrefix);
                    if (nameParts.length != 2) {
                        throw new Exception("CSV file " + csvFilePath + " is named unexpectedly. Check suffix/prefix in campusJobSchedulerContext.xml");
                    }
                    // Check that files exists. Fail the whole process if it does not
                    if (!Files.exists(Paths.get(csvFilePath))) {
                        throw new Exception("CSV file " + csvFilePath + " is defined in export.csv but cannot be found");
                    }
                    String fileName = nameParts[1].replace("=", "");
                    if (importerMap.containsKey(fileName)) {
                        // Prepare Export object that will be persisted if process gets started
                        Export export = new Export();
                        export.setFileName(csvFilePath);
                        export.setCreationDate(new Date());
                        export.setExportDate(parseDateFromTimestamp(csvFileInfo[1]));
                        filesToExport.put(fileName, export);
                    } else {
                        LOG.warn(fileName + " is ignored because it is not in the whitelist");
                    }
                } else {
                    LOG.info("Ignored file " + csvFilePath + " because of its unexpected name");
                }
            } catch (ParseException pe) {
                LOG.error("Parsing problem when reading export file : " + pe.getMessage());
            } catch (IOException ioe) {
                LOG.error("IO problem when reading export file : " + ioe.getMessage());
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }

        // Finally check that list of files to process is as long as the white list
        if (filesToExport.size() != importerMap.size()) {
            LOG.error("Found only " + filesToExport.size() + " files out of " + importerMap.size());
            throw new Exception("Number of files to process does not match number of white-listed files");
        }

        LOG.info("Files to export: " + filesToExport.keySet().size());
        return filesToExport;
    }

    // TODO move to some Util class
    private static Date parseDateFromTimestamp(String timestampAsString) throws ParseException {
        String timestampStringWithoutNanos = timestampAsString.substring(0, 23) + timestampAsString.substring(29);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        return format.parse(timestampStringWithoutNanos);
    }
}
