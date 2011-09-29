/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.util;

/**
 * @author Chiranjit Acharya
 *
 **/ 
import java.security.MessageDigest;


public class TextStringUtil 
{
	private TextStringUtil() {
	}

	public static String toString(Exception e) {   
	  
	    StringWriter s = new StringWriter();   
	    e.printStackTrace(new PrintWriter(s));   
	    return s.toString();    
	}  
	
	/**  
	 * This String utility or util method can be used to merge 2 arrays of  
	 * string values. 
	 * If the input arrays are like this array1 = {"a", "b" ,  
	 * "c"} array2 = {"c", "d", "e"} Then the output array will have {"a", "b" ,  
	 * "c", "d", "e"}  
	 *   
	 * This takes care of eliminating duplicates and checks null values.  
	 *   
	 * @param values  
	 * @return  
	 */  
	public static String[] mergeStringArrays(String array1[], String array2[])   
	{ 
		if (array1 == null || array1.length == 0)   
			return array2;   
		if (array2 == null || array2.length == 0)   
			return array1;   
		List array1List = Arrays.asList(array1);   
		List array2List = Arrays.asList(array2);   
		List result = new ArrayList(array1List);     
		List tmp = new ArrayList(array1List);   
		tmp.retainAll(array2List);   
		result.removeAll(tmp);   
		result.addAll(array2List);     
		return ((String[]) result.toArray(new String[result.size()]));   
	}  
	
	/**  
	 * This String utility or util method can be used to trim all the String  
	 * values in list of Strings. For input [" a1 ", "b1 ", " c1"] the output  
	 * will be {"a1", "b1", "c1"} Method takes care of null values. This method  
	 * is collections equivalent of the trim method for String array.  
	 *  
	 * @param values  
	 * @return  
	 */  
	public static List trim(final List values)  
	{ 
		List newValues = new ArrayList();   
		for (int i = 0, length = values.size(); i < length; i++) {   
			String v = (String) values.get(i);   
			if (v != null) {   
				v = v.trim();   
			}   
			newValues.add(v);   
		}   
		return newValues;   
	}  
	
	/**  
	 * This String utility or util method can be used to  
	 * trim all the String values in the string array.  
	 * For input {" a1 ", "b1 ", " c1"}  
	 * the output will be {"a1", "b1", "c1"}  
	 * Method takes care of null values.  
	 * @param values  
	 * @return  
	 */  
	public static String[] trim(final String[] values) 
	{ 
		for (int i = 0, length = values.length; i < length; i++) {   
			if (values[i] != null) {   
				values[i] = values[i].trim();                                   
			}   
		}   
		return values;   
	}  
	
	/**  
	* This method is used to split the given string into different tokens at  
	* the occurrence of specified delimiter  
	*  
	* @param str The string that needs to be broken  
	* @param delimeter The delimiter used to break the string  
	* @return a string array  
	*/  
	  
	public static String[] getTokensArray(String str, String delimeter)  
	{ 
		String[] data;   
		if(str == null){   
			return null;   
		}   
	  
		if (delimeter == null || "".equals(delimeter)   || "".equals(str)) {   
			data = new String[1];   
			data[0] = str;   
			return data;   
		} else {   
			StringTokenizer st = new StringTokenizer(str, delimeter);   
			int tokenCount = st.countTokens();   
			data = new String[tokenCount];   
			for (int i = 0; st.hasMoreTokens(); i++) {   
				data[i] = st.nextToken();   
			}   
		return data;   
		}   
	}  
	
	/**  
	* This method creates a set of unique string tokens which are separated by  
	* separator  
	*  
	* @param str  
	* @param separator  
	* @return  
	*/  
	public static Set getUniqueTokens(String str, String separator)  
	{ 
	 StringTokenizer tokenizer = new StringTokenizer(str, separator);   
	 Set tokens = new HashSet();   
	 while (tokenizer.hasMoreTokens()) {   
	     tokens.add(tokenizer.nextToken());   
	 }   
	 return tokens;   
	}  
	
	
	/**  
	* Return a not null string.  
	*  
	* @param s String  
	* @return empty string if it is null otherwise the string passed in as  
	* parameter.  
	*/  
	  
	public static String nonNull(String s)   
	{ 
		if (s == null) {   
			return "";   
		}   
		return s;   
	}  

	public static String hex(byte[] array) {
		StringBuffer sb = new StringBuffer();

		for(int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		}

		return sb.toString();
	}

	public static String md5sum(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			return hex(md.digest(message.getBytes("CP1252")));
		} catch(Exception e) {
		}

		return null;
	}

	public static String stripNonAlphaNumericAndSpace(String input) {
		String validCharacterString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";

		return stripNonValidCharacters(validCharacterString,input);
	}

	public static String stripNonValidCharacters(String validCharacterString,String input) {
		char[] validCharacters = validCharacterString.toCharArray();
		StringBuffer newString = new StringBuffer();

		for(int i = 0; i < input.length(); i++)
			for(int j = 0; j < validCharacters.length; j++)
				if(input.charAt(i) == validCharacters[j]) {
					newString.append(validCharacters[j]);
				}

		return newString.toString();
	}

	public static String trimHeadAndTail(String toTrim) {
		String whitespace = "\r\n \t";
		String newString = "";
		int startIndex;

		for(startIndex = 0; startIndex < toTrim.length(); startIndex++) {
			if(whitespace.indexOf(toTrim.charAt(startIndex)) == -1) {
				break;
			}
		}

		int endIndex;

		for(endIndex = toTrim.length() - 1; endIndex >= 0; endIndex--) {
			if(whitespace.indexOf(toTrim.charAt(endIndex)) == -1) {
				break;
			}
		}

		if((endIndex - startIndex) <= 0) {
			return "";
		}

		newString = toTrim.substring(startIndex,endIndex + 1);

		return newString;
	}

	public static String trimHeadAndTailToOne(String toTrim) {
		String whitespace = "\r\n \t";
		String newString = "";
		int startIndex;

		for(startIndex = 0; startIndex < toTrim.length(); startIndex++) {
			if(whitespace.indexOf(toTrim.charAt(startIndex)) == -1) {
				break;
			}
		}

		int endIndex;

		for(endIndex = toTrim.length() - 1; endIndex >= 0; endIndex--) {
			if(whitespace.indexOf(toTrim.charAt(endIndex)) == -1) {
				break;
			}
		}

		if((endIndex - startIndex) <= 0) {
			return "";
		}

		if(startIndex >= 1) {
			startIndex--;
		}

		if(endIndex <= (toTrim.length() - 1)) {
			endIndex++;
		}

		newString = toTrim.substring(startIndex,endIndex + 1);

		return newString;
	}

	public static String urlEncode(String input) {
		char[] specialCharacters = {' ','&'};
		String[] replacements = {"%20","%26"};
		StringBuffer newString = new StringBuffer();

		for(int i = 0; i < input.length(); i++) {
			boolean replaced = false;

			for(int j = 0; j < specialCharacters.length; j++)
				if(input.charAt(i) == specialCharacters[j]) {
					newString.append(replacements[j]);
					replaced = true;

					break;
				}

			if(!replaced) {
				newString.append(input.charAt(i));
			}
		}

		return newString.toString();
	}
}

