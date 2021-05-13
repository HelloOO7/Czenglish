package czenglish.input;

import czenglish.LangUtilities;

public class Message {

	public String author;
	
	public long timestamp;

	public String content;

	public Message(String author, String content) {
		this(author, content, -1);
	}
	
	public Message(String author, String content, long timeStamp) {
		this.author = author;
		content = LangUtilities.removeLinks(content);
		content = LangUtilities.removeNonspokenChars(content);
		this.content = content;
		this.timestamp = timeStamp;
	}
}
