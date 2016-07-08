package org.olat.modules.portfolio.handler;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 08.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderTemplateMediaResource implements MediaResource {
	
	private static final OLog log = Tracing.createLoggerFor(BinderTemplateMediaResource.class);
	
	private final BinderRef template;
	private final RepositoryEntry templateEntry;
	
	public BinderTemplateMediaResource(BinderRef template, RepositoryEntry templateEntry) {
		this.template = template;
		this.templateEntry = templateEntry;
	}
	
	@Override
	public boolean acceptRanges() {
		return false;
	}
	
	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void release() {
		//
	}
	
	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}
		
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
			Binder loadedTemplate = portfolioService.getBinderByKey(template.getKey());
			String label = loadedTemplate.getTitle();
			String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

			String file = secureLabel + ".zip";
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));

			zout.setLevel(9);
			zout.putNextEntry(new ZipEntry("binder.xml"));
			BinderXStream.toStream(loadedTemplate, zout);
			zout.closeEntry();
			
			if(StringHelper.containsNonWhitespace(loadedTemplate.getImagePath())) {
				File posterImage = portfolioService.getPosterImageFile(loadedTemplate);
				if(posterImage.exists()) {
					zout.putNextEntry(new ZipEntry(loadedTemplate.getImagePath()));
					FileUtils.copyFile(posterImage, new ShieldOutputStream(zout));
					zout.closeEntry();
				}
			}
			
			OLATResource resource = templateEntry.getOlatResource();
			File baseContainer= FileResourceManager.getInstance().getFileResource(resource);
			RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(templateEntry, baseContainer);
			importExport.exportDoExportProperties(zout);
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
