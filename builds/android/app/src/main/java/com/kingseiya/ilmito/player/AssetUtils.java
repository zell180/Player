/*
 * This file is part of EasyRPG Player
 *
 * Copyright (c) 2017 EasyRPG Project. All rights reserved.
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package com.kingseiya.ilmito.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.BufferedInputStream;
import java.util.Arrays;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.kingseiya.ilmito.R;

// based on https://stackoverflow.com/q/15574983/

public class AssetUtils {
	public static void copyFolder(AssetManager assetManager, String source, String target) {
		// "Name" is the name of your folder!
		String[] files = null;

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			// Checking file on assets subfolder
			try {
				files = assetManager.list(source);
			} catch (IOException e) {
				Log.e("ERROR", "Failed to get asset file list.", e);
			}
			// Analyzing all file on assets subfolder
			for (String filename : files) {
				InputStream in = null;
				OutputStream out = null;
				// First: checking if there is already a target folder
				File folder = new File(target);
				boolean success = true;
				if (!folder.exists()) {
					success = folder.mkdir();
				}
				if (success) {
					// Moving all the files on external SD
					String sourceFile = source + "/" + filename;
					String targetFile = folder.getAbsolutePath() + "/" + filename;
					try {
						in = assetManager.open(sourceFile);
						out = new FileOutputStream(targetFile, false);
						/*Log.i("WEBVIEW",
								Environment.getExternalStorageDirectory()
										+ "/yourTargetFolder/" + name + "/"
										+ filename);*/
						copyFile(in, out);
						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;
					} catch (IOException e) {
						try {
							assetManager.list(sourceFile);
						} catch (IOException f) {
							Log.e("ERROR",
									"Failed to copy asset file: " + filename, f);
							continue;
						}
						
						copyFolder(assetManager, sourceFile, targetFile);
					}
				} else {
					// Do something else on failure
				}
			}
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// is to know is we can neither read nor write
		}
	}

	public static void unzipFile(AssetManager assetManager, String source, String target) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media

			String filename;
			InputStream is = null;
			ZipInputStream zis = null;
			ZipEntry ze = null;

			try {
				is = assetManager.open(source);
				zis = new ZipInputStream(new BufferedInputStream(is));

				while((ze = zis.getNextEntry()) != null) {
					filename = ze.getName();

					if(ze.isDirectory()) {
						File fmd = new File(target + "/" + filename);
						fmd.mkdirs();
						continue;
					}

					FileOutputStream fout = new FileOutputStream(target + "/" + filename, false);
					copyFile(zis, fout);
					fout.flush();
					fout.close();

					fout.close();
					zis.closeEntry();
				}
				zis.close();
				is.close();

			} catch (IOException e) {
				Log.e("ERROR", "Failed to get asset zipfile.", e);
			}

		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// is to know is we can neither read nor write
		}
	}

	//Method used to copy file from APK Expansion Files
	public static void copyFolderFromExpansion(Context appContext, String target,
											   Handler progressHandler,
											   int mainVersion,
											   int patchVersion,
											   boolean update) throws IOException {
		// "Name" is the name of your folder!
		String[] files = null;
		String state = Environment.getExternalStorageState();
		ZipResourceFile expansionFile = null;
		String text = appContext.getResources().getString(R.string.install_game_file, 2, 100);

		if (!update) {
			expansionFile = new ZipResourceFile(Environment.getExternalStorageDirectory().getAbsolutePath() +
															"/Android/obb/" +
															appContext.getPackageName() +
															"/main." +
															mainVersion +
															"." + appContext.getPackageName() +
															".obb");
		} else {
			expansionFile = new ZipResourceFile(Environment.getExternalStorageDirectory().getAbsolutePath() +
															"/Android/obb/" +
															appContext.getPackageName() +
															"/patch." +
															patchVersion +
															"." + appContext.getPackageName() +
															".obb");
		}

		ZipResourceFile.ZipEntryRO[] zipFiles = expansionFile.getAllEntries();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			((Activity) appContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("PROVA");
				}
			});
			int fileIdx = 0;
			int zipSize = zipFiles.length;

			// Analyzing all file on assets subfolder
			for (ZipResourceFile.ZipEntryRO file: zipFiles) {
				fileIdx += 1;
				sendProgressMsg(progressHandler, fileIdx, zipSize, update);
				String pathInsideZip = file.mFileName;
				String fileName = file.mFileName.substring(pathInsideZip.lastIndexOf("/"));
				if (!fileName.equals("/")) {
					String filePath = pathInsideZip.replace(fileName,"");
					InputStream in = null;
					OutputStream out = null;
					// First: checking if there is already a target folder
					File folder = new File(target + filePath);
					boolean success = true;
					if (!folder.exists()) {
						success = folder.mkdirs();
					}
					if (success) {
						// Moving all the files on external SD
						//String sourceFile = source + "/" + filename;
						String targetFile = folder.getAbsolutePath() + fileName;
						try {
							in = expansionFile.getInputStream(pathInsideZip);
							out = new FileOutputStream(targetFile, false);
							copyFile(in, out);
							in.close();
							in = null;
							out.flush();
							out.close();
							out = null;
						} catch (IOException e) {
							Log.e("ERROR","Failed to copy asset file: " + targetFile, e);
						}
					} else {
						// Do something else on failure
					}
				}
			}

		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// is to know is we can neither read nor write
		}
	}

	private static void sendProgressMsg(Handler progressHandler, int fileIdx, int zipSize, boolean update) {
		Message msg = progressHandler.obtainMessage();
		msg.arg1 = fileIdx;
		msg.arg2 = zipSize;
		msg.obj = update;
		progressHandler.sendMessage(msg);
	}

	//Not necessary if modify save_path value in GameInformation
	public static void copySaveFromExternal(Context appContext, String target) throws IOException {
		String source = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
						appContext.getPackageName().substring(appContext.getPackageName().lastIndexOf("."))
						.replace(".","");
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			// Analyzing all file on assets subfolder
			File saveFolder = new File(source);
			if (saveFolder.exists()) {
				InputStream in = null;
				OutputStream out = null;
				String [] files = saveFolder.list();
				if (files != null) {
					for (String filename: files) {
						if (filename.startsWith("Save")) {
							String sourceFile = source + "/" + filename;
							String targetFile = new File(target).getAbsolutePath() + "/" + filename;
							try {
								in = new FileInputStream(sourceFile);
								out = new FileOutputStream(targetFile, false);
								copyFile(in, out);
								in.close();
								in = null;
								out.flush();
								out.close();
								out = null;
							} catch (IOException e) {
								Log.e("ERROR","Failed to copy save file: " + targetFile, e);
							}
						}
					}
				}
			} else {
					// Do something else on failure
			}
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// is to know is we can neither read nor write
		}
	}

	// Method used by copyAssets() on purpose to copy a file.
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
	
	public static boolean exists(AssetManager assetManager, String source) {
		try {
			String[] s = assetManager.list(source);
			if (s == null || s.length == 0) {
				return false;
			}
			
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean fileExists(AssetManager assetManager, String filename) {
		try {
			return Arrays.asList(assetManager.list("")).contains(filename);
		} catch (IOException e) {
			return false;
		}
	}

	public static void askForStoragePermission(Activity context) {
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(context,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
			//int check = -1;
			//while (check != 0) {
			//	check = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			//}
		}
	}

	public static void removeExpansion(Context appContext, int mainVersion, int patchVersion,
									   											boolean update) {
		// Rimuove i file .obb se l'installazione va a buon fine
		File fileToDelete = null;
		if (!update) {
			fileToDelete = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
					"/Android/obb/" +
					appContext.getPackageName() +
					"/main." +
					mainVersion +
					"." + appContext.getPackageName() +
					".obb");
		} else {
			fileToDelete = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
					"/Android/obb/" +
					appContext.getPackageName() +
					"/patch." +
					patchVersion +
					"." + appContext.getPackageName() +
					".obb");
		}
		fileToDelete.delete();
	}
}
