package wb.android.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

//There are a lot of helper methods here to make sure that no functionality is lost if a method is overwritten
public class StorageManager {
	
	private static final String TAG = "StorageManager";
	private static final boolean D = true;
	
	protected File _root;
	
	private static SDCardFileManager _externalInstance = null;
	private static InternalStorageManager _internalInstance = null;
	
	protected StorageManager(File root) {
		_root = root;
	}
	
	public static StorageManager getInstance(Context context) {
		if (_externalInstance != null)
			return _externalInstance;
		try {
			_externalInstance = new SDCardFileManager(context);
			return _externalInstance;
		} catch (SDCardStateException e) {
			if (_internalInstance != null)
				return _internalInstance;
			else {
				_internalInstance = new InternalStorageManager(context);
				return _internalInstance;
			}
		}
	}
	
	public static final SDCardFileManager getExternalInstance(Context context) throws SDCardStateException {
		if (_externalInstance != null)
			return _externalInstance;
		_externalInstance = new SDCardFileManager(context);
		return _externalInstance;
	}
	
	public static final SDCardFileManager getExternalInstance(Context context, String[] allowedStates) throws SDCardStateException {
		if (_externalInstance != null)
			return _externalInstance;
		_externalInstance = new SDCardFileManager(context, allowedStates);
		return _externalInstance;
	}
	
	public static final InternalStorageManager getInternalInstance(Context context) {
		if (_internalInstance != null)
			return _internalInstance;
		_internalInstance = new InternalStorageManager(context);
		return _internalInstance;
	}
	
	public static final File getDownloadsFolder() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	}
	
	public static String GetRootPath() {
		 if (_externalInstance != null)
			 return _externalInstance._root.getAbsolutePath();
		 else if (_internalInstance != null)
			 return _internalInstance._root.getAbsolutePath();
		 else
			 return "";
	}
	
	public File getRoot() {
		return new File(_root.getAbsolutePath());
	}
	
	public String getRootPath() {
		return _root.getAbsolutePath();
	}

	public boolean isRoot(final File dir) {
		return dir.equals(_root);
	}
	
	// Returns the directory if successfully built. Returns null otherwise
	public File mkdir(final String name) {
		return mkdirHelper(_root, name);
	}
	
	public File mkdir(final File root, final String name) {
		return mkdirHelper(root, name);
	}
	
	private final File mkdirHelper(final File root, final String name) {
		final File dir = new File(root, name);
		if (!dir.exists()) {
			final boolean success = dir.mkdir();
			if (!success)
				return null;
		}
		return dir;
	}
	
	public File getFile(final String filename) {
		return new File(_root, filename);
	}
	
	public File getFile(final File root, final String filename) {
		return new File(root, filename);
	}
	
	public File rename(final File oldFile, final String newName) {
		final File file = this.getFile(newName);
		final boolean success = oldFile.renameTo(file);
		if (success)
			return file;
		else
			return oldFile;
	}
	
	public boolean delete(final String filename) {
		return deleteHelper(new File(_root, filename));
	}
	
	public boolean delete(final File dir, final String filename) {
		return deleteHelper(new File(dir, filename));
	}
	
	public boolean delete(final File file) {
		return deleteHelper(file);
	}
	
	private final boolean deleteHelper(final File file) {
		if (file == null || !file.exists())
			return true;
		if  (!file.canWrite())
			return false;
		if (file.isDirectory() && file.listFiles().length > 0)
			return false;
		return file.delete();
	}
	
	public boolean deleteRecursively(final File dir) {
		if (dir == null || !dir.exists())
			return true;
		if (!dir.canWrite())
			return false;
		if (dir.isDirectory()) {
			final File[] files = dir.listFiles();
			final int len = files.length;
			for (int i = 0; i < len; i++) 
				deleteRecursively(files[i]);
			if (dir.listFiles().length > 0)
				return false;
		}
		return dir.delete();
	}
	
	public boolean deleteAll() {
		final File[] files = _root.listFiles();
		final int len = files.length;
		for (int i = 0; i < len; i++) 
			deleteRecursively(files[i]);
		return (_root.listFiles().length == 0);
	}
	
	public boolean write(final String filename, final byte[] data) {
		return writeHelper(_root, filename, data);
	}
	
	public boolean write(final File dir, final String filename, final byte[] data) {
		return writeHelper(dir, filename, data);
	}
	
	private final boolean writeHelper(final File dir, final String filename, final byte[] data) {
		if (data == null || filename == null || dir == null)
			return false;
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path + filename);
			fos.write(data);
			fos.close();
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {}
		}
		return true;
	}
	
	public boolean write(final String filename, final String data) {
		return writeHelper(_root, filename, data);
	}
	
	public boolean write(final File dir, final String filename, final String data) {
		return writeHelper(dir, filename, data);
	}
	
	private final boolean writeHelper(final File dir, final String filename, final String data) {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path + filename);
			fos.write(data.getBytes());
			fos.close();
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {}
		}
		return true;
	}
	
	public boolean createFile(final File file) {
		try {
			if (!file.exists()) {
				return file.createNewFile();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public boolean writeBitmap(final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		return writeBitmapHelper(_root, bitmap, filename, format, quality);
	}
	
	public boolean writeBitmap(final File dir, final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		return writeBitmapHelper(dir, bitmap, filename, format, quality);
	}
	
	private final boolean writeBitmapHelper(final File dir, final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path + filename);
			bitmap.compress(format, quality, fos);
			fos.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {}
		}
		return true;
	}
	
	public boolean writeBitmap(final Uri imageUri, final Bitmap bitmap, final CompressFormat format, int quality) {
		if (imageUri == null || bitmap == null || format == null) {
			Log.e(TAG, "Invalid parameters - cannot supply null values to writeBitmap method");
			return false;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(imageUri.getPath());
			bitmap.compress(format, quality, fos);
			fos.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {}
		}
		return true;
	}
		
	public byte[] read(final File f) {
		byte[] buffer;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			buffer = new byte[(int)f.length()];
			fis.read(buffer);
			fis.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			buffer = null;
			return buffer;
		} finally {
			try {
				if (fis != null)
					fis.close();
			}
			catch (IOException e) {
				return null;
			}
		}
		return buffer;
	}
	
	public byte[] read(final InputStream is) {
		if (is== null) Log.d(TAG, "null");
		ByteArrayOutputStream os = null;
		byte[] bytes = null;
		try {
			os = new ByteArrayOutputStream();
			byte[] buffer = new byte[2048];
			int n = 0;
			while (-1 != (n = is.read(buffer))) {
				os.write(buffer, 0, n);
			}
			bytes = os.toByteArray();
			os.close();
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
			return null;
		}
		finally {
			try {
				if (os != null)
					os.close();
			}
			catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		return bytes;
	}
	
	public File changeExtension(File source, String newExtension) {
		try {
			if (!newExtension.startsWith("."))
				newExtension = "." + newExtension;
			String path = source.getCanonicalPath();
			path = path.substring(0, path.lastIndexOf('.'));
			path = path + newExtension;
			File destination = new File(path);
			if(source.renameTo(destination))
				return destination;
			else
				return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	public boolean move(File source, File destination) {
		return source.renameTo(destination);
	}
	
	public boolean copy(File destination, boolean overwrite) throws IOException {
		return copyHelper(_root, destination, overwrite);
	}
	
	public boolean copy(File source, File destination, boolean overwrite) throws IOException {
		return copyHelper(source, destination, overwrite);
	}
	
	private boolean copyHelper(File source, File destination, boolean overwrite) throws IOException {
		if (source == null || destination == null || !source.exists()) {
			Log.e(TAG, "Invalid Inputs. Either the source/destination is null or the source does not exist");
			return false;
		}
		try {
			if (source.getCanonicalPath().equalsIgnoreCase(destination.getCanonicalPath())) {
				Log.e(TAG, "Invalid Inputs. The source File cannot have the same path as the destination File");
				return false;
			}
		} catch (IOException e) {
			throw e;
		}
		if (source.isDirectory()) {
			if (!destination.exists()) {
				if (!destination.mkdir()) {
					throw new IOException("Failed to create the directory: " + destination.getAbsolutePath());
				}
			}
			else {
				if (!destination.isDirectory()) {
					throw new IOException("Cannot copy a directory to a file");
				}
			}
			File[] files = listFilesAndDirectories(source);
			for (int i=0; i<files.length; i++) {
				File sourceSub = files[i];
				File destSub = getFile(destination, sourceSub.getName());
				copy(sourceSub, destSub, overwrite);
			}
		}
		else {
			if (destination.exists() && !overwrite)
				return false;
			if (destination.isDirectory())
				destination = getFile(destination, source.getName());
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				fis = new FileInputStream(source);
				fos = new FileOutputStream(destination);
				byte[] buffer = new byte[8192];
				int read;
				while((read = fis.read(buffer)) != -1) {
					fos.write(buffer, 0, read);
				}
				fis.close();
				fos.close();
			}
			catch (IOException e) {
				throw e;
			}
			finally {
				try {
					if (fis != null)
						fis.close();
				}
				catch (IOException e) {
					Log.e(TAG, e.toString());
				}
				try {
					if (fos != null)
						fos.close();
				}
				catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
		return true;
	}
	
	/**
	 * The caller is responsible for closing the InputStream
	 * @param is
	 * @param destination
	 * @param overwrite
	 * @return
	 * @throws IOException
	 */
	public boolean copy(InputStream is, File destination, boolean overwrite) throws IOException {
		if (is == null || destination == null) {
			Log.e(TAG, "Invalid Inputs. Either the source/destination is null");
			return false;
		}
		if (destination.exists() && !overwrite)
			return false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(destination);
			byte[] buffer = new byte[2048];
			int read;
			while((read = is.read(buffer)) != -1) {
				fos.write(buffer, 0, read);
			}
			fos.close();
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		return true;
	}
	
	public File[] listFilesAndDirectories() {
		return _root.listFiles();
	}
	
	public File[] listFilesAndDirectories(final File root) {
		return root.listFiles();
	}
	
	public File[] listFiles() {
		return listFilesHelper(_root);
	}
	
	public File[] listFiles(final File root) {
		return listFilesHelper(root);
	}
	
	private final File[] listFilesHelper(final File root) {
		FileFilter filesFilter = new FileFilter() {
			public final boolean accept(File file) {
				return !file.isDirectory();
			}
		};
		return this.list(root, filesFilter);
	}
	
	public File[] listDirs() {
		return listDirsHelper(_root);
	}
	
	public File[] listDirs(final File root) {
		return listDirsHelper(root);
	}
	
	private final File[] listDirsHelper(final File root) {
		FileFilter dirsFilter = new FileFilter() {
			public final boolean accept(File file) {
				return file.isDirectory();
			}
		};
		return this.list(root, dirsFilter);
	}
	
	public File[] list(final FileFilter filter) {
		return _root.listFiles(filter);
	}
	
	public File[] list(final File root, final FileFilter filter) {
		return root.listFiles(filter);
	}
	
	public File[] list(final String extension) {
		return listHelper(_root, extension);
	}
	
	public File[] list(final File root, final String extension) {
		return listHelper(root, extension);
	}
	
	private final File[] listHelper(final File root, final String extension) {
		FileFilter ff = new FileFilter() {
			@Override
			@SuppressLint("DefaultLocale")
			public boolean accept(File pathname) {
				if (pathname.getName().toLowerCase().endsWith(extension))
					return true;
				else
					return false;
			}
		};
		return list(root, ff);
	}
	
	public Bitmap getBitmap(final String filename) {
		return getBitmapHelper(_root, filename);
	}
	
	public Bitmap getBitmap(final File root, final String filename) {
		return getBitmapHelper(root, filename);
	}
	
	private final Bitmap getBitmapHelper(final File root, final String filename) {
		try {
			File path = new File(root, filename);
			return BitmapFactory.decodeFile(path.getCanonicalPath());
		} catch (IOException e) {
			return null;
		}
	}
	
	public Bitmap getMutableMemoryEfficientBitmap(File file) {
		return getMutableMemoryEfficientBitmapHelper(file, Bitmap.Config.RGB_565, 1024);
	}
	
	public Bitmap getMutableMemoryEfficientBitmap(File file, Bitmap.Config config) {
		return getMutableMemoryEfficientBitmapHelper(file, config, 1024);
	}
	
	public Bitmap getMutableMemoryEfficientBitmap(File file, int maxDimension) {
		return getMutableMemoryEfficientBitmapHelper(file, Bitmap.Config.RGB_565, 1024);
	}
	
	public Bitmap getMutableMemoryEfficientBitmap(File file, Bitmap.Config config, int maxDimension) {
		return getMutableMemoryEfficientBitmapHelper(file, config, maxDimension);
	}
	
	private final Bitmap getMutableMemoryEfficientBitmapHelper(File file, Bitmap.Config config, int maxDimension) {
		try {
			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(file),null,o);
			
			int scale=1;
			while(o.outWidth/scale/2>=maxDimension && o.outHeight/scale/2>=maxDimension)
				scale*=2;
			
			//Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize=scale;
			o2.inPurgeable = true;
			o2.inInputShareable = true;
			o2.inDither = true;
			o2.inPreferredConfig = config;
			return BitmapFactory.decodeFile(file.getAbsolutePath(), o2).copy(config, true);
		} 
		catch (FileNotFoundException e) {
			return null;
		}
	}
	
	public boolean isExternal() {
		return (this instanceof SDCardFileManager);
	}
	
	public FileOutputStream getFOS(String filename) throws FileNotFoundException {
		return getFOSHelper(_root, filename);
	}
	
	public FileOutputStream getFOS(File dir, String filename) throws FileNotFoundException {
		return getFOSHelper(dir, filename);
	}
	
	private final FileOutputStream getFOSHelper(File dir, String filename) throws FileNotFoundException {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		return new FileOutputStream(path + filename);
	} 
	
	public File zipBuffered(int buffer) {
		return zipBufferedHelper(_root, buffer, null);
	}
	
	public File zipBuffered(int buffer, FileFilter filter) {
		return zipBufferedHelper(_root, buffer, filter);
	}
	
	public File zipBuffered(File inputDir, int buffer) {
		return zipBufferedHelper(inputDir, buffer, null);
	}
	
	public File zipBuffered(File inputDir, int buffer, FileFilter filter) {
		return zipBufferedHelper(inputDir, buffer, filter);
	}
	
	//Uses a buffer to conserver memory
	private File zipBufferedHelper(File inputDir, int buffer, FileFilter filter) { 
		//What if file exists
		if (!inputDir.isDirectory()) {
			Log.e(TAG, "The input is not a directory");
			return null;
		}
		File zipFile = (inputDir.getParentFile() != null) ? getFile(inputDir.getParentFile(), inputDir.getName() + ".zip") : getFile(inputDir.getName() + ".zip"); 		
		ZipOutputStream zipStream = null;
		try {  	
			zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
			zipBufferedRecursively(inputDir, inputDir, zipStream, buffer, filter);
			zipStream.close(); 
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return null;
		} finally {
			try {
				if (zipStream != null)
					zipStream.close();
			}
			catch (IOException e) { Log.e(TAG, e.toString()); }
		}
		return zipFile;
	}
	
	private void zipBufferedRecursively(File file, File base, ZipOutputStream zipStream, int buffer, FileFilter filter) throws IOException {
		if (file.isDirectory()) {
			File[] files;
			if (filter == null)
				files = listFilesAndDirectories(file);
			else
				files = list(file, filter);
			for(int i=0; i < files.length; i++) { 
				zipBufferedRecursively(files[i], base, zipStream, buffer, filter);
			}
		}
		else {
			ZipEntry entry = new ZipEntry(file.getPath().substring(base.getPath().length() + 1)); 
			zipStream.putNextEntry(entry);
			BufferedInputStream reader = null;
			try {
				reader = new BufferedInputStream(new FileInputStream(file), buffer); 
				int count; 
				byte[] bytes = new byte[buffer];
				while ((count = reader.read(bytes, 0, buffer)) != -1) { 
					zipStream.write(bytes, 0, count); 
				} 
				zipStream.write(bytes);
				reader.close();
			}
			finally {
				if (reader != null)
					reader.close();
			}
		}
	}
	
	public File zip() {
		return zipHelper(_root);
	}
	
	public File zip(File inputDir) {
		return zipHelper(inputDir);
	}
	
	private File zipHelper(File inputDir) { 
		File zipFile = (inputDir.getParentFile() != null) ? getFile(inputDir.getParentFile(), inputDir.getName() + ".zip") : getFile(inputDir.getName() + ".zip"); 		
		ZipOutputStream zipStream = null;
		try {  	
			zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
			zipRecursively(inputDir, zipStream);
			zipStream.close(); 
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return null;
		} finally {
			try {
				if (zipStream != null)
					zipStream.close();
			}
			catch (IOException e) { Log.e(TAG, e.toString()); return null; }
		}
		return zipFile;
	}
	
	private void zipRecursively(File file, ZipOutputStream zipStream) throws IOException {
		if (file.isDirectory()) {
			if (D) Log.v(TAG, "Zipping: " + file.getName());
			File[] files = listFilesAndDirectories(file); 
			for(int i=0; i < files.length; i++) { 
				zipRecursively(files[i], zipStream);
			}
		}
		else {
			ZipEntry entry = new ZipEntry(file.getName()); 
			zipStream.putNextEntry(entry);
			byte[] bytes = read(file); 
			zipStream.write(bytes);
		}
	}
	
	public boolean unzip(File zip, boolean overwrite) {
		return unzipHelper(zip, _root, 8192, overwrite);
	}
	
	public boolean unzip(File zip, int buffer, boolean overwrite) {
		return unzipHelper(zip, _root, buffer, overwrite);
	}
	
	public boolean unzip(File zip, File extractTo, boolean overwrite) {
		return unzipHelper(zip, extractTo, 8192, overwrite);
	}
	
	public boolean unzip(File zip, File extractTo, int buffer, boolean overwrite) {
		return unzipHelper(zip, extractTo, buffer, overwrite);
	}
	
	private boolean unzipHelper(File zip, File extractTo, int buffer, boolean overwrite) {
		InputStream in = null;
		BufferedOutputStream out = null;
		try {
			ZipFile archive = new ZipFile(zip);
			Enumeration<? extends ZipEntry> e = archive.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				File file = new File(extractTo, entry.getName());
				if (entry.getName().endsWith(".zip"))
					continue; //Fix in the future. Currently, just ignore nested .zip files
				File parent = file.getParentFile();
				if (!parent.exists())
					parent.mkdirs();
				if (entry.isDirectory())
					file.mkdirs();
				else {
					if (!overwrite && file.exists())
						continue;
					in = archive.getInputStream(entry);
					out = new BufferedOutputStream(new FileOutputStream(file));
					byte[] bytes = new byte[buffer];
					int read;
					while (-1 != (read = in.read(bytes))) {
						out.write(bytes, 0, read);
					}
					in.close();
					out.close();
				}
			}
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
			catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		return true;
	}
	
	public static String getMD5Checksum(InputStream is) {
		byte[] buffer = new byte[1024];
		MessageDigest complete;
		try {
			complete = MessageDigest.getInstance("MD5");
			try {
				int numRead;
				do {
					numRead = is.read(buffer);
					if (numRead > 0)
						complete.update(buffer, 0, numRead);
				} while (numRead != -1);
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				return null;
			}
			byte[] b = complete.digest();
			StringBuilder builder = new StringBuilder();
			for (int i=0; i < b.length; i++) {
				builder.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
			}
			return builder.toString();
		}
		catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}
	
	public static String getMD5Checksum(File file) {
		FileInputStream fis = null;
		byte[] buffer = new byte[1024];
		MessageDigest complete;
		try {
			complete = MessageDigest.getInstance("MD5");
			try {
				fis = new FileInputStream(file);
				int numRead;
				do {
					numRead = fis.read(buffer);
					if (numRead > 0)
						complete.update(buffer, 0, numRead);
				} while (numRead != -1);
				fis.close();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				return null;
			} finally {
				try {
					if (fis != null)
						fis.close();
				} 
				catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
			
			byte[] b = complete.digest();
			StringBuilder builder = new StringBuilder();
			for (int i=0; i < b.length; i++) {
				builder.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
			}
			return builder.toString();
		}
		catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}
	
	public void print() {
		printHelper(_root, "> ");
	}
	
	private void printHelper(File file, String indent) {
		if (file.isDirectory()) {
			Log.d(TAG, indent + File.separatorChar + file.getName() + " -- " + file.getAbsolutePath());
			for(File child : file.listFiles())
				printHelper(child, "  " + indent);
		}
		else {
			Log.d(TAG, indent + file.getName());
		}
	}
	
	/**
	 * Returns the file extension. Returns null if the file is invalid (null, nameless, directory)
	 * @param file - the file to get the extension from
	 * @return the extension as a string
	 */
	public static final String getExtension(File file) {
		if (file == null || TextUtils.isEmpty(file.getName()) || file.isDirectory()) {
			return null;
		}
		String name = file.getName();
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return null;
		}
		else {
			return name.substring(index + 1);
		}
	}
		
}