// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import se.ams.dcatprocessor.rdf.DcatException;

/**
 * Utility class for methods used by multiple classes
 * 
 * @author nacbr
 *
 */
public class Util {
	
	/**
	 * Type-independant variable null/empty check
	 * @param t - To be checked
	 * @param errorMessage - Included in exception
	 * @throws DcatException - Thrown if null
	 */
	public static <T> void checkNotNull(T t, String errorMessage) throws DcatException {
		if(isNullOrEmpty(t)) {
			throw new DcatException(errorMessage);
		}
	}
	
	/**
	 * Type-independant List null/empty check
	 * @param tList - To be checked
	 * @param errorMessage - Included in exception
	 * @throws DcatException - Thrown if null or empty
	 */
	public static <T> void checkNotNull(List<T> tList, String errorMessage) throws DcatException {
		if(isNullOrEmpty(tList)) {
			throw new DcatException(errorMessage);
		}
	}
	
	public static <T> boolean isNullOrEmpty(T t) {
		return Optional.ofNullable(t).isEmpty() || t.toString().isEmpty();
	}

	public static <T> boolean isNotNullOrEmpty(T t) {
		return !isNullOrEmpty(t);
	}

	public static <T> boolean isNullOrEmpty(List<T> tList) {
		return Optional.ofNullable(tList).isEmpty() || tList.isEmpty();
	}

	/**
	 * Replaces all keys in msg that equals a key in replace with the
	 * value at the corresponding index in replacement 
	 * @param msg - To be changed
	 * @param replace - Values to replace
	 * @param replacement - Replacements
	 * @return - The String containing the new values
	 */
	public static String createErrorMsg(String msg, String[] replace, String[] replacement) {
		if(replace.length == replacement.length) {
			for (int i = 0; i < replace.length; i++) {
				msg = msg.replaceAll(replace[i], replacement[i]);
			}
		}
		return msg;
	}
	
	private static final String COMMA = ",";
	
	/**
	 * Merge an array of Stings into one String with the supplied separator
	 * @param strings - The array containing the strings to be merged
	 * @param separator - The separator between the strings. If null, a default separator is used
	 * @return - The merged string
	 */
	public static String mergeStringsWithSeparator(String[] strings, @Nullable String separator) {
		
		if(Util.isNullOrEmpty(strings)) {
			return null;
		}
		
		String internalSeparator = separator;
		
		if(Util.isNullOrEmpty(separator)) {
			internalSeparator = COMMA;
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < strings.length; i++) {
			sb.append(strings[i]);
			//Append , only in between. Not after the last String
			if(i < strings.length - 1) {
				sb.append(internalSeparator);	
			}	
		}
		
		return sb.toString();
	}
	
	public static boolean isURI(String url) {
	    if (url == null) return false;
	    try {
	        new URI(url).toURL();
	        return true;
	    } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
	        return false;
	    }
	}
}
