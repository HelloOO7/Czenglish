package czenglish;

public class WordPair {	
	public String english;
	public String czech;
	
	public WordPair(String line){
		String[] data = line.split("\\t");
		if (data.length > 1){
			english = EnglishStemmer.stem(data[0].toLowerCase().trim());
			if (data.length > 2){
				czech = CzechStemmer.stem(data[1].toLowerCase().replace(" se", "").trim()); //fix for zvratná slovesa
			}
		}
	}
	
	private static String deaccent(String str){
		return CzechUtilities.lowercaseAndDeaccent(str);
	}
}
