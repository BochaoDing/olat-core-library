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
package org.olat.modules.video;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.video.manager.VideoExportMediaResource;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * The <code>VideoManager</code> singleton is responsible for dealing with video
 * resources.
 *
 * Initial date: 01.04.2015<br>
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public interface VideoManager {

	public abstract File getVideoFile(OLATResource videoResource);

	public abstract VFSLeaf getPosterframe(OLATResource videoResource);

	public abstract void setPosterframe(OLATResource videoResource, VFSLeaf posterframe);

	public abstract HashMap<String, VFSLeaf> getAllTracks(OLATResource videoResource);

	public abstract void addTrack(OLATResource videoResource, String lang, VFSLeaf trackFile);

	public abstract VFSLeaf getTrack(OLATResource videoResource, String lang);

	public abstract void removeTrack(OLATResource videoResource, String lang);

	public abstract boolean getFrame(OLATResource videoResource, int frameNumber, VFSLeaf frame) throws IOException;

	/**
	 * Read the the metdatadata-xml in the videoresource folder
	 * @param videoResource
	 * @return
	 */
	public VideoMetadata readVideoMetadataFile(OLATResource videoResource);

	/**
	 * Trigger the transcoding process to generate versions of the video
	 * 
	 * @param video
	 * @return
	 */
	public abstract void startTranscodingProcess(OLATResource video);
	
	public abstract List<VideoTranscoding> getVideoTranscodings(OLATResource video);

	/**
	 * Get a human readable aspect ratio from the given video size. Recognizes
	 * the most common aspect ratios
	 * 
	 * @param width
	 * @param height
	 * @return String containing a displayable aspect ratio
	 */
	public abstract String getAspectRatio(int width, int height);

	/**
	 * Create a display title for the given resolution. The title uses the i18n
	 * keys for standard resolutions. For non standard resolutions, the
	 * "original" translation is used
	 * 
	 * @param resolution The resolution of the video
	 * @param translator The translator to be used
	 * @return The display title for this resolution
	 */
	public abstract String getDisplayTitleForResolution(int resolution, Translator translator);

	/**
	 * Get the master container for this resource where the actual video is stored
	 * @param videoResource
	 * @return VFSContainer
	 */
	public abstract VFSContainer getMasterContainer(OLATResource videoResource);
	
	/**
	 * Get the container where all the transcoded videos are stored
	 * @param videoResource
	 * @return VFSContainer
	 */
	public abstract VFSContainer getTranscodingContainer(OLATResource videoResource);
	
	/**
	 * Get the master video file 
	 * @param videoResource
	 * @return VFSLeaf or NULL if it does not exist
	 */
	public abstract VFSLeaf getMasterVideoFile(OLATResource videoResource);
	
	/**
	 * Get a media resource which represents an export of a video resource
	 * @param repoEntry
	 * @return VideoExportMediaResource which generates a ZIP file on the fly
	 */
	public abstract VideoExportMediaResource getVideoExportMediaResource(RepositoryEntry repoEntry);

	/**
	 * Check if the given file is an archive generated by the export (and thus
	 * can be imported again). The validation callback is set to true if valid
	 * and the title and description are extracted
	 * 
	 * @param file
	 *            The zip file handle
	 * @param eva
	 *            the resource validation callback
	 * @return true: recognized as video resource; false: not recognized
	 */
	public abstract void validateVideoExportArchive(File file,  ResourceEvaluation eval);

	/**
	 * Import the given file to the resource on disk
	 * @param repoEntry 
	 *            The repository entry that represents the video in the repository
	 * @param masterVideo The video file to be added to the repository. Must be an mp4 file.
	 */
	public abstract boolean importFromMasterFile(RepositoryEntry repoEntry, VFSLeaf masterVideo);

	/**
	 * Import the given export archive to the resource on disk
	 * 
	 * @param repoEntry
	 *            The repository entry that represents the video in the repository
	 * @param exportArchive
	 *            The archive to be added to the repository. The archive must be
	 *            created by the video export feature.
	 */
	public abstract boolean importFromExportArchive(RepositoryEntry repoEntry, VFSLeaf exportArchive);

	/**
	 * Update video transcoding
	 * 
	 * @param videoTranscoding
	 * @return VideoTranscoding the updated transcoding object
	 */
	public VideoTranscoding updateVideoTranscoding(VideoTranscoding videoTranscoding);

	/**
	 * Copy video resource to identical new video resource. 
	 * @param sourceResource the existing video resource
	 * @param targetResource the empty new resource
	 */
	public abstract void copyVideo(OLATResource sourceResource, OLATResource targetResource);

	/**
	 * Delete the video transcodings on disk an in database
	 * @param videoResource
	 * @return true: success; false: failed
	 */
	public abstract boolean deleteVideoTranscodings(OLATResource videoResource);

}