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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.util.vfs;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.version.VFSRevision;

public class VFSRevisionMediaResource implements MediaResource {
	
	private static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";

	private String encoding;
	private boolean forceDownload = false;
	private boolean unknownMimeType = false;
	private final VFSRevision revision;
	
	public VFSRevisionMediaResource(VFSRevision version) {
		this(version,false);
	}
	
	public VFSRevisionMediaResource(VFSRevision revision, boolean forceDownload) {
		this.revision = revision;
		this.forceDownload = forceDownload;
	}

	public String getContentType() {
		String mimeType = WebappHelper.getMimeType(revision.getName());
		if (mimeType == null) {
			mimeType = MIME_TYPE_OCTET_STREAM;
			unknownMimeType = true;
		} else {
			// if any encoding is set, append it for the browser
			if (encoding != null) {
				mimeType = mimeType + ";charset=" + encoding;
				unknownMimeType = false;
			}
		}
		return mimeType;
	}

	public InputStream getInputStream() {
		return revision.getInputStream();
	}

	public Long getLastModified() {
		long lastModified = revision.getLastModified();
		return (lastModified == VFSConstants.UNDEFINED) ? null : new Long(lastModified);
	}

	public Long getSize() {
		long size = revision.getSize();
		return (size == VFSConstants.UNDEFINED) ? null : new Long(size);
	}

	public void prepare(HttpServletResponse hres) {
		String filename = "";
		try {
			filename = URLEncoder.encode(revision.getName(), "utf-8");
		} catch (UnsupportedEncodingException wontHappen) {
			//nothing
		}
		if (forceDownload || unknownMimeType)
			hres.setHeader("Content-Disposition", "attachment; filename=" + filename);
		else
			hres.setHeader("Content-Disposition", "filename=" + filename);
	}

	public void release() {
		//do nothing
	}
}
