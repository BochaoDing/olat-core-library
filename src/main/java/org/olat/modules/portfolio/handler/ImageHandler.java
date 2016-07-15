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
package org.olat.modules.portfolio.handler;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaInformations;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.manager.MediaDAO;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.editor.InteractiveAddPageElementHandler;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageElementAddController;
import org.olat.modules.portfolio.ui.media.CollectImageMediaController;
import org.olat.modules.portfolio.ui.media.ImageMediaController;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ImageHandler extends AbstractMediaHandler implements InteractiveAddPageElementHandler {
	
	public static final String IMAGE_TYPE = "image";
	private final AtomicInteger idGenerator = new AtomicInteger();

	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private PortfolioFileStorage fileStorage;
	
	public ImageHandler() {
		super(IMAGE_TYPE);
	}
	
	@Override
	public String getIconCssClass() {
		return "o_filetype_jpg";
	}

	@Override
	public String getIconCssClass(MediaLight media) {
		String filename = media.getRootFilename();
		if (filename != null){
			return CSSHelper.createFiletypeIconCssClassFor(filename);
		}
		return "o_filetype_jpg";
	}

	@Override
	public VFSLeaf getThumbnail(MediaLight media, Size size) {
		String storagePath = media.getStoragePath();

		VFSLeaf thumbnail = null;
		if(StringHelper.containsNonWhitespace(storagePath)) {
			VFSContainer storageContainer = fileStorage.getMediaContainer(media);
			VFSItem item = storageContainer.resolve(media.getRootFilename());
			if(item instanceof VFSLeaf) {
				VFSLeaf leaf = (VFSLeaf)item;
				if(leaf instanceof MetaTagged) {
					MetaInfo metaInfo = ((MetaTagged)leaf).getMetaInfo();
					thumbnail = metaInfo.getThumbnail(size.getHeight(), size.getWidth(), true);
				}
			}
		}
		
		return thumbnail;
	}

	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		String description = null;
		if (mediaObject instanceof MetaTagged) {
			MetaInfo meta = ((MetaTagged)mediaObject).getMetaInfo();
			title = meta.getTitle();
			description = meta.getComment();
		}
		return new Informations(title, description);
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		return null;
	}
	
	public Media createMedia(String title, String description, File file, String filename, String businessPath, Identity author) {
		Media media = mediaDao.createMedia(title, description, filename, IMAGE_TYPE, businessPath, 60, author);
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, filename);
		FileUtils.copyFileToFile(file, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		mediaDao.updateStoragePath(media, storagePath, filename);
		return media;
	}

	@Override
	public Media createMedia(AbstractArtefact artefact) {
		return null;//no specific image document in old portfolio
	}
	
	@Override
	public Component getContent(UserRequest ureq, WindowControl wControl, PageElement element) {	
		if(element instanceof Media) {
			return getContent(ureq, (Media)element);
		}
		if(element instanceof MediaPart) {
			MediaPart mediaPart = (MediaPart)element;
			return getContent(ureq, mediaPart.getMedia());
		}
		return null;
	}
	
	private ImageComponent getContent(UserRequest ureq, Media media) {
		File mediaDir = new File(FolderConfig.getCanonicalRoot(), media.getStoragePath());
		File mediaFile = new File(mediaDir, media.getRootFilename());
		ImageComponent imageCmp = new ImageComponent(ureq.getUserSession(), "image_" + idGenerator.incrementAndGet());
		imageCmp.setMedia(mediaFile);
		return imageCmp;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new ImageMediaController(ureq, wControl, media);
	}
/*
	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		return new ImageMediaEditorController(ureq, wControl, (MediaPart)element);
	}*/

	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
		return new CollectImageMediaController(ureq, wControl);
	}
}
