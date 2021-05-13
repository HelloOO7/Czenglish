/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package czenglish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class OccurenceGroup {

	public String stem;
	public boolean isEnglish;
	
	public List<String> texts = new ArrayList<>();
	public Map<String, Integer> textsOccurences = new HashMap<>();
	public int numOccurences = 1;

	public OccurenceGroup(String str, boolean isEnglish) {
		stem = isEnglish ? EnglishStemmer.stem(str) : CzechStemmer.stem(str);
		texts.add(str);
		textsOccurences.put(str, 1);
		this.isEnglish = isEnglish;
	}
	
	public OccurenceGroup(String stem, String str, boolean isEnglish){
		this.stem = stem;
		this.isEnglish = isEnglish;
		texts.add(str);
		textsOccurences.put(str, 1);
	}

	public void addText(String text) {
		for (String exist : texts) {
			if (exist.equalsIgnoreCase(text)) {
				textsOccurences.put(exist, textsOccurences.get(exist) + 1);
				return;
			}
		}
		texts.add(text);
		textsOccurences.put(text, 1);
	}
	
	public String getMostOccurText(){
		List<Map.Entry<String, Integer>> entries = new ArrayList<>(textsOccurences.entrySet());
		entries.sort((Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) -> o2.getValue() - o1.getValue());

		return entries.get(0).getKey();
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 17 * hash + Objects.hashCode(this.stem);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final OccurenceGroup other = (OccurenceGroup) obj;
		if (!Objects.equals(this.stem, other.stem)) {
			return false;
		}
		return true;
	}

}
