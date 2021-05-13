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

public class ViberIOSData implements IInput {

	public static final List<String> BADBADBADBADBAD = new ArrayList<>();

	static {
		BADBADBADBADBAD.add("Sára");
	}

	private List<MessageGroup> messageGroups = new ArrayList<>();
	private List<ChatInfo> chatInfoList = new ArrayList<>();
	private List<Contact> contactList = new ArrayList<>();

	public ViberIOSData(File f) {
		try {
			Connection con = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath().replace("\\", "/"));

			Statement stm = con.createStatement();
			ResultSet chatInfos = stm.executeQuery("SELECT * FROM ZCONVERSATION");
			while (chatInfos.next()) {
				chatInfoList.add(new ChatInfo(chatInfos));
			}

			stm = con.createStatement();
			ResultSet contactInfos = stm.executeQuery("SELECT * FROM ZMEMBER");
			while (contactInfos.next()) {
				contactList.add(new Contact(contactInfos));
			}

			stm = con.createStatement();
			ResultSet messages = stm.executeQuery("SELECT * FROM ZVIBERMESSAGE");

			Map<MessageChatGroupInfo, MessageGroup> groups = new HashMap<>();

			while (messages.next()) {
				String type = messages.getString("ZSYSTEMTYPE");
				if (type == null) {
					String msg = messages.getString("ZTEXT");
					if (msg != null) {
						msg = msg.trim();
						if (!msg.isEmpty()) {

							MessageChatGroupInfo hdr = new MessageChatGroupInfo(messages);

							MessageGroup grp = groups.get(hdr);

							if (grp == null) {
								grp = new MessageGroup();
								groups.put(hdr, grp);
							} else {
								for (MessageChatGroupInfo hdr2 : groups.keySet()) {
									if (hdr.equals(hdr2)) {
										hdr = hdr2;
										break;
									}
								}
							}
							hdr.addContactId(messages);

							grp.messages.add(new Message(getContactName(getContactId(messages)), msg, cocoa2Unix(messages.getFloat("ZDATE"))));
						}
					}
				}
			}

			for (Map.Entry<MessageChatGroupInfo, MessageGroup> e : groups.entrySet()) {
				if (isChatInfoGroup(e.getKey().chatID)) {
					e.getValue().name = getGroupName(e.getKey().chatID);
				} else {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < e.getKey().contactIDs.size(); i++) {
						int cid = e.getKey().contactIDs.get(i);
						if (i != 0) {
							sb.append(" + ");
						}
						sb.append(getContactName(cid));
					}
					e.getValue().name = sb.toString();
				}
			}

			Outer:
			for (MessageGroup wg : groups.values()) {
				for (String badbadbad : BADBADBADBADBAD) {
					if (wg.name.contains(badbadbad)) {
						continue Outer;
					}
				}
				messageGroups.add(wg);
			}

			con.close();
		} catch (SQLException ex) {
			Logger.getLogger(ViberIOSData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static long cocoa2Unix(float cocoa){
		return (long)(978307200000L + cocoa * 1000); //1.1.2001 + (ms)cocoa
	}

	private boolean isChatInfoGroup(int chatID) {
		for (ChatInfo i : chatInfoList) {
			if (i.id == chatID) {
				return i.isGroup;
			}
		}
		return false;
	}

	private String getContactName(int contactId) {
		for (Contact con : contactList) {
			if (con.id == contactId) {
				return con.getGoodName();
			}
		}
		return null;
	}

	public static int getContactId(ResultSet rsl) throws SQLException {
		int id = rsl.getInt("ZPHONENUMINDEX");
		return id;
	}

	private String getGroupName(int groupId) {
		for (ChatInfo chat : chatInfoList) {
			if (chat.id == groupId) {
				return chat.name;
			}
		}
		return null;
	}

	public static class MessageChatGroupInfo {

		public int chatID;
		public List<Integer> contactIDs = new ArrayList<>();

		public MessageChatGroupInfo(ResultSet rsl) throws SQLException {
			chatID = rsl.getInt("ZCONVERSATION");
		}

		public void addContactId(ResultSet rsl) throws SQLException {
			int id = getContactId(rsl);
			if (!contactIDs.contains(id)){
				contactIDs.add(id);
			}
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 79 * hash + this.chatID;
			//hash = 79 * hash + this.contactID;
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
			final MessageChatGroupInfo other = (MessageChatGroupInfo) obj;
			if (this.chatID != other.chatID) {
				return false;
			}
			return true;
		}
	}

	public static class Contact {

		public int id;
		public String name;
		public String clientName;

		public Contact(ResultSet rsl) throws SQLException {
			id = rsl.getInt("Z_PK");
			name = rsl.getString("ZNAME");
			clientName = rsl.getString("ZDISPLAYSHORTNAME");
		}

		public String getGoodName() {
			StringBuilder sb = new StringBuilder();

			boolean hasName = name != null;
			boolean hasCName = clientName != null;

			if (hasName) {
				sb.append(name);
			}
			if (hasCName) {
				if (hasName) {
					sb.append("/");
				}
				sb.append(clientName);
			}

			return sb.toString();
		}
	}

	public static class ChatInfo {

		public int id;
		public String name;
		public boolean isGroup;

		public ChatInfo(ResultSet rsl) throws SQLException {
			id = rsl.getInt("Z_PK");
			name = rsl.getString("ZNAME");
			isGroup = (rsl.getInt("ZUSERSETTINGS") & 8) != 0;
		}
	}

	@Override
	public List<MessageGroup> getMessageGroups() {
		return messageGroups;
	}
}
