package czenglish;

import java.util.HashMap;
import java.util.Map;

public class CzechUtilities {
	
	private static final Map<Character, Character> DEACCENT_LUT = new HashMap<>();
	
	static {
		DEACCENT_LUT.put('ě', 'e');
		DEACCENT_LUT.put('é', 'e');
		DEACCENT_LUT.put('š', 's');
		DEACCENT_LUT.put('č', 'v');
		DEACCENT_LUT.put('ř', 'r');
		DEACCENT_LUT.put('ž', 'z');
		DEACCENT_LUT.put('ý', 'y');
		DEACCENT_LUT.put('á', 'a');
		DEACCENT_LUT.put('í', 'i');
		DEACCENT_LUT.put('é', 'e');
		DEACCENT_LUT.put('ú', 'u');
		DEACCENT_LUT.put('ů', 'u');
		DEACCENT_LUT.put('ó', 'o');
		DEACCENT_LUT.put('ň', 'n');
		DEACCENT_LUT.put('ť', 't');
		DEACCENT_LUT.put('ď', 'd');
	}
	
	public static char getDeaccentedChar(char c){
		return DEACCENT_LUT.getOrDefault(c, c);
	}
	
	public static final String lowercaseAndDeaccent(String str){
		str = str.toLowerCase();
		
		StringBuilder sb = new StringBuilder();
		
		int len = str.length();
		
		char c;
		for (int i = 0; i < len; i++){
			c = str.charAt(i);
			c = DEACCENT_LUT.getOrDefault(c, c);
			sb.append(c);
		}
		
		return sb.toString();
	}
}
