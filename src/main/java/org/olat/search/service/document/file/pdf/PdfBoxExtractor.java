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
package org.olat.search.service.document.file.pdf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.LimitedContentWriter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.document.file.DocumentAccessException;
import org.olat.search.service.document.file.FileContent;
import org.olat.search.service.document.file.FileDocumentFactory;

/**
 * 
 * Initial date: 19.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfBoxExtractor implements PdfExtractor {
	
	private static final OLog log = Tracing.createLoggerFor(PdfBoxExtractor.class);
	
	@Override
	public void extract(VFSLeaf document, File bufferFile)
	throws IOException, DocumentAccessException {
		FileContent content = extractTextFromPdf(document);
		storePdfTextInBuffer(content, bufferFile);
	}
	
	private void storePdfTextInBuffer(FileContent pdfText, File pdfTextFile) throws IOException {
		try(FileWriter out = new FileWriter(pdfTextFile)) {
			if(StringHelper.containsNonWhitespace(pdfText.getTitle())) {
				out.write(pdfText.getTitle());
				out.write("\u00A0|\u00A0");
			}
			out.write(pdfText.getContent());
		} catch(IOException e) {
			throw e;
		}
	}
	
	private FileContent extractTextFromPdf(VFSLeaf leaf) throws IOException, DocumentAccessException {
		if (log.isDebug()) log.debug("readContent from pdf starts...");
		PDDocument document = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(leaf.getInputStream());			
			document = PDDocument.load(bis);
			if (document.isEncrypted()) {
				try {
					document.decrypt("");
				} catch (Exception e) {
					log.warn("PDF is encrypted. Can not read content file=" + leaf.getName());
					LimitedContentWriter writer = new LimitedContentWriter(128, FileDocumentFactory.getMaxFileSize());
					writer.append(leaf.getName());
					writer.close();
					return new FileContent(leaf.getName(), writer.toString());
				}
			}	
			String title = getTitle(document);
			if (log.isDebug()) log.debug("readContent PDDocument loaded");
			PDFTextStripper stripper = new PDFTextStripper();
			LimitedContentWriter writer = new LimitedContentWriter(50000, FileDocumentFactory.getMaxFileSize());
			stripper.writeText(document, writer);
			writer.close();
			return new FileContent(title, writer.toString());
		} finally {
			if (document != null) {
			  document.close();
			}
			if (bis != null) {
				bis.close();
			}
		}
	}
	
	private String getTitle(PDDocument document) {
		if(document != null && document.getDocumentInformation() != null) {
			return document.getDocumentInformation().getTitle();
		}
		return null;
	}
}
