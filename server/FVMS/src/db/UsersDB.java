package db;

import java.sql.SQLException;
import java.util.logging.Logger;

import config.Tools;
import db.tools.Columns;
import db.tools.Messages;
import db.tools.Tables;

public class UsersDB extends DBConnection {
	private static UsersDB instance = null;

	private UsersDB() {
		super();
	}

	public static UsersDB getInstance() {
		if (instance == null)
			instance = new UsersDB();
		return instance;
	}

	public void printAllUsers() {
		String statement = "Select * from users";
		executeStatement(statement);
		try {
			while (getResultSet().next()) {
				System.out.println("user: "
						+ getResultSet().getString("user_name").toString()
						+ "email: "
						+ getResultSet().getString("email").toString());
			}
		} catch (SQLException e) {
			logSQLException("printAllUsers", e);
		}

		closeResultSetAndStatement();
	}

	public boolean doesUserExist(String email) {
		return isValuePresentInTable(email, Columns.USERS_email, Tables.USERS);
	}

	public String insertUser(String username, String password, String email) {
		String result_message = "";
		boolean username_ok = !isValuePresentInTable(username,
				Columns.USERS_username, Tables.USERS);
		boolean email_ok = isEmailValid(email);
		if (username_ok && email_ok) {
			password = Tools.hashString(password);
			String statement = "insert into users values(default,\"" + email
					+ "\",\"" + username + "\",\"" + password + "\");";
			executeStatement(statement);
			closeResultSetAndStatement();
			result_message = Messages.User_inserted.toString();
		} else {
			if (!username_ok) {
				result_message += Messages.Username_used.toString();
			}
			if (!email_ok) {
				result_message += Messages.Email_invalid.toString();
			}
		}
		return result_message;
	}

	public String removeUser_byUsername(String user_name) {
		return removeUser(user_name, Columns.USERS_username);
	}

	public String removeUser_byEmail(String email) {
		return removeUser(email, Columns.USERS_email);
	}

	public String checkUser_byUsername(String user_name, String password) {
		return checkUser(user_name, password, Columns.USERS_username);
	}

	public String checkUser_byEmail(String email, String password) {
		return checkUser(email, password, Columns.USERS_email);
	}

	public String insertTeam(String team) {
		String result = "";
		if (team.trim().isEmpty()) {
			result = Messages.Value_Empty.toString();
		} else {
			if (isValuePresentInTable(team, Columns.TEAMS_name, Tables.TEAMS)) {
				result = Messages.Team_alreadyExists.toString();
			} else {
				String statement = "INSERT INTO TEAMS VALUES(default, \""
						+ team + "\");";
				int rows_affected = executeUpdate(statement);
				if (rows_affected > 0) {
					result = Messages.Team_inserted.toString();
				} else {
					result = Messages.Team_insertionFailed.toString();
				}
				closeResultSetAndStatement();
			}
		}
		return result;
	}

	public String removeTeam(String team) {
		String result = "";
		if (team.trim().isEmpty()) {
			result = Messages.Value_Empty.toString();
		} else {
			if (!isValuePresentInTable(team, Columns.TEAMS_name, Tables.TEAMS)) {
				result = Messages.Team_notExists.toString();
			} else {
				int rows_affected = removeRow_byValue(team, Columns.TEAMS_name,
						Tables.TEAMS);
				if (rows_affected > 0) {
					result = Messages.Team_removed.toString();
				} else {
					result = Messages.Team_removalFailed.toString();
				}
			}
		}
		return result;
	}

	public String addUserToTeam_byUsername(String user, String team) {
		return addUser_toTeam(user, Columns.USERS_username, team);
	}

	public String addUserToTeam_byEmail(String email, String team) {
		return addUser_toTeam(email, Columns.USERS_email, team);
	}

	public String addTeamLider_byUsername(String user, String team) {
		return addTeamLider(user, team, Columns.USERS_username);
	}

	public String addTeamLider_byEmail(String email, String team) {
		return addTeamLider(email, team, Columns.USERS_email);
	}

	public String removeUserFromTeam_byUsername(String user, String team) {
		return removeUserOrLiderFromTeam(user, team, Columns.USERS_username,
				Tables.TEAM_USERS);
	}

	public String removeUserFromTeam_byEmail(String email, String team) {
		return removeUserOrLiderFromTeam(email, team, Columns.USERS_email,
				Tables.TEAM_USERS);
	}

	public String removeLiderFromTeam_byUsername(String user, String team) {
		return removeUserOrLiderFromTeam(user, team, Columns.USERS_username,
				Tables.TEAM_LEADERS);
	}

	public String removeLiderFromTeam_byEmail(String email, String team) {
		return removeUserOrLiderFromTeam(email, team, Columns.USERS_email,
				Tables.TEAM_LEADERS);
	}

	public String isUserInTeam_byUsername(String user, String team) {
		return isUserInORLiderOfTeam(user, team, Columns.USERS_username,
				Tables.TEAM_USERS).toString();
	}

	public String isUserInTeam_byEmail(String email, String team) {
		return isUserInORLiderOfTeam(email, team, Columns.USERS_email,
				Tables.TEAM_USERS).toString();
	}

	public String isUserLiderOfTeam_byUsername(String user, String team) {
		return isUserInORLiderOfTeam(user, team, Columns.USERS_username,
				Tables.TEAM_LEADERS).toString();
	}

	public String isUserLiderOfTeam_byEmail(String email, String team) {
		return isUserInORLiderOfTeam(email, team, Columns.USERS_email,
				Tables.TEAM_LEADERS).toString();
	}

	private String removeUserOrLiderFromTeam(String value, String team,
			Columns userColumn, Tables teamTable) {
		Messages result = null;
		result = teamAndUserExist(value, team, userColumn);
		if (result.equals(Messages.OK)) {
			int uid = getID(value, userColumn, Columns.USERS_Id, Tables.USERS);
			int tid = getID(team, Columns.TEAMS_name, Columns.TEAMS_Id,
					Tables.TEAMS);
			String statement = "DELETE FROM " + teamTable + " WHERE "
					+ Columns.USERS_Id + "=" + uid + " AND " + Columns.TEAMS_Id
					+ "=" + tid + ";";
			int rows_affected = executeUpdate(statement);
			if (rows_affected > 0) {
				result = Messages.UserRemovalFromTeam_Succeeded;
			} else {
				result = Messages.UserRemovalFromTeam_Failed;
			}
			closeResultSetAndStatement();
		}

		return result.toString();
	}

	private Messages teamAndUserExist(String user, String team,
			Columns userColumn) {
		Messages result = Messages.OK;
		if (!isValuePresentInTable(user, userColumn, Tables.USERS)) {
			result = Messages.User_inexistend;
		} else {
			if (!isValuePresentInTable(team, Columns.TEAMS_name, Tables.TEAMS)) {
				result = Messages.Team_notExists;
			}
		}
		return result;
	}

	private String addUser_toTeam(String user, Columns user_column, String team) {
		Messages result = null;
		result = teamAndUserExist(user, team, user_column);
		if (result.equals(Messages.OK)) {
			int uid = getID(user, user_column, Columns.USERS_Id, Tables.USERS);
			int tid = getID(team, Columns.TEAMS_name, Columns.TEAMS_Id,
					Tables.TEAMS);
			result = isUserInORLiderOfTeam(user, team, user_column,
					Tables.TEAM_USERS);
			if (result.equals(Messages.User_notMemberOFTheTeam)) {
				String statement = "INSERT INTO " + Tables.TEAM_USERS
						+ " VALUES(" + uid + "," + tid + ");";
				int rows_affected = executeUpdate(statement);
				if (rows_affected > 0) {
					result = Messages.User_addedToTeam;
				} else {
					result = Messages.AddingToTeam_Failed;
				}
			} else {
				result = Messages.User_alreadyInTeam;
			}
			closeResultSetAndStatement();
		}
		return result.toString();
	}

	private Messages isUserInORLiderOfTeam(String user, String team,
			Columns userColumn, Tables tableLookingIn) {
		Messages result = null;
		result = teamAndUserExist(user, team, userColumn);
		if (result.equals(Messages.OK)) {
			int uid = getID(user, userColumn, Columns.USERS_Id, Tables.USERS);
			int tid = getID(team, Columns.TEAMS_name, Columns.TEAMS_Id,
					Tables.TEAMS);
			String statement = "SELECT * FROM " + tableLookingIn + " WHERE "
					+ Columns.TEAMS_Id + "=" + tid + " AND " + Columns.USERS_Id
					+ "=" + uid + ";";
			executeStatement(statement);
			try {
				boolean lookingIn_TeamUsers = tableLookingIn
						.equals(Tables.TEAM_USERS);
				if (getResultSet().first()) {
					result = lookingIn_TeamUsers ? Messages.User_isMemberOfTheTeam
							: Messages.User_isLiderOfTheTeam;
				} else {
					result = lookingIn_TeamUsers ? Messages.User_notMemberOFTheTeam
							: Messages.User_notLiderOfTheTeam;
				}
			} catch (SQLException e) {
				logSQLException("isUserInTeam", e);
				result = Messages.OperationFailed;
			}
			closeResultSetAndStatement();
		}
		return result;
	}

	private String addTeamLider(String value, String team, Columns userColumn) {
		Messages result = null;
		result = teamAndUserExist(value, team, userColumn);
		if (result.equals(Messages.OK)) {
			result = isUserInORLiderOfTeam(value, team, userColumn,
					Tables.TEAM_LEADERS);
			if (result.equals(Messages.User_notLiderOfTheTeam)) {
				int tid = getID(team, Columns.TEAMS_name, Columns.TEAMS_Id,
						Tables.TEAMS);
				int uid = getID(value, userColumn, Columns.USERS_Id,
						Tables.USERS);
				int affected_rows = insertRowIntoTable("" + uid + "," + tid,
						Tables.TEAM_LEADERS);
				if (affected_rows > 0) {
					result = Messages.TeamLider_Added;
				} else {
					result = Messages.TeamLider_AddingFailed;
				}
			} else {
				result = Messages.User_alreadyTeamLider;
			}
		}
		return result.toString();
	}

	private String removeUser(String user, Columns userColumn) {
		String result = "";
		int rows = removeRow_byValue(user, userColumn, Tables.USERS);
		if (rows > 0) {
			result = Messages.User_deleted.toString();
		} else {
			result = Messages.User_inexistend.toString();
		}
		return result;
	}

	private String checkUser(String value, String password, Columns column) {
		String result = "";
		String statement = "SELECT pass FROM " + Tables.USERS.toString()
				+ " WHERE " + column + "=\"" + value + "\";";
		executeStatement(statement);
		try {
			if (!getResultSet().first()) {
				result = Messages.User_inexistend.toString();
			} else {
				String actual_password = getResultSet().getString(
						Columns.USERS_password.toString()).toString();
				password = Tools.hashString(password);
				if (actual_password.equals(password)) {
					result = Messages.Login_succesfull.toString();
				} else {
					result = Messages.Login_failed.toString();
					Logger.getGlobal().info(
							"Login failed: '" + password
									+ "' does not match actual password: "
									+ actual_password);
				}
			}
		} catch (SQLException e) {
			logSQLException("check_user", e);
		}
		closeResultSetAndStatement();
		return result;
	}

}