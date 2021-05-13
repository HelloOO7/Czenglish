package czenglish.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordCSV implements IInput {

	private MessageGroup group = new MessageGroup();
	
	public DiscordCSV(File f){
		try {
			group.name = f.getName();
			BufferedReader reader = new BufferedReader(new FileReader(f));
			
			StringBuilder sb = new StringBuilder();
			
			boolean allowContextChange = true;
			
			int currentContext = 0;
			
			while (reader.ready()){
				char c = (char)reader.read();
				
				if (allowContextChange && c == ','){
					currentContext++;
					if (currentContext == 3){
						currentContext = 0;
						group.messages.add(new Message(null, sb.toString()));
						sb = new StringBuilder();
					}
				}
				
				if (c == '"'){
					allowContextChange = !allowContextChange;
				}
				
				if (currentContext == 2){
					sb.append(c);
				}
			}
			
			reader.close();
		} catch (IOException ex) {
			Logger.getLogger(DiscordCSV.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@Override
	public List<MessageGroup> getMessageGroups() {
		List l = new ArrayList();
		l.add(group);
		return l;
	}

}
