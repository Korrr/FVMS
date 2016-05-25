package db;

import java.sql.SQLException;

import utils.Logger;
import db.tools.Config;
import config.Settings;
import db.tools.Columns;
import db.tools.Messages;
import db.tools.Tables;

public class FileSystemDB {
	private DBConnection db = null;
	private UsersDB udb = null;
	private static FileSystemDB instance = null;
	private static int nextAvailableFileID = 0;

	private FileSystemDB() {
		db = DBConnection.getInstance();
		udb = UsersDB.getInstance();
	}

	public static FileSystemDB getInstance() {
		if (instance == null) {
			instance = new FileSystemDB();
		}
		return instance;
	}

	public String addNewFile(int fid, String email, String path, String hash,
			String datetime) {
		return insertChange(fid, fid, datetime, hash, email,
				Config.NEWFILE_DEFAULTMESSAGE.toString(), path);
	}

	public int getNextAvailableID() {
		if (nextAvailableFileID <= 0) {
			String statement = "SELECT MAX(" + Columns.Changes_ID + ") FROM "
					+ Tables.CHANGES;
			boolean weHaveResults = db.executeStatement(statement);
			if (weHaveResults) {
				try {
					if (db.getResultSet().first()) {
						nextAvailableFileID = db.getResultSet().getInt(1) + 1;
					}
				} catch (SQLException e) {
					db.logSQLException("getNextAvailableID", e);
				}
				db.closeResultSetAndStatement();
			} else {
				Logger.logWARNING("Last CID could not be read.");
			}
		} else {
			nextAvailableFileID += 1;
		}

		return nextAvailableFileID;
	}

	public boolean isFileInDB(String hash) {
		boolean result = false;
		result = db.isValuePresentInTable(hash, Columns.Changes_Hash,
				Tables.CHANGES);
		return result;
	}

	public String addChange(int originalFileID, String datetime, String hash,
			String owner, String message, String path) {
		int cid = getNextAvailableID();
		return insertChange(cid, originalFileID, datetime, hash, owner,
				message, path);
	}

	public int removeFileByHash(String hash) {
		return db.removeRow_byValue(hash, Columns.Changes_Hash, Tables.CHANGES);
	}

	public String getLastChangePath(int fid) {
		String result = null;
		if (db.isValuePresentInTable(fid, Columns.Changes_FID, Tables.CHANGES)) {
			String statement = "SELECT LAST(" + Columns.Changes_Path
					+ ") FROM " + Tables.CHANGES;
			result = getPath(statement);
		} else {
			result = Messages.File_doesNotExist.toString();
		}

		return result;
	}

	public int getChangeID(String hash) {
		return db.getID(hash, Columns.Changes_Hash, Columns.Changes_ID,
				Tables.CHANGES);
	}

	public int getOriginalFileID(String hash) {
		return db.getID(hash, Columns.Changes_Hash, Columns.Changes_FID,
				Tables.CHANGES);
	}

	public String getFilePath(String hash) {
		String result = null;
		boolean fileExists = db.isValuePresentInTable(hash,
				Columns.Changes_Hash, Tables.CHANGES);
		if (fileExists) {
			String statement = "SELECT " + Columns.Changes_Path + " FROM "
					+ Tables.CHANGES + " WHERE " + Columns.Changes_Hash + "=\""
					+ hash + "\";";
			result = getPath(statement);

		}
		return result;
	}

	public int removeAllFiles() {
		int rows_deleted = 0;

		String statement = "Delete from " + Tables.CHANGES;
		rows_deleted = db.executeUpdate(statement);
		db.closeResultSetAndStatement();
		return rows_deleted;
	}
	public int moveFileToTrash(int id){
		int filesUpdated = 0;
		String statement = "UPDATE " + Tables.CHANGES + " SET " + Columns.Changes_Status + "=" + Config.STATUS_MOVEDTOTRASH + " WHERE " + Columns.Changes_FID + "=" + id;
		filesUpdated = db.executeUpdate(statement);
		db.closeResultSetAndStatement();
		return filesUpdated;
	}

	public String[] getFileVersions(int id) {
		String statement = "SELECT COUNT(" + Columns.Changes_FID + ") FROM "
				+ Tables.CHANGES;
		db.executeUpdate(statement);
		statement = "SELECT " + Columns.Changes_Path + " FROM "
				+ Tables.CHANGES + " WHERE " + Columns.Changes_FID + "=\"" + id
				+ "\";";
		int filesNo = db.executeUpdate(statement);
		db.closeResultSetAndStatement();
		String[] result = null;
		if (filesNo > 0) {
			result = new String[filesNo];
			int index = 0;
			try {
				while (db.getResultSet().next()) {
					String path = db.getResultSet().getString(1);
					result[index] = path;
					index++;
				}
			} catch (SQLException e) {
				Logger.logERROR(e);
			}
		}
		return result;
	}

	public String getFileFolder(int id) {
		return Settings.FS_ROOTFOLDER + id;
	}

	private String insertChange(int cid, int id, String datetime, String hash,
			String owner, String message, String path) {
		Messages result = null;
		boolean fileIsAlreadySaved = isFileInDB(hash);
		if (fileIsAlreadySaved) {
			result = Messages.File_alreadySaved;
		} else {
			int uid = udb.getID(owner, Columns.USERS_email, Columns.USERS_Id,
					Tables.USERS);
			String values = cid + "," + id + ", \"" + datetime + "\",\"" + hash
					+ "\"," + uid + ",\"" + message + "\",\"" + path + "\"," + Config.STATUS_AVAILABLE;
			int rows_affected = db.insertRowIntoTable(values, Tables.CHANGES);
			if (rows_affected > 0) {
				result = Messages.Change_added;
			} else {
				result = Messages.Change_addingFailed;
			}
		}
		return result.toString();
	}
	public int getStatus(int id){
		int status = -1;
		String statement = "SELECT " + Columns.Changes_Status + " FROM " + Tables.CHANGES + " WHERE " + Columns.Changes_FID+"="+id;
		db.executeStatement(statement);
		try {
			db.getResultSet().first();
			status= db.getResultSet().getInt(1);
		} catch (SQLException e) {
			Logger.logERROR(e);
		}
		return status;
	}
	private String getPath(String statement) {
		String result = null;
		boolean weHaveResults = false;

		db.executeStatement(statement);
		try {
			weHaveResults = db.getResultSet().first();
			if (weHaveResults) {
				result = db.getResultSet().getString(1);
			} else {
				result = Messages.NoFileFoundWithSpecifiedID.toString();
			}
		} catch (SQLException e) {
			db.logSQLException("getPath", e);
		}

		db.closeResultSetAndStatement();
		return result;
	}

}
