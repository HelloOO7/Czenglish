package czenglish;

public class LangUtilities {
	public static String removeNonspokenChars(String str){
		return str.replace(".", " ").replace("(", "").replace("?", " ").replace("@", "").replace("!", " ").replace(")", "").replace(",", " ").replace(";", "").replace(":", " ").replace("-", " ").replace("\"", "").replace("”", "").replace("/", " ").replace("\\n", " ").replaceAll("\\s+", " ");
	}
	
	public static String removeLinks(String str){
		return str.replaceAll("(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)", "");
	}
}
