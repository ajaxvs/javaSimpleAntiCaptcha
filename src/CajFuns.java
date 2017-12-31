package ru.ajaxvs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class CajFuns {
	//========================================
	private CajFuns() {}
	//========================================
	/**
	 * Prints to console.
	 * @param o any printable object
	 */
	static public void trace(Object o) {
		System.out.println(o);
	}
	//========================================
	/**
	 * Application directory detection.
	 * @param mainClass
	 * @return path string.
	 */
	static public String getAppPath(Class<?> mainClass) {
		String decodedPath = "";

		try {
			String path = mainClass.getProtectionDomain().getCodeSource().getLocation().getPath();
			File jarFile = new File(path);
			path = jarFile.getParentFile().getPath();
			decodedPath = URLDecoder.decode(path, "UTF-8") + "/";
		} catch (UnsupportedEncodingException e) {
			//np, using default path
		}
		return decodedPath;
	}
	//========================================
}