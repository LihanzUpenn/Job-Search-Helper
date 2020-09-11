package db;

public class MySQLDBUtil {
	private static final String INSTANCE = "jobsearchengineprojectdatabase.cgef2gx0uggi.us-east-2.rds.amazonaws.com"; // host name
	private static final String PORT_NUM = "3306";
	public static final String DB_NAME = "SearchEngineProjectData"; // Database name
	private static final String USERNAME = "zhulihan";
	private static final String PASSWORD = "Lihanz123";
	public static final String URL = "jdbc:mysql://"
			+ INSTANCE + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";

}
