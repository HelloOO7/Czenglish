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

public class ViberDB implements IInput {

	public static final List<String> BADBADBADBADBAD = new ArrayList<>();
	public static final List<String> GOODGOODGOOD = new ArrayList<>();

	public static final boolean BLACKLIST_ENABLE = true;
	public static final boolean WHITELIST_ENABLE = true;

	static {
		BADBADBADBADBAD.add("Sára");
		//BADBADBADBADBAD.add("GJK");
		GOODGOODGOOD.add("GJK");
	}

	private List<MessageGroup> wordGroups = new ArrayList<>();
	private List<ChatInfo> chatInfoList = new ArrayList<>();
	private List<Contact> contactList = new ArrayList<>();

	public ViberDB(File f) {
		try {
			Connection con = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath().replace("\\", "/"));

			Statement stm = con.createStatement();
			ResultSet chatInfos = stm.executeQuery("SELECT * FROM ChatInfo");
			while (chatInfos.next()) {
				chatInfoList.add(new ChatInfo(chatInfos));
			}

			stm = con.createStatement();
			ResultSet contactInfos = stm.executeQuery("SELECT * FROM Contact");
			while (contactInfos.next()) {
				contactList.add(new Contact(contactInfos));
			}

			stm = con.createStatement();
			ResultSet rsl = stm.executeQuery("SELECT * FROM MessageInfo");

			Map<ChatGroupInfo, MessageGroup> groups = new HashMap<>();

			while (rsl.next()) {
				String msg = rsl.getString("Body");
				if (msg != null) {
					msg = msg.trim();
					if (!msg.isEmpty()) {

						ChatGroupInfo hdr = new ChatGroupInfo(rsl);

						MessageGroup grp = groups.get(hdr);

						if (grp == null) {
							grp = new MessageGroup();
							groups.put(hdr, grp);
						} else {
							for (ChatGroupInfo hdr2 : groups.keySet()) {
								if (hdr.equals(hdr2)) {
									hdr = hdr2;
									break;
								}
							}
						}
						hdr.addContactId(rsl);

						grp.messages.add(new Message(getContactName(getContactId(rsl)), msg, rsl.getLong("TimeStamp")));
					}
				}
			}

			for (Map.Entry<ChatGroupInfo, MessageGroup> e : groups.entrySet()) {
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
				boolean wl = true;
				boolean bl = false;
				if (BLACKLIST_ENABLE) {
					for (String badbadbad : BADBADBADBADBAD) {
						if (wg.name.contains(badbadbad)) {
							bl = true;
							break;
						}
					}
				}
				if (WHITELIST_ENABLE) {
					wl = false;
					for (String goodgood : GOODGOODGOOD) {
						if (wg.name.contains(goodgood)) {
							wl = true;
							break;
						}
					}
				}
				if (wl && !bl) {
					wordGroups.add(wg);
				}
				else {
					//System.out.println("omitting group " + wg.name);
				}
			}

			con.close();
		} catch (SQLException ex) {
			Logger.getLogger(ViberDB.class.getName()).log(Level.SEVERE, null, ex);
		}
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

	private String getGroupName(int groupId) {
		for (ChatInfo chat : chatInfoList) {
			if (chat.id == groupId) {
				return chat.name;
			}
		}
		return null;
	}

	public static int getContactId(ResultSet rsl) throws SQLException {
		int id = rsl.getInt("ContactID");
		return id;
	}

	public static class ChatGroupInfo {

		public int chatID;
		public List<Integer> contactIDs = new ArrayList<>();

		public ChatGroupInfo(ResultSet rsl) throws SQLException {
			chatID = rsl.getInt("ChatID");
		}

		public void addContactId(ResultSet rsl) throws SQLException {
			int id = getContactId(rsl);
			if (!contactIDs.contains(id)) {
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
			final ChatGroupInfo other = (ChatGroupInfo) obj;
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
			id = rsl.getInt("ContactID");
			name = rsl.getString("Name");
			clientName = rsl.getString("ClientName");
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
			id = rsl.getInt("ChatID");
			name = rsl.getString("Name");
			isGroup = (rsl.getInt("Flags") & 4) != 0;
		}
	}

	@Override
	public List<MessageGroup> getMessageGroups() {
		return wordGroups;
	}
}
