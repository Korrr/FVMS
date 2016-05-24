package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import db.tools.Columns;
import db.tools.Config;
import db.tools.Tables;

public class DBConnection {
	private Connection conn;
	private Statement stmt;
	private ResultSet rs;

	private static DBConnection instance = null;

	public static DBConnection getInstance() {
		if (instance == null)
			instance = new DBConnection();
		return instance;
	}

	protected DBConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://"
					+ Config.ULR.getString() + "/fvms?" + "user="
					+ Config.USER.getString() + "&password="
					+ Config.PASSWORD.getString());

		} catch (SQLException ex) {
			logSQLException("DBConnection", ex);
		} catch (Exception ex) {
			Logger.getGlobal().log(Level.FINE, ex.getMessage());
		}
	}

	public int removeRow_byValue(String value, Columns column, Tables table) {
		int rows_removed = 0;
		if (isValuePresentInTable(value, column, table)) {
			String statement = "Delete from " + table + " where " + column
					+ "=\"" + value + "\";";
			rows_removed = executeUpdate(statement);
			if (rows_removed > 0) {
				Logger.getGlobal().info(
						rows_removed + " rows successfully deleted");
			}
			closeResultSetAndStatement();
		}
		return rows_removed;
	}

	public void logSQLException(String method_name, SQLException ex) {
		Logger.getGlobal().log(
				Level.FINE,
				"SQLState: " + ex.getSQLState() + ", " + method_name + ": "
						+ ex.getMessage());
	}

	public void closeResultSetAndStatement() {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException sqlEx) {
				logSQLException("closeResultSetAndStatement", sqlEx);
			}

			rs = null;
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) {
				logSQLException("closeResultSetAndStatement", sqlEx);
			}

			stmt = null;
		}
	}

	public boolean isEmailValid(String email) {
		boolean isAlreadyPresent = isValuePresentInTable(email,
				Columns.USERS_email, Tables.USERS);
		boolean inValidFormat = email
				.matches("[A-Za-z_.0-9]+@[A-Za-z_.0-9]+\\.[A-Za-z_.0-9]+");
		if (isAlreadyPresent) {
			Logger.getGlobal().info("Email is already used: " + email);
		}
		if (!inValidFormat) {
			Logger.getGlobal().info("Email is not in a valid format: " + email);
		}
		return !isAlreadyPresent && inValidFormat;
	}

	public boolean isValuePresentInTable(String value, Columns column,
			Tables table) {
		String statement = "SELECT " + column + " FROM " + table + " WHERE "
				+ column + "=\"" + value + "\";";
		return executeValuePresentStatement(statement);
	}

	public boolean isValuePresentInTable(int value, Columns column, Tables table) {
		String statement = "SELECT " + column + " FROM " + table + " WHERE "
				+ column + "=" + value + ";";
		return executeValuePresentStatement(statement);
	}

	public boolean stmtAndRs_NullOrClosed(String statement) {
		boolean readyToRun = false;
		try {
			String notClosedMessage = "' not closed after last use. Close statement and result set after each use!!";
			;
			if (stmt != null) {
				if (!stmt.isClosed()) {
					Logger.getGlobal().warning(
							"Statement: '" + statement + notClosedMessage);
					readyToRun = true;
				}
			} else {
				readyToRun = true;
			}
			if (rs != null) {
				if (!rs.isClosed()) {
					Logger.getGlobal().warning("Statement " + notClosedMessage);
					readyToRun &= true;
				}
			} else {
				readyToRun = true;
			}
		} catch (SQLException e) {
			logSQLException("stmt_readyToRun", e);
		}
		return readyToRun;
	}

	public boolean executeStatement(String statement) {
		boolean statementRun = false;
		boolean readyToRun = stmtAndRs_NullOrClosed(statement);
		if (readyToRun) {
			try {
				stmt = conn.createStatement();
				if (stmt.execute(statement)) {
					rs = stmt.getResultSet();
					statementRun = true;
				}
			} catch (SQLException ex) {
				logSQLException("executeStatement: " + statement, ex);
			}
		}

		return statementRun;
	}

	public int executeUpdate(String statement) {
		rs = null;
		int rowsUpdated = 0;
		boolean readyToRun = stmtAndRs_NullOrClosed(statement);
		if (readyToRun) {
			try {
				stmt = conn.createStatement();
				rowsUpdated = stmt.executeUpdate(statement);
				if (rowsUpdated > 0) {
					rs = stmt.getResultSet();
				}
			} catch (SQLException ex) {
				logSQLException("executeUpdate: " + statement, ex);
			}
		}
		return rowsUpdated;
	}

	public int getID(String value, Columns valueColumn, Columns idColumn,
			Tables table) {
		String statement = "SELECT " + idColumn.toString() + " FROM " + table
				+ " WHERE " + valueColumn + "=\"" + value + "\";";
		int id = -1;
		try {
			executeStatement(statement);
			rs.first();
			id = rs.getInt(1);
			closeResultSetAndStatement();
		} catch (SQLException e) {
			logSQLException("getID", e);
		}
		return id;
	}

	public int insertRowIntoTable(String values, Tables table) {
		String statement = "INSERT INTO " + table + " VALUES(" + values + ");";
		int rows_affected = executeUpdate(statement);
		closeResultSetAndStatement();
		return rows_affected;
	}

	public ResultSet getResultSet() {
		return rs;
	}

	private boolean executeValuePresentStatement(String statement) {
		executeStatement(statement);
		boolean result = false;
		try {
			result = rs.first();
		} catch (SQLException e) {
			logSQLException("isValuePresentInTable:" + statement, e);
		}
		closeResultSetAndStatement();
		return result;
	}

}