package czenglish.input;

import czenglish.CzechUtilities;
import czenglish.LangUtilities;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessengerJson implements IInput {

	private MessageGroup messages = new MessageGroup();

	public MessengerJson(File f) {
		System.out.println("read " + f);
		messages.name = f.getName();
		
		//I don't wanna read some crappy jsons...
		//Just search for the hardcoded key value pairs
		//It's not like anyone's gonna read this anyway
		try {
			Scanner scanner = new Scanner(f);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.startsWith("\"content\":")) {
					line = line.substring("\"content\":".length(), line.length());

					line = convertUnicode(line);

					messages.messages.add(new Message(null, line));
				}
			}

			scanner.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(MessengerJson.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static List<Character> HEX_LOWERCASE = Arrays.asList(new Character[]{'a', 'b', 'c', 'd', 'e', 'f'});

	private static String convertUnicode(String str) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\\' && (i + 5) < str.length()) {
				i++;
				if (str.charAt(i) == 'u') {
					i++;
					try {
						out.write(Integer.parseInt(str.substring(i, i += 4), 16));
					} catch (NumberFormatException ex) {
						System.out.println(str);
						throw new RuntimeException();
					}
					i--;
				} else {
					i--;
				}
			} else {
				out.write(c);
			}
		}

		return new String(out.toByteArray());
	}

	@Override
	public List<MessageGroup> getMessageGroups() {
		ArrayList l = new ArrayList();
		l.add(messages);
		return l;
	}
}
