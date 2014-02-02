package edu.uams.clara.webapp.common.util;

import java.util.HashMap;
import java.util.Map;

import com.ibm.icu.text.Normalizer;

public class StringHelper {
	
	/**
	 * Combination of replacements for upper- and lowercase mode.
	 */
	private static class Replacement {
 
		private final String upper;
		private final String lower;
 
		Replacement(String ucReplacement, String lcReplacement) {
			this.upper = ucReplacement;
			this.lower = lcReplacement;
		}
 
		Replacement(String caseInsensitiveReplacement) {
			this(caseInsensitiveReplacement, caseInsensitiveReplacement);
		}
 
	}
	
	/**
	 * Map containing replacements for corner cases (i.e. not decomposed by the
	 * Normalizer)
	 */
	private Map<Integer, Replacement> charMap = buildReplacementMap();
	/**
	 * builds a map containing all replacements that are not automatically
	 * performed by the normalizer.
	 *
	 * @return Replacement containing both replacements for upper- and lowercase
	 *         mode.
	 */
	private Map<Integer, Replacement> buildReplacementMap() {
		Map<Integer, Replacement> map = new HashMap<Integer, Replacement>();
		map.put(0xc6, new Replacement("AE", "Ae"));
		map.put(0xe6, new Replacement("ae"));
		map.put(0xd0, new Replacement("D"));
		map.put(0x111, new Replacement("d"));
		map.put(0xd8, new Replacement("O"));
		map.put(0xf8, new Replacement("o"));
		map.put(0x152, new Replacement("OE", "Oe"));
		map.put(0x153, new Replacement("oe"));
		map.put(0x166, new Replacement("T"));
		map.put(0x167, new Replacement("t"));
		return map;
	}
	/**
	 * <p>
	 * This method takes an input String and replaces all special characters
	 * like umlauts, accented or other letter with diacritical marks with their
	 * basic ascii eqivalents.
	 * </p>
	 * <p>
	 * Example: The String "André" or "Ándre" would be converted to "Andre".
	 * </p>
	 * <p>
	 * The flag <code>replaceAllCapitalLetters</code> controls the replacement
	 * behavior of special characters that are decomposed into two plain ASCII
	 * chars, like "Æ" or "æ".
	 * </p>
	 * <p>
	 * In "lowercase" mode (i.e.<code> replaceAllCapitalLetters=false</code> )
	 * both aforementioned examples would be converted to "Ae".
	 * </p>
	 * <p>
	 * In "uppercase" mode (<code>replaceAllCapitalLetters=false</code>) the
	 * replacement would be "AE".
	 * </p>
	 *
	 * @param input                    String to convert
	 * @param replaceAllCapitalLetters <code>true</code> causes uppercase special chars that are
	 *                                 replaced by more than one character to be replaced by
	 *                                 all-uppercase replacements; <code>false</code> will cause only
	 *                                 the initial character of the replacements to be in uppercase
	 *                                 and all subsequent replacement characters will be in
	 *                                 lowercase.
	 * @return Input string reduced to ASCII-safe characters.
	 */
	public String convertToAscii(String input, boolean replaceAllCapitalLetters) {
		/*
		 * operating on char arrays because java.lang.String seems to perform an
		 * automatic recomposition of decomposed characters.
		 */
		String result = null;
		if (null != input) {
			char[] src = input.toCharArray();
			/* save space for exotic UTF characters */
			char[] target = new char[src.length * 3];
			int len = Normalizer.normalize(input.toCharArray(), target, Normalizer.NFKD, 0);
			result = processSpecialChars(target, 0, len, replaceAllCapitalLetters);
		}
		return result;
	}
 
	private String processSpecialChars(char[] target, int offset, int len, boolean uppercase) {
		StringBuilder result = new StringBuilder();
		boolean skip = false;
 
		for (int i = 0; i < len; i++) {
			if (skip) {
				skip = false;
			} else {
				char c = target[i];
				if ((c > 0x20 && c < 0x40) || (c > 0x7a && c < 0xc0) || (c > 0x5a && c < 0x61) || (c > 0x79 && c < 0xc0) || c == 0xd7 || c == 0xf7) {
					result.append(c);
				} else if (Character.isDigit(c) || Character.isISOControl(c)) {
					result.append(c);
				} else if (Character.isWhitespace(c) || Character.isLetter(c)) {
					boolean isUpper = false;
 
					switch (c) {
						case '\u00df':
							result.append("ss");
							break;
						/* Handling of capital and lowercase umlauts */
						case '\u00B0':
							result.append("Degree");
							break;
						case 'A':
						case 'O':
						case 'U':
							isUpper = true;
						case 'a':
						case 'o':
						case 'u':
							result.append(c);
							if (i + 1 < target.length && target[i + 1] == 0x308) {
								result.append(isUpper && uppercase ? 'E' : 'e');
								skip = true;
							}
							break;
						default:
							Replacement rep = charMap.get(Integer.valueOf(c));
							if (rep != null) {
								result.append(uppercase ? rep.upper : rep.lower);
							} else
								result.append(c);
					}
				}
			}
		}
 
		return result.toString();
	}
	
}
