/*
 * This file is part of dcat-ap-se-processor.
 *
 * dcat-ap-se-processor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dcat-ap-se-processor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with dcat-ap-se-processor.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.ams.dcatprocessor.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.Nullable;
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
		try {
			new URL(url).toURI();
			return true;
		} catch (URISyntaxException | MalformedURLException e) {
			return false;
		}
	}
	
}
