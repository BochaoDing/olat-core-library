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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.core.io.Resource;

/**
 * @author Mike Stock Comment:
 */
public class FileUtils {

	private static final OLog log = Tracing.createLoggerFor(FileUtils.class);
	
	private static int buffSize = 32 * 1024;
	// the following is for cleaning up file I/O stuff ... so it works fine on NFS
	public static final int BSIZE = 8*1024;

	// matches files and folders of type:
	// bla, bla1, bla12, bla.html, bla1.html, bla12.html
	private static final Pattern fileNamePattern = Pattern.compile("(.+?)\\p{Digit}*(\\.\\w{2,4})?");

	/**
	 * For security reasons Servlet containers deny requests with an encoded
	 * slash (/) or backslash (\) in the request URL (they return a 400 code).
	 * As a result, a file or directory that contains such character in its
	 * name cannot be accessed (or created by WebDAV). To prevent the creation
	 * of such named objects in the OLAT GUI, the two characters are
	 * blacklisted. However, with the help of a ZIP archive, that contains
	 * e.g. files with such malicious names, such objects can be created (but
	 * never accessed). The good thing is, that such objects can be deleted
	 * via the OLAT GUI.
	 *
	 * URL: invalid characters for a request: / \ (denied by the Servlet container, see above)
	 * Windows: invalid characters for a file name: \ / : * ? " < > | (true but such can be created via WebDAV or with the help of a ZIP archive)
	 * Linux: invalid characters for a file or directory name: / (but you have to escape certain chars like ";$%&*")
	 */
	private static final char[] FILE_NAME_FORBIDDEN_CHARS = { '/', '\\', '\n', '\r', '\t', '\f' };
	private static final char[] FILE_NAME_ACCEPTED_CHARS = { ' ' };

	static {
		Arrays.sort(FILE_NAME_FORBIDDEN_CHARS);
		Arrays.sort(FILE_NAME_ACCEPTED_CHARS);
	}

	/**
	 * @param sourceFile
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean copyFileToDir(String sourceFile, String targetDir, FileFilter filter, String wt) {
		return copyFileToDir(new File(sourceFile), new File(targetDir), false, filter, wt);
	}
	/**
	 * @param sourceFile
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean copyFileToDir(String sourceFile, String targetDir) {
		return copyFileToDir(new File(sourceFile), new File(targetDir), false, null);
	}

	/**
	 * @param sourceFile
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean moveFileToDir(String sourceFile, String targetDir, FileFilter filter, String wt) {
		return copyFileToDir(new File(sourceFile), new File(targetDir), true, filter, wt);
	}
	/**
	 * @param sourceFile
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean moveFileToDir(String sourceFile, String targetDir) {
		return copyFileToDir(new File(sourceFile), new File(targetDir), true, null);
	}

	/**
	 * @param sourceFile
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean copyFileToDir(File sourceFile, File targetDir, FileFilter filter, String wt) {
		return copyFileToDir(sourceFile, targetDir, false, filter, wt);
	}
	/**
	 * @param sourceFile
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean copyFileToDir(File sourceFile, File targetDir, String wt) {
		return copyFileToDir(sourceFile, targetDir, false, null, wt);
	}

	/**
	 * @param sourceFile
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean moveFileToDir(File sourceFile, File targetDir, FileFilter filter, String wt) {
		return copyFileToDir(sourceFile, targetDir, true, filter, wt);
	}
	/**
	 * @param sourceFile
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean moveFileToDir(File sourceFile, File targetDir) {
		return copyFileToDir(sourceFile, targetDir, true, null);
	}

	/**
	 * @param sourceDir
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean copyDirToDir(String sourceDir, String targetDir, FileFilter filter, String wt) {
		return copyDirToDir(new File(sourceDir), new File(targetDir), false, filter, wt);
	}
	/**
	 * @param sourceDir
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean copyDirToDir(String sourceDir, String targetDir) {
		return copyDirToDir(new File(sourceDir), new File(targetDir), false, null);
	}

	/**
	 * @param sourceDir
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean moveDirToDir(String sourceDir, String targetDir, FileFilter filter, String wt) {
		return moveDirToDir(new File(sourceDir), new File(targetDir), filter, wt);
	}
	/**
	 * @param sourceDir
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean moveDirToDir(String sourceDir, String targetDir, String wt) {
		return moveDirToDir(new File(sourceDir), new File(targetDir), wt);
	}

	/**
	 * @param sourceDir
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean copyDirToDir(File sourceDir, File targetDir, FileFilter filter, String wt) {
		return copyDirToDir(sourceDir, targetDir, false, filter, wt);
	}
	/**
	 * @param sourceDir
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean copyDirToDir(File sourceDir, File targetDir, String wt) {
		return copyDirToDir(sourceDir, targetDir, false, null, wt);
	}

	/**
	 * @param sourceDir
	 * @param targetDir
	 * @param filter file filter or NULL if no filter applied
	 * @return true upon success
	 */
	public static boolean moveDirToDir(File sourceDir, File targetDir, FileFilter filter, String wt) {
		return copyDirInternal(sourceDir, targetDir, true, false, filter, wt);
	}
	/**
	 * @param sourceDir
	 * @param targetDir
	 * @return true upon success
	 */
	public static boolean moveDirToDir(File sourceDir, File targetDir, String wt) {
		return copyDirInternal(sourceDir, targetDir, true, false, null, wt);
	}

	/**
	 * Get the size in bytes of a directory
	 * 
	 * @param path
	 * @return true upon success
	 */
	public static long getDirSize(File path) {
		Iterator<File> path_iterator;
		File current_file;
		long size;

		File[] f = path.listFiles();
		if (f == null) { return 0; }
		path_iterator = (Arrays.asList(f)).iterator();
		size = 0;
		while (path_iterator.hasNext()) {
			current_file = path_iterator.next();
			if (current_file.isFile()) {
				size += current_file.length();
			} else {
				size += getDirSize(current_file);
			}
		}
		return size;
	}

	/**
	 * Copy the contents of a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary.
	 * 
	 * @param sourceDir directory which contents to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param filter file filter or NULL if no filter applied
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyDirContentsToDir(File sourceDir, File targetDir, boolean move, FileFilter filter, String wt) {
		return copyDirInternal(sourceDir, targetDir, move, false, filter, wt);
	}
	/**
	 * Copy the contents of a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary.
	 * 
	 * @param sourceDir directory which contents to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyDirContentsToDir(File sourceDir, File targetDir, boolean move, String wt) {
		return copyDirInternal(sourceDir, targetDir, move, false, null, wt);
	}
	
	/**
	 * Copy a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary. The directory itself will be created on the target location.
	 * 
	 * @param sourceDir directory to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param filter file filter or NULL if no filter applied
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyDirToDir(File sourceDir, File targetDir, boolean move, FileFilter filter, String wt) {
		return copyDirInternal(sourceDir, targetDir, move, true, filter, wt);
	}
	/**
	 * Copy a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary. The directory itself will be created on the target location.
	 * 
	 * @param sourceDir directory to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyDirToDir(File sourceDir, File targetDir, boolean move, String wt) {
		return copyDirInternal(sourceDir, targetDir, move, true, null, wt);
	}

	/**
	 * Copy a directory from one spot on hard disk to another. Will create any
	 * target dirs if necessary.
	 * 
	 * @param sourceDir directory to copy on local hard disk.
	 * @param targetDir new directory to be created on local hard disk.
	 * @param move
	 * @param createDir If true, a directory with the name of the source directory will be created
	 * @param filter file filter or NULL if no filter applied
	 * @return true if the copy was successful.
	 */
	private static boolean copyDirInternal(File sourceDir, File targetDir, boolean move, boolean createDir, FileFilter filter, String wt) {
		if (sourceDir.isFile()) return copyFileToDir(sourceDir, targetDir, move, filter, wt);
		if (!sourceDir.isDirectory()) return false;

		// copy only if filter allows. filtered items are considered a success
		// and not a failure of the operation
		if (filter != null
				&& ! filter.accept(sourceDir)) return true;
		
		targetDir.mkdirs(); // this will also copy/move empty directories
		if (!targetDir.isDirectory()) return false;

		if (createDir) targetDir = new File(targetDir, sourceDir.getName());
		if (move) {
			// in case of move just rename the directory to new location. The operation might fail 
			// on a NFS or when copying accross different filesystems. In such cases, continue and copy
			// the files instead
			if (sourceDir.renameTo(targetDir)) return true;
		} // else copy structure
		
		targetDir.mkdirs();
		boolean success = true;
		String[] fileList = sourceDir.list();
		if (fileList == null) return false; // I/O error or not a directory
		for (int i = 0; i < fileList.length; i++) {
			File f = new File(sourceDir, fileList[i]);
			if (f.isDirectory()) {
				success &= copyDirToDir(f, targetDir, move, filter, wt+File.separator+f.getName());
			} else {
				success &= copyFileToDir(f, targetDir, move, filter, wt+" file="+f.getName());
			}
		}
		
		// in case of a move accross different filesystems, clean up now
		if (move) {
			sourceDir.delete();
		}
		return success;
	}

	/**
	 * Copy a file from one spot on hard disk to another. Will create any target
	 * dirs if necessary.
	 * 
	 * @param sourceFile file to copy on local hard disk.
	 * @param targetDir new file to be created on local hard disk.
	 * @param move
	 * @param filter file filter or NULL if no filter applied
	 * @return true if the copy was successful.
	 */
	public static boolean copyFileToDir(File sourceFile, File targetDir, boolean move, FileFilter filter, String wt) {
		try {
			// copy only if filter allows. filtered items are considered a success
			// and not a failure of the operation
			if (filter != null
					&& ! filter.accept(sourceFile)) return true;

			// catch if source is directory by accident
			if (sourceFile.isDirectory()) { return copyDirToDir(sourceFile, targetDir, move, filter, wt); }

			// create target directories
			targetDir.mkdirs(); // don't check for success... would return false on
			// existing dirs
			if (!targetDir.isDirectory()) return false;
			File targetFile = new File(targetDir, sourceFile.getName());

			// catch move/copy of "same" file -> buggy under Windows.
			if (sourceFile.getCanonicalPath().equals(targetFile.getCanonicalPath())) return true;
			if (move) { 
				// try to rename it first - operation might only be successful on a local filesystem!
				if (sourceFile.renameTo(targetFile)) return true;
				// it failed, so continue with copy code!
			}

			bcopy (sourceFile, targetFile, "copyFileToDir:"+wt);
			
			if (move) {
				// to finish the move accross different filesystems we need to delete the source file
				sourceFile.delete();
			}
		} catch (IOException e) {
			log.error("Could not copy file::" + sourceFile.getAbsolutePath() + " to dir::" + targetDir.getAbsolutePath(), e);
			return false;
		}
		return true;
	} // end copy
	
	/**
	 * Copy method to copy a file to another file
	 * @param sourceFile
	 * @param targetFile
	 * @param move true: move file; false: copy file
	 * @return true: success; false: failure
	 */
	public static boolean copyFileToFile(File sourceFile, File targetFile, boolean move) {
		try {
			if (sourceFile.isDirectory() || targetFile.isDirectory()) { return false; }

			// create target directories
			targetFile.getParentFile().mkdirs(); // don't check for success... would return false on
			
			// catch move/copy of "same" file -> buggy under Windows.
			if (sourceFile.getCanonicalPath().equals(targetFile.getCanonicalPath())) return true;
			if (move) { 
				// try to rename it first - operation might only be successful on a local filesystem!
				if (sourceFile.renameTo(targetFile)) return true;
				// it failed, so continue with copy code!
			}

			bcopy (sourceFile, targetFile, "copyFileToFile");
			
			if (move) {
				// to finish the move accross different filesystems we need to delete the source file
				sourceFile.delete();
			}
		} catch (IOException e) {
			log.error("Could not copy file::" + sourceFile.getAbsolutePath() + " to file::" + targetFile.getAbsolutePath(), e);
			return false;
		}
		return true;
	} // end copy

	/**
	 * Copy a file from one spot on hard disk to another. Will create any target
	 * dirs if necessary.
	 * 
	 * @param sourceFile file to copy on local hard disk.
	 * @param targetDir new file to be created on local hard disk.
	 * @param move
	 * @return true if the copy was successful.
	 */
	public static boolean copyFileToDir(File sourceFile, File targetDir, boolean move, String wt) {
			return copyFileToDir(sourceFile, targetDir, move, null, wt);
	}
	
	/**
	 * Copy an InputStream to an OutputStream.
	 * 
	 * @param source InputStream, left open.
	 * @param target OutputStream, left open.
	 * @param length how many bytes to copy.
	 * @return true if the copy was successful.
	 */
	public static boolean copy(InputStream source, OutputStream target, long length) {
		if (length == 0) return true;
		try {
			int chunkSize = (int) Math.min(buffSize, length);
			long chunks = length / chunkSize;
			int lastChunkSize = (int) (length % chunkSize);
			// code will work even when chunkSize = 0 or chunks = 0;
			byte[] ba = new byte[chunkSize];

			for (long i = 0; i < chunks; i++) {
				int bytesRead = readBlocking(source, ba, 0, chunkSize);
				if (bytesRead != chunkSize) { throw new IOException(); }
				target.write(ba);
			} // end for
			// R E A D / W R I T E last chunk, if any
			if (lastChunkSize > 0) {
				int bytesRead = readBlocking(source, ba, 0, lastChunkSize);
				if (bytesRead != lastChunkSize) { throw new IOException(); }
				target.write(ba, 0, lastChunkSize);
			} // end if
		} catch (IOException e) {
			// don't log as error - happens all the time (ClientAbortException)
			if (log.isDebug()) log.debug("Could not copy stream::" + e.getMessage() + " with length::" + length);
			return false;
		}
		return true;
	} // end copy
	
	
	/**
	 * Copy an InputStream to an OutputStream, until EOF. Use only when you don't
	 * know the length.
	 * 
	 * @param source InputStream, left open.
	 * @param target OutputStream, left open.
	 * @return true if the copy was successful.
	 */
	public static boolean copy(InputStream source, OutputStream target) {
		try {

			int chunkSize = buffSize;
			// code will work even when chunkSize = 0 or chunks = 0;
			// Even for small files, we allocate a big buffer, since we
			// don't know the size ahead of time.
			byte[] ba = new byte[chunkSize];

			while (true) {
				int bytesRead = readBlocking(source, ba, 0, chunkSize);
				if (bytesRead > 0) {
					target.write(ba, 0, bytesRead);
				} else {
					break;
				} // hit eof
			} // end while
		} catch (IOException e) {
			// don't log as error - happens all the time (ClientAbortException)
			if (log.isDebug()) log.debug("Could not copy stream::" + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Reads exactly <code>len</code> bytes from the input stream into the byte
	 * array. This method reads repeatedly from the underlying stream until all
	 * the bytes are read. InputStream.read is often documented to block like
	 * this, but in actuality it does not always do so, and returns early with
	 * just a few bytes. readBlockiyng blocks until all the bytes are read, the
	 * end of the stream is detected, or an exception is thrown. You will always
	 * get as many bytes as you asked for unless you get an eof or other
	 * exception. Unlike readFully, you find out how many bytes you did get.
	 * 
	 * @param in
	 * @param b the buffer into which the data is read.
	 * @param off the start offset of the data.
	 * @param len the number of bytes to read.
	 * @return number of bytes actually read.
	 * @exception IOException if an I/O error occurs.
	 */
	public static final int readBlocking(InputStream in, byte b[], int off, int len) throws IOException {
		int totalBytesRead = 0;

		while (totalBytesRead < len) {
			int bytesRead = in.read(b, off + totalBytesRead, len - totalBytesRead);
			if (bytesRead < 0) {
				break;
			}
			totalBytesRead += bytesRead;
		}
		return totalBytesRead;
	} // end readBlocking
	
	public static void deleteDirsAndFiles(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Get rid of ALL files and subdirectories in given directory, and all subdirs
	 * under it,
	 * 
	 * @param dir would normally be an existing directory, can be a file aswell
	 * @param recursive true if you want subdirs deleted as well
	 * @param deleteDir true if dir needs to be deleted as well
	 * @return true upon success
	 */
	public static boolean deleteDirsAndFiles(File dir, boolean recursive, boolean deleteDir) {

		boolean success = true;

		if (dir == null) return false;

		// We must empty child subdirs contents before can get rid of immediate
		// child subdirs
		if (recursive) {
			String[] allDirs = dir.list();
			if (allDirs != null) {
				for (int i = 0; i < allDirs.length; i++) {
					success &= deleteDirsAndFiles(new File(dir, allDirs[i]), true, false);
				}
			}
		}

		// delete all files in this dir
		String[] allFiles = dir.list();
		if (allFiles != null) {
			for (int i = 0; i < allFiles.length; i++) {
				File deleteFile = new File(dir, allFiles[i]);
				success &= deleteFile.delete();
			}
		}

		// delete passed dir
		if (deleteDir) {
			success &= dir.delete();
		}
		return success;
	} // end deleteDirContents

	/**
	 * @param newF
	 */
	public static void createEmptyFile(File newF) {
		try {
			FileOutputStream fos = new FileOutputStream(newF);
			fos.close();
		} catch (IOException e) {
			throw new AssertException("empty file could not be created for path " + newF.getAbsolutePath(), e);
		}

	}
	
	
	/**
	 * @param baseDir
	 * @param fileVisitor
	 */
	public static void visitRecursively(File baseDir, FileVisitor fileVisitor) {
		visit(baseDir, fileVisitor);
	}
	
	private static void visit(File file, FileVisitor fileVisitor) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				visit(f, fileVisitor);
			}
		}
		else { // regular file
			fileVisitor.visit(file);
		}
	}
	
	
	/**
	 * @param target
	 * @param data
	 * @param encoding
	 */
	public static void save(File target, String data, String encoding) {
		try {
			save(new FileOutputStream(target), data, StringHelper.check4xMacRoman(encoding));
		} catch (IOException e) {
			throw new OLATRuntimeException(FileUtils.class, "could not save file", e);
		}
	}
	
	public static InputStream getInputStream(String data, String encoding) {
		try {
			byte[] ba = data.getBytes(StringHelper.check4xMacRoman(encoding));
			ByteArrayInputStream bis = new ByteArrayInputStream(ba);
			return bis;
		} catch (IOException e) {
			throw new OLATRuntimeException(FileUtils.class, "could not save to output stream", e);
		}
	}

	/**
	 * @param target
	 * @param data
	 * @param encoding
	 */
	public static void save(OutputStream target, String data, String encoding) {
		try {
			byte[] ba = data.getBytes(StringHelper.check4xMacRoman(encoding));
			ByteArrayInputStream bis = new ByteArrayInputStream(ba);
			bcopy (bis, target, "saveDataToFile");
		} catch (IOException e) {
			throw new OLATRuntimeException(FileUtils.class, "could not save to output stream", e);
		}
	}
	
	/**
	 * Save a given input stream to a file
	 * @param source the input stream
	 * @param target the file
	 */
	public static void save(InputStream source, File target) {
		try {
			BufferedInputStream bis = new BufferedInputStream(source);
			BufferedOutputStream bos = getBos(target);
			
			cpio (bis, bos, "fileSave");
			
			bos.flush();
			bos.close();
			
			bis.close();
		} catch (IOException e) {
			throw new OLATRuntimeException(FileUtils.class, "could not save stream to file::" + target.getAbsolutePath(), e);
		}		
	}

	public static String load(Resource source, String encoding) {
    		try {
    			return load(source.getInputStream(), encoding);
    		} catch (FileNotFoundException e) {
    			throw new RuntimeException("File not found: " + source.getFilename());
    		} catch (IOException e) {
    			throw new RuntimeException("File not found: " + source.getFilename());
    		}
    	}



	/**
	 * @param source
	 * @param encoding
	 * @return the file in form of a string
	 */
	public static String load(File source, String encoding) {
		try {
			return load(new FileInputStream(source), encoding);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("could not copy file to ram: " + source.getAbsolutePath());
		}
	}

	/**
	 * @param source
	 * @param encoding
	 * @return the inpustream in form of a string
	 */
	public static String load(InputStream source, String encoding) {
		String htmltext = null;
		try {
			ByteArrayOutputStream bas = new ByteArrayOutputStream();
			boolean success = FileUtils.copy(source, bas);
			source.close();
			bas.close();
			if (!success) throw new RuntimeException("could not copy inputstream to ram");
			htmltext = bas.toString(StringHelper.check4xMacRoman(encoding));
		} catch (IOException e) {
			throw new OLATRuntimeException(FileUtils.class, "could not load from inputstream", e);
		}
		return htmltext;
	}

	/**
	 * checks whether the given File is a Directory and if it contains any files or sub-directories
	 * 
	 * 
	 * @return returns true if given File-object is a directory and contains any files or subdirectories 
	 */
	public static boolean isDirectoryAndNotEmpty(File directory){
		String[] content = directory.list();
		if(content == null) return false;
		return (content.length > 0); 
	}
	
	/**
	 * @param is the inputstream to close, may also be null
	 */
	public static void closeSafely(InputStream is) {
		if (is == null) return;
		try {
			is.close();
		} catch (IOException e) {
			// nothing to do
		}
		
	}
	
	/**
	 * @param os the outputstream to close, may also be null
	 */
	public static void closeSafely(OutputStream os) {
		if (os == null) return;
		try {
			os.close();
		} catch (IOException e) {
			// nothing to do
		}
		
	}	

	/**
	 * Extract file suffix. E.g. 'html' from index.html
	 * @param filePath
	 * @return return empty String "" without suffix. 
	 */
	public static String getFileSuffix(String filePath) {
		if(StringHelper.containsNonWhitespace(filePath)) { 
			int lastDot = filePath.lastIndexOf('.');
			if (lastDot > 0) {
				if (lastDot < filePath.length())
					return filePath.substring(lastDot + 1).toLowerCase();
			}
		}
		return "";
	}
	
	/**
	 * Simple check for filename validity. 
	 * It compares each character if it is accepted, forbidden or in a certain (Latin-1) range. <p>
	 * Characters < 33 --> control characters and space
	 * Characters > 255 --> above ASCII
	 * http://www.danshort.com/ASCIImap/
	 * TODO: control chars from 127 - 157 should also not be accepted
	 * TODO: how about non ascii chars in filenames, they should also work! See: OLAT-5704
	 * 
	 * @param filename
	 * @return true if filename valid
	 */
	public static boolean validateFilename(String filename) {
		if (filename == null) {
			return false;
		}
		
		for (int i = 0; i<filename.length(); i++) {
			char character = filename.charAt(i);
			if (Arrays.binarySearch(FILE_NAME_ACCEPTED_CHARS, character) < 0) {
				if (character < 33 || character > 255 || Arrays.binarySearch(FILE_NAME_FORBIDDEN_CHARS, character) >= 0) {
					return false;
				}
			}
		}
		//check if there are any unwanted path denominators in the name
		if (".".equals(filename) || "..".equals(filename)) {
			return false;
		}

		return true;
	}

	public static String normalizeFilename(String name) {
		String nameFirstPass = name.replace(" ", "_")
				.replace("\u00C4", "Ae")
				.replace("\u00D6", "Oe")
				.replace("\u00DC", "Ue")
				.replace("\u00E4", "ae")
				.replace("\u00F6", "oe")
				.replace("\u00FC", "ue")
				.replace("\u00DF", "ss")
				.replace("\u00F8", "o")
				.replace("\u2205", "o")
				.replace("\u00E6", "ae");
		String nameNormalized = Normalizer.normalize(nameFirstPass, Normalizer.Form.NFKD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+","");
		String nameSanitized = nameNormalized.replaceAll("\\W+", "");
		return nameSanitized;
	}
	
	
	
	/**
	 * Creates a new directory in the specified directory, using the given prefix and suffix strings to generate its name.
	 * It uses File.createTempFile() and should provide a unique name.
	 * @param prefix
	 * @param suffix
	 * @param directory
	 * @return
	 */
	public static File createTempDir(String prefix, String suffix, File directory) {
		File tmpDir = null;
		try {
			File tmpFile = File.createTempFile(prefix, suffix, directory);
			if(tmpFile.exists()) {
				tmpFile.delete();
			}
			boolean tmpDirCreated = tmpFile.mkdir();
			if(tmpDirCreated) {
			  tmpDir = tmpFile;
			}			
		} catch (Exception e) {
			//bummer!
		}
		return tmpDir;
	}
	
	public static void bcopy (File src, File dst, String wt) throws FileNotFoundException, IOException {
		bcopy (new FileInputStream(src), new FileOutputStream(dst), wt);
	}	
	public static void bcopy (InputStream src, File dst, String wt) throws FileNotFoundException, IOException {
		bcopy (src, new FileOutputStream(dst), "copyStreamToFile:"+wt);
	}
	public static BufferedOutputStream getBos (FileOutputStream of) {
		return new BufferedOutputStream (of, BSIZE);
	}
	public static BufferedOutputStream getBos (OutputStream os) {
		return new BufferedOutputStream (os, BSIZE);
	}
	public static BufferedOutputStream getBos (File of) throws FileNotFoundException {
		return getBos (new FileOutputStream(of));
	}
	public static BufferedOutputStream getBos (String fname) throws FileNotFoundException {
		return getBos (new File (fname));
	}
	
	/**
	 * Buffered copy streams (closes both streams when done)  
	 * 
	 * @param src InputStream
	 * @param dst OutputStream
	 * @throws IOException 
	 */
	public static void bcopy (InputStream src, OutputStream dst, String wt) throws IOException {

		BufferedInputStream  bis = new BufferedInputStream(src);
		BufferedOutputStream bos = getBos (dst);

		try {
			cpio (bis, bos, wt);
			bos.flush();
		} catch (IOException e) {
			throw new RuntimeException("I/O error in cpio "+wt);
		} finally {
			bos.close();
			dst.close();
			bis.close(); // no effect
			src.close(); // no effect
		}
	}
	
	/**
	 * copy in, copy out (leaves both streams open) 
	 * <p> 
	 * @see FileUtils.getBos() which creates a matching BufferedOutputStream
	 * </p>
	 * 
	 * @param in BuferedInputStream
	 * @param out BufferedOutputStream
	 * @param wt What this I/O is about
	 */
	public static long cpio (InputStream in, OutputStream out, String wt) throws IOException {
		
		byte[] buffer = new byte[BSIZE];

		int c;
		long tot = 0;
		long s = 0;
		boolean debug = log.isDebug();
		if(debug) {
			s = System.nanoTime();
		}
		
		while ((c = in.read(buffer, 0, buffer.length)) != -1) {
    		out.write(buffer, 0, c);
    		tot += c;
		}
		
		if(debug) {
			long tim = System.nanoTime() - s;
			double dtim = tim == 0 ? 0.5 : tim; // avg of those less than 1 nanoseconds is taken as 0.5 nanoseconds
			double bps = tot*1000*1000/dtim;
			log.debug(String.format("cpio %,13d bytes %6.2f ms avg %6.1f Mbps %s%n", tot, dtim/1000/1000, bps/1024, wt));
		}
		return tot;
	}
	
	/**
	 * from a newer version of apache commons.io Determines whether the specified
	 * file is a Symbolic Link rather than an actual file.
	 * <p>
	 * Will not return true if there is a Symbolic Link anywhere in the path, only
	 * if the specific file is.
	 * 
	 * @param file the file to check
	 * @return true if the file is a Symbolic Link
	 * @throws IOException if an IO error occurs while checking the file
	 * @since Commons IO 2.0
	 */
	public static boolean isSymlink(File file) throws IOException {
		if (file == null) { throw new NullPointerException("File must not be null"); }
		if ("\\".equals(File.separatorChar) ) { return false; } // Windows doesn't know symlinks!
		File fileInCanonicalDir = null;
		if (file.getParent() == null) {
			fileInCanonicalDir = file;
		} else {
			File canonicalDir = file.getParentFile().getCanonicalFile();
			fileInCanonicalDir = new File(canonicalDir, file.getName());
		}

		if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
			return false;
		} else {
			return true;
		}
	}
	
	public static String rename(File f) {
		String filename = f.getName();
		String newName = filename;
		File newFile = f;
		for(int count=0; newFile.exists() && count < 999 ; ) {
			count++;
			newName = appendNumberAtTheEndOfFilename(filename, count);
			newFile = new File(f.getParentFile(), newName);
		}
		if(!newFile.exists()) {
			return newName;
		}
		return null;
	}
	
	/**
	 * Sticks together a new filename. If there's a match with a common filename
	 * with extension, add the counter to the end of the filename before the
	 * extension. Else just add the counter to the end of the name. E.g.:
	 * hello.xml => hello1.xml where 1 is the counter
	 * hello1.xml => hello2.xml
	 * blaber 	 => blaber1
	 * blaber1 	 => blaber2
	 * 
	 * @param name
	 * @param number
	 * @return The new name with the counter added
	 */
	public static String appendNumberAtTheEndOfFilename(String name, int number) {
		// Try to match the file to the pattern "[name].[extension]"
		Matcher m = fileNamePattern.matcher(name);
		StringBuilder newName = new StringBuilder();
		if (m.matches()) {
			newName.append(m.group(1)).append(number);
			if (m.group(2) != null) {
				// is null in case it was not a file or does not contain a file ending.
				newName.append(m.group(2));
			}
		} else {
			newName.append(name).append(number);
		}
		return newName.toString();
	}
}