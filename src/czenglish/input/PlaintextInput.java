package czenglish.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaintextInput implements IInput {

	private MessageGroup grp = new MessageGroup();
	
	public PlaintextInput(File f){
		try {
			grp.name = f.getName();
			Scanner s = new Scanner(f);

			while (s.hasNextLine()){
				grp.messages.add(new Message(null, s.nextLine()));
			}
			
			s.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(PlaintextInput.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(PlaintextInput.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@Override
	public List<MessageGroup> getMessageGroups() {
		List l = new ArrayList();
		l.add(grp);
		return l;
	}

}
