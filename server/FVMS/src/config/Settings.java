package config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import utils.Logger;

public class Settings {
	private static Properties properties = null;

	public static void loadProperties(String load_file) {
		try {
			FileInputStream fis = new FileInputStream(load_file);
			properties = new Properties();
			properties.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			Logger.logERROR(e);
		} catch (IOException e) {
			Logger.logERROR(e);
		}
		DB_URL = properties.getProperty("db_url");
		DB_NAME = properties.getProperty("db_name");
		DB_USER=properties.getProperty("db_user");
		DB_ADMIN_USERNMANE=properties.getProperty("db_admin_user");
		DB_ADMIN_EMAIL=properties.getProperty("db_admin_email");

		TRASH_ENABLED = Boolean.parseBoolean(properties.getProperty("trash_enabled"));
		TRASH_EXPTIME = Integer.parseInt(properties.getProperty("trash_expirationTime"));
		TRASH_MAXSIZE = Integer.parseInt(properties.getProperty("trash_limit"));
		TRASH_FOLDER = properties.getProperty("trash_folder");
		
		TEMP_PATH = properties.getProperty("temp_path");
		
		FS_ROOTFOLDER = properties.getProperty("fileSystem_rootFolder");
		
		CONN_QLOGIN = properties.getProperty("conn_loginQueue");
		CONN_QINIT = properties.getProperty("conn_initQueue");
		CONN_QLOGOUT = properties.getProperty("conn_logoutQueue");
		CONN_HOST = properties.getProperty("conn_host");
		CONN_QHISTORY = properties.getProperty("conn_historyQueue");
		CONN_QUPLOAD = properties.getProperty("conn_uploadQueue");
		CONN_QDOWNLOAD = properties.getProperty("conn_downloadQueue");
		
		LOGGER_WRITER = properties.getProperty("logger_writer");
		
	}
	
	public static String DB_URL="localhost";
	public static String DB_NAME="fvms";
	public static String DB_USER="root";
	public static String DB_PASSWORD="parola";
	public static String DB_ADMIN_USERNMANE="default";
	public static String DB_ADMIN_EMAIL="default";
	public static boolean TRASH_ENABLED = true;
	public static int TRASH_EXPTIME = 10;
	public static int TRASH_MAXSIZE = 1024;
	public static String TEMP_PATH = "./TEMP";
	public static String TRASH_FOLDER="./TRASH";
	public static String FS_ROOTFOLDER = "./";
	public static String CONN_QLOGIN="QLogin";
	public static String CONN_HOST="localost";
	public static String CONN_QINIT = "QInit";
	public static String CONN_QLOGOUT = "QLogout";
	public static String CONN_QHISTORY="QHistory";
	public static String LOGGER_WRITER="console";
	public static String CONN_QUPLOAD = "QSaveChanges";
	public static String CONN_QDOWNLOAD = "QDownload";
	public static String Conn_QFILEDOWNLOAD = "QFileDownload";
}
