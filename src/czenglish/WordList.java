package czenglish;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WordList implements Iterable<WordList.Entry> {

	public List<Entry> entries = new ArrayList<>();

	public WordList(File f) {
		try {
			Scanner s = new Scanner(f);

			int index = 0;

			while (s.hasNextLine()) {
				String line = s.nextLine().trim().toLowerCase();
				if (!line.isEmpty()) {
					Entry e = new Entry(index);
					e.words.addAll(Arrays.asList(line.split("/")));
					entries.add(e);
					index++;
				}
				else {
					entries.add(new Entry(index));
					index++;
				}
			}

			s.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(WordList.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean hasWordStem(String word, boolean eng) {
		if (eng) {
			word = EnglishStemmer.stem(word);
		} else {
			word = CzechStemmer.stem(word);
		}
		word = word.toLowerCase();
		for (Entry e : entries) {
			for (String w : e.words) {
				if (eng) {
					w = EnglishStemmer.stem(w);
				} else {
					w = CzechStemmer.stem(w);
				}
				if (w.equals(word)){
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasWord(String word) {
		word = word.toLowerCase();
		for (Entry e : entries) {
			if (e.words.contains(word)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<Entry> iterator() {
		return entries.iterator();
	}

	public static class Entry {

		public int index;
		public List<String> words = new ArrayList<>();

		public Entry(int idx) {
			index = idx;
		}
	}
}
