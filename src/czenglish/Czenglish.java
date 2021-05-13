/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package czenglish;

import czenglish.input.DiscordCSV;
import czenglish.input.IInput;
import czenglish.input.Message;
import czenglish.input.MessengerJson;
import czenglish.input.ViberDB;
import czenglish.input.ViberIOSData;
import czenglish.input.MessageGroup;
import czenglish.input.MessengerDB;
import czenglish.input.PlaintextInput;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

public class Czenglish {

	private static final List<String> BLACKLIST = new ArrayList<>();

	static {
		loadBlackList("czech_stopwords.txt");
	}

	public static boolean isStopWord(String word) {
		int len = word.length();
		for (int i = 0; i < len; i++) {
			if (Character.isDigit(word.charAt(0))) {
				return true;
			}
		}
		return BLACKLIST.contains(CzechUtilities.lowercaseAndDeaccent(word));
	}
	
	public static void loadBlackList(String path){
		try {
			BufferedReader sw = new BufferedReader(new FileReader("dict/" + path));

			String line;
			while ((line = sw.readLine()) != null) {
				String data = line.toLowerCase();
				if (!BLACKLIST.contains(data)) {
					BLACKLIST.add(CzechUtilities.lowercaseAndDeaccent(data));
				}
			}

			sw.close();
		} catch (IOException ex) {
			Logger.getLogger(Czenglish.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void main(String[] args) {
		File dictFile = new File("dict/dict.odct");

		Dictionary dict;

		if (!dictFile.exists()) {
			dict = Dictionary.readTxt(new File("dict/en-cs.txt"));
			dict.writeBinary(dictFile);
			return;
		} else {
			System.out.println("reading binary dictionary");
			dict = Dictionary.readBinary(dictFile);
			System.out.println("read binary dictionary");
		}

		File inDir = new File("input");

		List<IInput> inputs = new ArrayList<>();

		boolean messengerEnabled = false;
		boolean viberEnabled = true;
		boolean viberiOSEnabled = false;
		boolean plaintextEnabled = false;
		boolean discordEnabled = false;
		boolean amalieMessengerEnabled = true;

		for (File f : inDir.listFiles()) {
			if (messengerEnabled && f.getName().endsWith(".json")) {
				inputs.add(new MessengerJson(f));
			} else if (viberEnabled && f.getName().equals("viber.db")) {
				inputs.add(new ViberDB(f));
			} else if (viberiOSEnabled && f.getName().equals("Contacts.data")) {
				inputs.add(new ViberIOSData(f));
			} else if (plaintextEnabled && f.getName().endsWith(".txt")) {
				inputs.add(new PlaintextInput(f));
			} else if (discordEnabled && f.getName().endsWith(".csv")) {
				inputs.add(new DiscordCSV(f));
			} else if (amalieMessengerEnabled && f.getName().startsWith("threads_db")){
				inputs.add(new MessengerDB(f));
			}
		}

		System.out.println("begin processing inputs");

		String mode = "list";
		String extra = "top10";
		String groupMode = "null";
		String listFile = "zkratky.txt";
		String listFile2 = "exclaimy_ale_cesky.txt";
		String additionalBlackList = "9d_bl.txt";
		if (additionalBlackList != null){
			loadBlackList(additionalBlackList);
		}

		boolean angl_IgnoreFullAnglMatch = false;
		boolean ignoreDiacritics = false;
		boolean includeStopInTotal = true;

		float anglTotal = 0f;

		int groupCount = 0;

		Map<OccurenceGroup, OccurenceGroup> totalAnglOccurences = new HashMap<>();

		for (IInput input : inputs) {

			List<MessageGroup> groups = input.getMessageGroups();

			if (groupMode.equals("month")) {
				groups = MessageSorter.groupByMonth(groups);
			} else if (groupMode.equals("week")) {
				groups = MessageSorter.groupByWeek(groups);
			} else if (groupMode.equals("sender")) {
				groups = MessageSorter.groupBySender(groups);
			}

			groupCount += groups.size();

			if (mode.equals("listCompare")) {
				WordList list1 = new WordList(new File("dict/" + listFile));
				WordList list2 = new WordList(new File("dict/" + listFile2));

				int wordTotalTotal = 0;
				int listWordTotalTotal1 = 0;
				int listWordTotalTotal2 = 0;

				Map<OccurenceGroup, OccurenceGroup> occurences1 = new HashMap<>();
				Map<OccurenceGroup, OccurenceGroup> occurences2 = new HashMap<>();

				for (MessageGroup msgGrp : groups) {
					//System.out.println("GROUP " + msgGrp.name);

					int totalWords = 0;
					int totalListWord1 = 0;
					int totalListWord2 = 0;

					for (Message msg : msgGrp.messages) {
						for (String word : msg.content.split(" ")) {
							word = word.toLowerCase();
							if (list1.hasWordStem(word, true)) {
								OccurenceGroup grp = new OccurenceGroup(word, true);
								if (!occurences1.containsKey(grp)) {
									occurences1.put(grp, grp);
								} else {
									occurences1.get(grp).numOccurences++;
									occurences1.get(grp).addText(word);
								}
								totalListWord1++;
							}
							if (list2.hasWordStem(word, false)) {
								OccurenceGroup grp = new OccurenceGroup(word, false);
								if (!occurences2.containsKey(grp)) {
									occurences2.put(grp, grp);
								} else {
									occurences2.get(grp).numOccurences++;
									occurences2.get(grp).addText(word);
								}
								totalListWord2++;
							}

							totalWords++;
						}
					}

					listWordTotalTotal1 += totalListWord1;
					listWordTotalTotal2 += totalListWord2;
					wordTotalTotal += totalWords;
				}

				/*for (OccurenceGroup g : occurences1.keySet()){
					System.out.println(g.texts + ", " + g.numOccurences);
				}
				for (OccurenceGroup g : occurences2.keySet()){
					System.out.println(g.texts + ", " + g.numOccurences);
				}*/
				for (int i = 0; i < list1.entries.size(); i++){
					OccurenceGroup eng = findOGByWordsENG(list1.entries.get(i).words, occurences1.keySet());
					OccurenceGroup cze = findOGByWordsCZE(list2.entries.get(i).words, occurences2.keySet());
					
					System.out.println((cze != null ? cze.texts + ": " + cze.numOccurences : "NO DATA") + " | " + (eng != null ? eng.texts + ": " + eng.numOccurences : "NO DATA"));
				}
			}
			if (mode.equals("list")) {
				WordList list = new WordList(new File("dict/" + listFile));

				int wordTotalTotal = 0;
				int listWordTotalTotal = 0;

				for (MessageGroup msgGrp : groups) {
					//System.out.println("GROUP " + msgGrp.name);

					Map<OccurenceGroup, OccurenceGroup> occurences = new HashMap<>();

					int totalWords = 0;
					int totalListWord = 0;

					for (Message msg : msgGrp.messages) {
						for (String word : msg.content.split(" ")) {
							word = word.toLowerCase();
							if (list.hasWord(word)) {
								OccurenceGroup grp = new OccurenceGroup(word, word, true);
								if (!occurences.containsKey(grp)) {
									occurences.put(grp, grp);
								} else {
									occurences.get(grp).numOccurences++;
									occurences.get(grp).addText(word);
								}
								totalListWord++;
							}

							totalWords++;
						}
					}

					System.out.println(msgGrp.name + "," + ((float) totalListWord / totalWords * 100));

					if (extra.contains("wordcloud") && occurences.size() > 0) {
						String wcName = getSaneFileName(msgGrp.name);
						WordCloudMaker.makeWordCloud(occurences.values(), OCCURENCES_LIMIT).writeToFile("wordcloud/" + wcName + ".png");
					}
					
					printOccurenceListStm(occurences);

					listWordTotalTotal += totalListWord;
					wordTotalTotal += totalWords;
				}

				System.out.println("finished list " + listFile);

				System.out.println("total ratio " + ((float) listWordTotalTotal / wordTotalTotal));
			} else if (mode.equals("angl")) {

				WordList zkratky = new WordList(new File("dict/zkratky.txt"));

				for (MessageGroup msgGrp : groups) {
					System.out.println("GROUP " + msgGrp.name);
					int anglCount = 0;
					int totalWordCount = 0;
					int fullAnglMsgCount = 0;

					int fullAnglWordCount = 0;
					int fullAnglZkratkyCount = 0;

					Map<OccurenceGroup, OccurenceGroup> occurences = new HashMap<>();

					for (Message msg : msgGrp.messages) {
						if (!msg.content.trim().isEmpty()) {
							int msgTotalWordCount = 0;
							int msgAnglWordCount = 0;

							Map<OccurenceGroup, OccurenceGroup> msgOccurences = new HashMap<>();

							for (String word : msg.content.split(" ")) {
								if (word.length() >= 4) {
									word = word.toLowerCase();
									if (ignoreDiacritics) {
										word = CzechUtilities.lowercaseAndDeaccent(word);
									}
									if (!isStopWord(word)) {
										String eng = dict.findBestMatchENG(word, false);
										String cze = dict.findBestMatchCZE(word, ignoreDiacritics);
										if (eng != null && cze != null) {

											boolean isFullMatch = EnglishStemmer.stem(word).equalsIgnoreCase(eng);

											if (!angl_IgnoreFullAnglMatch || !isFullMatch) {

												if (StringUtils.getLevenshteinDistance(CzechStemmer.stem(word), cze) > StringUtils.getLevenshteinDistance(EnglishStemmer.stem(word), eng)) {
													//System.out.println(word + " je ang");
													msgAnglWordCount++;

													String keyWord = word;

													if (!isFullMatch) {
														keyWord = CzechStemmer.stem(keyWord);
														//vertexy, vertexům ...
													}

													OccurenceGroup grp = new OccurenceGroup(keyWord, word, true);
													if (!msgOccurences.containsKey(grp)) {
														msgOccurences.put(grp, grp);
													} else {
														msgOccurences.get(grp).numOccurences++;
														msgOccurences.get(grp).addText(word);
													}
												}
											}
										}

										msgTotalWordCount++;
									} else if (includeStopInTotal) {
										msgTotalWordCount++;
									}
								}
							}

							if (msgTotalWordCount == 0 || (float) msgAnglWordCount / msgTotalWordCount < 0.7f) {
								//this isn't a fully english message most likely
								anglCount += msgAnglWordCount;
								totalWordCount += msgTotalWordCount;

								for (OccurenceGroup g : msgOccurences.keySet()) {
									if (occurences.containsKey(g)) {
										OccurenceGroup tgt = occurences.get(g);
										for (String text : g.texts) {
											tgt.addText(text);
										}
										tgt.numOccurences += g.numOccurences;
									} else {
										occurences.put(g, g);
									}
								}
							} else {
								fullAnglMsgCount++;

								System.out.println("FULL ANGL ZPRAVA " + msg.content + " (angl words " + msgAnglWordCount + ", total words " + msgTotalWordCount + ")");

								for (String word : msg.content.split(" ")) {
									if (zkratky.hasWord(word)) {
										fullAnglZkratkyCount++;
									}
									fullAnglWordCount++;
								}
							}
						}
					}

					System.out.println("procenta angl " + ((float) anglCount / totalWordCount));
					System.out.println("procenta full angl " + ((float) fullAnglMsgCount / msgGrp.messages.size()));
					System.out.println("z full angl procenta zkratky " + ((float) fullAnglZkratkyCount / fullAnglWordCount));
					anglTotal += ((float) anglCount / totalWordCount);

					if (extra.contains("top10")) {
						if (occurences.size() >= 1) {
							printOccurenceListStm(occurences);
							if (extra.contains("wordcloud")) {
								String wcName = getSaneFileName(msgGrp.name);
								WordCloudMaker.makeWordCloud(occurences.values(), OCCURENCES_LIMIT).writeToFile("wordcloud/" + wcName + ".png");
							}
						}
					}

					for (OccurenceGroup g : occurences.keySet()) {
						if (totalAnglOccurences.containsKey(g)) {
							OccurenceGroup tgt = totalAnglOccurences.get(g);
							for (String text : g.texts) {
								tgt.addText(text);
							}
							tgt.numOccurences += g.numOccurences;
						} else {
							totalAnglOccurences.put(g, g);
						}
					}
				}
			} else if (mode.equals("mostcommon")) {
				for (MessageGroup msgGrp : groups) {
					System.out.println("GROUP " + msgGrp.name);
					Map<OccurenceGroup, OccurenceGroup> occurences = new HashMap<>();
					for (Message msg : msgGrp.messages) {
						for (String word : msg.content.split(" ")) {
							String lc = word.toLowerCase();
							if (word.length() >= 4 && !isStopWord(lc)) {
								OccurenceGroup grp = new OccurenceGroup(word, dict.isEnglish(lc, angl_IgnoreFullAnglMatch, ignoreDiacritics));
								if (!occurences.containsKey(grp)) {
									occurences.put(grp, grp);
								} else {
									occurences.get(grp).numOccurences++;
									occurences.get(grp).addText(word);
								}
							}
						}
					}

					printOccurenceListStm(occurences);

					List<Map.Entry<OccurenceGroup, OccurenceGroup>> entries = new ArrayList<>(occurences.entrySet());
					entries.sort((Map.Entry<OccurenceGroup, OccurenceGroup> o1, Map.Entry<OccurenceGroup, OccurenceGroup> o2) -> o2.getValue().numOccurences - o1.getValue().numOccurences);

					System.out.println("top 10 angl ratio " + getEffectiveTopNAnglRatio(entries, 10));
					System.out.println("top 25 angl ratio " + getEffectiveTopNAnglRatio(entries, 25));
					System.out.println("top 50 angl ratio " + getEffectiveTopNAnglRatio(entries, 50));
					System.out.println("top 100 angl ratio " + getEffectiveTopNAnglRatio(entries, 100));
					System.out.println("top 200 angl ratio " + getEffectiveTopNAnglRatio(entries, 200));
					System.out.println("all effective ratio " + getEffectiveTopNAnglRatio(entries, entries.size()));

					if (extra.contains("wordcloud") && occurences.size() >= 1 && msgGrp.name != null) {
						String wcName = getSaneFileName(msgGrp.name);
						WordCloudMaker.makeWordCloud(occurences.values(), OCCURENCES_LIMIT).writeToFile("wordcloud/" + wcName + ".png");
					}
				}
			}
		}

		if (mode.equals("angl")) {
			System.out.println("total angl ratio: " + (anglTotal / groupCount));

			if (extra.contains("top10")) {
				System.out.println("total occurences");
				printOccurenceListStm(totalAnglOccurences);
				if (extra.contains("wordcloud")) {
					String wcName = "all";
					WordCloudMaker.makeWordCloud(totalAnglOccurences.values(), OCCURENCES_LIMIT).writeToFile("wordcloud/" + wcName + ".png");
				}
			}
		}
	}
	
	private static OccurenceGroup findOGByWordsCZE(List<String> words, Collection<OccurenceGroup> groups){
		for (OccurenceGroup g : groups){
			for (String w : words){
				if (CzechStemmer.stem(w).equals(g.stem)){
					return g;
				}
			}
		}
		return null;
	}
	
	private static OccurenceGroup findOGByWordsENG(List<String> words, Collection<OccurenceGroup> groups){
		for (OccurenceGroup g : groups){
			for (String w : words){
				if (EnglishStemmer.stem(w).equals(g.stem)){
					return g;
				}
			}
		}
		return null;
	}

	private static float getEffectiveTopNAnglRatio(List<Map.Entry<OccurenceGroup, OccurenceGroup>> entries, int num) {
		int angl = 0;
		int total = 0;

		for (int i = 0; i < Math.min(num, entries.size()); i++) {
			int val = entries.get(i).getValue().numOccurences;
			if (entries.get(i).getValue().isEnglish) {
				angl += val;
			}
			total += val;
		}
		return angl / (float) total;
	}

	private static String getSaneFileName(String name) {
		char fileSep = '/'; // ... or do this portably.
		char escape = '%'; // ... or some other legal char.
		String s = name;
		int len = s.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			if (ch < ' ' || ch >= 0x7F || ch == fileSep || ch == '?' || ch == '!' // add other illegal chars
				|| (ch == '.' && i == 0) // we don't want to collide with "." or ".."!
				|| ch == escape) {
				sb.append(escape);
				if (ch < 0x10) {
					sb.append('0');
				}
				sb.append(Integer.toHexString(ch));
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	private static final int OCCURENCES_LIMIT = 200;

	private static void printOccurenceListStm(Map<OccurenceGroup, OccurenceGroup> map) {
		List<Map.Entry<OccurenceGroup, OccurenceGroup>> entries = new ArrayList<>(map.entrySet());
		entries.sort((Map.Entry<OccurenceGroup, OccurenceGroup> o1, Map.Entry<OccurenceGroup, OccurenceGroup> o2) -> o2.getValue().numOccurences - o1.getValue().numOccurences);

		for (int i = 0; i < Math.min(entries.size(), OCCURENCES_LIMIT); i++) {
			System.out.println((i + 1) + " (" + entries.get(i).getValue().numOccurences + "): " + entries.get(i).getValue().texts);
		}
	}

	private static void printOccurenceList(Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
		entries.sort((Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) -> o2.getValue() - o1.getValue());

		for (int i = 0; i < Math.min(entries.size(), OCCURENCES_LIMIT); i++) {
			System.out.println((i + 1) + ": " + entries.get(i).getKey());
		}
	}
}
