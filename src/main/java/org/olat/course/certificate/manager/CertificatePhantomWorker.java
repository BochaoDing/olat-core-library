/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.certificate.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.velocity.VelocityContext;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 16.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatePhantomWorker {
	private static final OLog log = Tracing
			.createLoggerFor(CertificateTemplateWorker.class);
	
	private final Float score;
	private final Boolean passed;
	private final Identity identity;
	private final RepositoryEntry entry;

	private Date dateCertification;
	private Date dateFirstCertification;

	private final Locale locale;
	private final UserManager userManager;
	private final CertificatesManagerImpl certificatesManager;

	public CertificatePhantomWorker(Identity identity, RepositoryEntry entry,
			Float score, Boolean passed, Date dateCertification,
			Date dateFirstCertification, Locale locale,
			UserManager userManager, CertificatesManagerImpl certificatesManager) {
		this.entry = entry;
		this.score = score;
		this.locale = locale;
		this.passed = passed;
		this.identity = identity;
		this.dateCertification = dateCertification;
		this.dateFirstCertification = dateFirstCertification;
		this.userManager = userManager;
		this.certificatesManager = certificatesManager;
	}

	public File fill(CertificateTemplate template, File destinationDir) {
		File certificateFile = new File(destinationDir, "Certificate.pdf");
		File templateFile = certificatesManager.getTemplateFile(template);
		File htmlCertificateFile = copyAndEnrichTemplate(templateFile);

		List<String> cmds = new ArrayList<String>();
		cmds.add("phantomjs");
		cmds.add(certificatesManager.getRasterizePath().toFile().getAbsolutePath());
		cmds.add(htmlCertificateFile.getAbsolutePath());
		cmds.add(certificateFile.getAbsolutePath());
		if(StringHelper.containsNonWhitespace(template.getFormat())) {
			cmds.add(template.getFormat());
		} else {
			cmds.add("A4");
		}
		if(StringHelper.containsNonWhitespace(template.getOrientation())) {
			cmds.add(template.getOrientation());
		} else {
			cmds.add("portrait");
		}
		
		CountDownLatch doneSignal = new CountDownLatch(1);
		ProcessWorker worker = new ProcessWorker(cmds, doneSignal);
		worker.start();

		try {
			doneSignal.await(3000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		htmlCertificateFile.delete();
		
		worker.destroyProcess();
		return certificateFile;
	}
	
	private File copyAndEnrichTemplate(File templateFile) {
		VelocityContext context = getContext();
		boolean result = false;
		File htmlCertificate = new File(templateFile.getParent(), "c" + UUID.randomUUID() + ".html");
		try(Reader in = Files.newBufferedReader(templateFile.toPath(), Charset.forName("UTF-8"));
			Writer output = new FileWriter(htmlCertificate)) {
			result = certificatesManager.getVelocityEngine().evaluate(context, output, "mailTemplate", in);
		} catch(Exception e) {
			log.error("", e);
		}
		return result ? htmlCertificate : null;
	}
	
	private VelocityContext getContext() {
		VelocityContext context = new VelocityContext();
		fillUserProperties(context);
		fillRepositoryEntry(context);
		fillCertificationInfos(context);
		fillAssessmentInfos(context);
		return context;
	}
	
	private void fillUserProperties(VelocityContext context) {
		User user = identity.getUser();
		List<UserPropertyHandler> userPropertyHandlers = userManager.getAllUserPropertyHandlers();
		for (UserPropertyHandler handler : userPropertyHandlers) {
			String propertyName = handler.getName();
			String value = handler.getUserProperty(user, null);
			context.put(propertyName, value);
		}
		
		String fullName = userManager.getUserDisplayName(identity);
		context.put("fullName", fullName);
	}
	
	private void fillRepositoryEntry(VelocityContext context) {
		String title = entry.getDisplayname();
		context.put("title", title);
		String externalRef = entry.getExternalRef();
		context.put("externalReference", externalRef);
		String authors = entry.getAuthors();
		context.put("authors", authors);
		String expenditureOfWorks = entry.getExpenditureOfWork();
		context.put("expenditureOfWorks", expenditureOfWorks);
		String mainLanguage = entry.getMainLanguage();
		context.put("mainLanguage", mainLanguage);
		
		if (entry.getLifecycle() != null) {
			Formatter format = Formatter.getInstance(locale);

			Date from = entry.getLifecycle().getValidFrom();
			String formattedFrom = format.formatDate(from);
			context.put("from", formattedFrom);

			Date to = entry.getLifecycle().getValidTo();
			String formattedTo = format.formatDate(to);
			context.put("to", formattedTo);
		}
	}
	
	private void fillCertificationInfos(VelocityContext context) {
		Formatter format = Formatter.getInstance(locale);

		if(dateCertification == null) {
			context.put("dateCertification", "");
		} else {
			String formattedDateCertification= format.formatDate(dateCertification);
			context.put("dateCertification", formattedDateCertification);
		}
		
		if(dateFirstCertification == null) {
			context.put("dateFirstCertification", "");
		} else {
			String formattedDateFirstCertification = format.formatDate(dateFirstCertification);
			context.put("dateFirstCertification", formattedDateFirstCertification);
		}
	}
	
	private void fillAssessmentInfos(VelocityContext context) {
		String roundedScore = AssessmentHelper.getRoundedScore(score);
		context.put("score", roundedScore);

		String status = (passed != null && passed.booleanValue()) ? "Passed" : "Failed";
		context.put("status", status);
	}
	
	public static boolean checkPhantomJSAvailabilty() {
		List<String> cmds = new ArrayList<String>();
		cmds.add("phantomjs");
		cmds.add("--help");
		
		CountDownLatch doneSignal = new CountDownLatch(1);
		ProcessWorker worker = new ProcessWorker(cmds, doneSignal);
		worker.start();

		try {
			doneSignal.await(3000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		log.info("PhantomJS help is available if exit value = 0: " + worker.getExitValue());
		return worker.getExitValue() == 0;
	}

	private static class ProcessWorker extends Thread {
		
		private volatile Process process;

		private int exitValue = -1;
		private final List<String> cmd;
		private final CountDownLatch doneSignal;
		
		public ProcessWorker(List<String> cmd, CountDownLatch doneSignal) {
			this.cmd = cmd;
			this.doneSignal = doneSignal;
		}
		
		public void destroyProcess() {
			if (process != null) {
				process.destroy();
				process = null;
			}
		}
		
		public int getExitValue() {
			return exitValue;
		}

		@Override
		public void run() {
			try {
				if(log.isDebug()) {
					log.debug(cmd.toString());
				}
				
				ProcessBuilder builder = new ProcessBuilder(cmd);
				process = builder.start();
				executeProcess(process);
				doneSignal.countDown();
			} catch (IOException e) {
				log.error ("Could not spawn convert sub process", e);
				destroyProcess();
			}
		}
		
		private final void executeProcess(Process proc) {
			StringBuilder errors = new StringBuilder();
			StringBuilder output = new StringBuilder();
			String line;

			InputStream stderr = proc.getErrorStream();
			InputStreamReader iserr = new InputStreamReader(stderr);
			BufferedReader berr = new BufferedReader(iserr);
			line = null;
			try {
				while ((line = berr.readLine()) != null) {
					errors.append(line);
				}
			} catch (IOException e) {
				//
			}
			
			InputStream stdout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			BufferedReader br = new BufferedReader(isr);
			line = null;
			try {
				while ((line = br.readLine()) != null) {
					output.append(line);
				}
			} catch (IOException e) {
				//
			}

			if (log.isDebug()) {
				log.debug("Error: " + errors.toString());
				log.debug("Output: " + output.toString());
			}

			try {
				exitValue = proc.waitFor();
				if (exitValue != 0) {
					log.warn("Problem with PhantomJS?");
				}
			} catch (InterruptedException e) {
				//
			}
		}
	}
}
