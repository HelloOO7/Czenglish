package czenglish;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

public class Dictionary {

	private Map<String, List<String>> eng2cze = new HashMap<>();
	private Map<String, List<String>> cze2eng = new HashMap<>();

	private List<List<String>> cze = new ArrayList<>();
	private List<List<String>> eng = new ArrayList<>();

	private Dictionary() {
		for (int i = 'a'; i <= 'z'; i++) {
			cze.add(new ArrayList<>());
			eng.add(new ArrayList<>());
		}
	}

	public static Dictionary readTxt(File f) {
		try {
			Dictionary d = new Dictionary();

			BufferedReader reader = new BufferedReader(new FileReader(f));

			List<WordPair> pairs = new ArrayList<>();

			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					pairs.add(new WordPair(line));
				}
			}

			reader.close();

			int lastChar = 0;

			d.cze2eng = new HashMap<>(pairs.size());
			d.eng2cze = new HashMap<>(pairs.size());

			for (WordPair wp : pairs) {
				if (wp.english != null && !wp.english.isEmpty() && Character.isLetter(wp.english.charAt(0))) {
					char fc = Character.toUpperCase(wp.english.charAt(0));
					if (fc != lastChar) {
						System.out.println("new fc " + fc);
						lastChar = fc;
						System.out.println("avail memory " + Runtime.getRuntime().freeMemory());
					}
					if (wp.czech != null) {
						fillMap(wp.english, wp.czech, d.eng2cze);
					}

					addAlphabetLookup(wp.english, d.eng);

					if (wp.czech != null && !wp.czech.isEmpty() && Character.isLetter(wp.czech.charAt(0))) {
						fillMap(wp.czech, wp.english, d.cze2eng);

						addAlphabetLookup(wp.czech, d.cze);
					}
				}
			}
			return d;
		} catch (IOException ex) {
			Logger.getLogger(Dictionary.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	private static void addAlphabetLookup(String str, List<List<String>> map) {
		char c = CzechUtilities.getDeaccentedChar(str.charAt(0));
		List<String> l = map.get(c - 'a');
		if (!l.contains(str)) {
			l.add(str);
		}
	}

	public List<String> getAlphabetLookupCZE(String str) {
		int index = CzechUtilities.getDeaccentedChar(str.charAt(0));
		if (index < 'a' || index > 'z') {
			return new ArrayList<>();
		}
		return cze.get(index - 'a');
	}

	public List<String> getAlphabetLookupENG(String str) {
		int index = CzechUtilities.getDeaccentedChar(str.charAt(0));
		if (index < 'a' || index > 'z') {
			return new ArrayList<>();
		}
		return eng.get(index - 'a');
	}

	public static Dictionary readBinary(File f) {
		try {
			Dictionary d = new Dictionary();
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));

			d.eng2cze = (Map) ois.readObject();
			d.cze2eng = (Map) ois.readObject();
			d.eng = (List) ois.readObject();
			d.cze = (List) ois.readObject();

			ois.close();

			/*DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			in.skip(4);

			int c2eSize = in.readInt();
			for (int i = 0; i < c2eSize; i++) {
				String key = in.readUTF();
				int wordCount = in.readUnsignedShort();
				List<String> words = new ArrayList<>();
				for (int j = 0; j < wordCount; j++) {
					words.add(in.readUTF());
				}
				d.cze2eng.put(key, words);
				d.cze.add(key);
			}

			int e2cSize = in.readInt();
			for (int i = 0; i < e2cSize; i++) {
				String key = in.readUTF();
				int wordCount = in.readUnsignedShort();
				List<String> words = new ArrayList<>();
				for (int j = 0; j < wordCount; j++) {
					words.add(in.readUTF());
				}
				d.eng2cze.put(key, words);
				d.eng.add(key);
			}

			in.close();*/
			return d;
		} catch (IOException | ClassNotFoundException ex) {
			Logger.getLogger(Dictionary.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public void writeBinary(File f) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			oos.writeObject(eng2cze);
			oos.writeObject(cze2eng);
			oos.writeObject(eng);
			oos.writeObject(cze);
			oos.close();

			/*DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			out.writeInt(0x6F646374);
			out.writeInt(eng2cze.size());

			for (Map.Entry<String, List<String>> e : eng2cze.entrySet()) {
				out.writeUTF(e.getKey());
				out.writeShort(e.getValue().size());
				for (String s : e.getValue()) {
					out.writeUTF(s);
				}
			}

			out.writeInt(cze2eng.size());

			for (Map.Entry<String, List<String>> e : cze2eng.entrySet()) {
				out.writeUTF(e.getKey());
				out.writeShort(e.getValue().size());
				for (String s : e.getValue()) {
					out.writeUTF(s);
				}
			}

			out.close();*/
		} catch (IOException ex) {
			Logger.getLogger(Dictionary.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public String findBestMatchCZE(String str, boolean dediacriticise) {
		str = CzechStemmer.stem(str.toLowerCase());
		return findBestMatchLevenshtein(getAlphabetLookupCZE(str), str, dediacriticise);
	}

	public String findBestMatchENG(String str, boolean dediacriticise) {
		str = EnglishStemmer.stem(str.toLowerCase());
		return findBestMatchLevenshtein(getAlphabetLookupENG(str), str, dediacriticise);
	}

	public boolean isEnglish(String word, boolean angl_IgnoreFullAnglMatch, boolean ignoreDiacritics) {
		String eng = findBestMatchENG(word, false);
		String cze = findBestMatchCZE(word, ignoreDiacritics);
		if (eng != null && cze != null) {

			if (!angl_IgnoreFullAnglMatch || !EnglishStemmer.stem(word).equalsIgnoreCase(eng)) {

				if (StringUtils.getLevenshteinDistance(CzechStemmer.stem(word), cze) > StringUtils.getLevenshteinDistance(EnglishStemmer.stem(word), eng)) {
					return true;
				}
			}
		}
		return false;
	}

	private static String findBestMatchLevenshtein(List<String> strings, String str, boolean dediacriticise) {
		int bestMatch = Integer.MAX_VALUE;
		String bestMatchStr = null;
		int dist;

		for (String c : strings) {
			if (dediacriticise) {
				c = CzechUtilities.lowercaseAndDeaccent(c);
			}
			dist = StringUtils.getLevenshteinDistance(c, str);
			if (dist < bestMatch) {
				bestMatch = dist;
				bestMatchStr = c;
			}
		}
		return bestMatchStr;
	}

	private static void fillMap(String key, String value, Map<String, List<String>> map) {
		List<String> l = map.get(key);
		if (l == null) {
			l = new ArrayList<>();
			map.put(key, l);
		}
		l.add(value);
	}
}
