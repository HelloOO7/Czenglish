package czenglish;

import czenglish.input.Message;
import czenglish.input.MessageGroup;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageSorter {
	public static List<MessageGroup> groupBySender(List<MessageGroup> groups){
		List<Message> messages = new ArrayList<>();
		for (MessageGroup g : groups){
			messages.addAll(g.messages);
		}
		
		Map<String, MessageGroup> groupsByAuthor = new HashMap<>();

		for (Message msg : messages){
			String author = msg.author;
			
			MessageGroup g = groupsByAuthor.get(author);
			if (g == null){
				g = new MessageGroup();
				g.name = author;
				groupsByAuthor.put(author, g);
			}
			
			g.messages.add(msg);
		}
		
		for (String key : new ArrayList<>(groupsByAuthor.keySet())){
			if (key != null && key.contains("Sára")){
				groupsByAuthor.remove(key);
			}
		}
		
		return new ArrayList<>(groupsByAuthor.values());
	}
	
	public static List<MessageGroup> groupByMonth(List<MessageGroup> groups){
		List<Message> messages = new ArrayList<>();
		for (MessageGroup g : groups){
			messages.addAll(g.messages);
		}
		
		Map<YearAndMonth, MessageGroup> groupsByYnM = new HashMap<>();
		
		for (Message msg : messages){
			YearAndMonth ym = getMsgYnM(msg);
			
			MessageGroup g = groupsByYnM.get(ym);
			if (g == null){
				g = new MessageGroup();
				groupsByYnM.put(ym, g);
			}
			
			g.messages.add(msg);
		}
		
		List<Map.Entry<YearAndMonth, MessageGroup>> entries = new ArrayList<>(groupsByYnM.entrySet());
		entries.sort(new Comparator<Map.Entry<YearAndMonth, MessageGroup>>() {
			@Override
			public int compare(Map.Entry<YearAndMonth, MessageGroup> o1, Map.Entry<YearAndMonth, MessageGroup> o2) {
				return (o1.getKey().year * 12 + o1.getKey().month) - (o2.getKey().year * 12 + o2.getKey().month);
			}
		});
		
		List<MessageGroup> result = new ArrayList<>();
		
		for (Map.Entry<YearAndMonth, MessageGroup> e : entries){
			e.getValue().name = (e.getKey().month + 1) + "/" + e.getKey().year;
			result.add(e.getValue());
		}
		
		return result;
	}
	
	public static List<MessageGroup> groupByWeek(List<MessageGroup> groups){
		List<Message> messages = new ArrayList<>();
		for (MessageGroup g : groups){
			messages.addAll(g.messages);
		}
		
		Map<YearAndWeek, MessageGroup> groupsByYnW = new HashMap<>();
		
		for (Message msg : messages){
			YearAndWeek ym = getMsgYnW(msg);
			
			MessageGroup g = groupsByYnW.get(ym);
			if (g == null){
				g = new MessageGroup();
				groupsByYnW.put(ym, g);
			}
			
			g.messages.add(msg);
		}
		
		List<Map.Entry<YearAndWeek, MessageGroup>> entries = new ArrayList<>(groupsByYnW.entrySet());
		entries.sort(new Comparator<Map.Entry<YearAndWeek, MessageGroup>>() {
			@Override
			public int compare(Map.Entry<YearAndWeek, MessageGroup> o1, Map.Entry<YearAndWeek, MessageGroup> o2) {
				return (o1.getKey().year * 53 + o1.getKey().week) - (o2.getKey().year * 53 + o2.getKey().week);
			}
		});
		
		List<MessageGroup> result = new ArrayList<>();
		
		for (Map.Entry<YearAndWeek, MessageGroup> e : entries){
			e.getValue().name = (e.getKey().week + 1) + "/" + e.getKey().year;
			result.add(e.getValue());
		}
		
		return result;
	}
	
	private static YearAndMonth getMsgYnM(Message msg){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(msg.timestamp);
		YearAndMonth ym = new YearAndMonth();
		ym.year = cal.get(Calendar.YEAR);
		ym.month = cal.get(Calendar.MONTH);
		return ym;
	}
	
	private static YearAndWeek getMsgYnW(Message msg){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(msg.timestamp);
		YearAndWeek ym = new YearAndWeek();
		ym.year = cal.get(Calendar.YEAR);
		ym.week = cal.get(Calendar.WEEK_OF_YEAR);
		return ym;
	}
	
	private static class YearAndWeek {
		public int year;
		public int week;

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 29 * hash + this.year;
			hash = 29 * hash + this.week;
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
			final YearAndWeek other = (YearAndWeek) obj;
			if (this.year != other.year) {
				return false;
			}
			if (this.week != other.week) {
				return false;
			}
			return true;
		}
		
	}
	
	private static class YearAndMonth {
		public int year;
		public int month;

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 29 * hash + this.year;
			hash = 29 * hash + this.month;
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
			final YearAndWeek other = (YearAndWeek) obj;
			if (this.year != other.year) {
				return false;
			}
			if (this.month != other.week) {
				return false;
			}
			return true;
		}
		
	}
}
