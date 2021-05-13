package czenglish.input;

import czenglish.LangUtilities;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessengerDB implements IInput {
	
	private List<MessageGroup> wordGroups = new ArrayList<>();

	public MessengerDB(File f) {
		try {
			Connection con = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath().replace("\\", "/"));

			Statement stm = con.createStatement();
			ResultSet rsl = stm.executeQuery("SELECT * FROM messages");

			MessageGroup grp = new MessageGroup();
			grp.name = f.getName();

			while (rsl.next()) {
				String msg = rsl.getString("text");
				if (msg != null) {
					msg = msg.trim();
					if (!msg.isEmpty()) {
						grp.messages.add(new Message(getNazevKontaktu(rsl.getString("sender")), msg, rsl.getLong("timestamp_ms")));
					}
				}
			}
			
			wordGroups.add(grp);
			
			con.close();
		} catch (SQLException ex) {
			Logger.getLogger(ViberDB.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static String getNazevKontaktu(String odpad){
		String namekey = "\"name\":\"";
		int index = odpad.indexOf(namekey) + namekey.length();
		int indexKonce = odpad.indexOf('"', index);
		return odpad.substring(index, indexKonce);
	}
	
	@Override
	public List<MessageGroup> getMessageGroups() {
		return wordGroups;
	}
}
