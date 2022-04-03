package dev.minecraftdorado.blackmarket.utils.database.mysql;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;

public class MySQL {
	
	private Connection con;
	
	private String host, user, pass, database;
	private int port = 3306;
	
	public MySQL(String host, String user, String pass, String database, int port) {
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.database = database;
		this.port = port;
	}
    
	public Connection getConnection() {
		try {
			if(con != null && !con.isClosed()) {return con;}

    		Class.forName("com.mysql.jdbc.Driver");
    		con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", user, pass);
    	}catch(Exception ex) {
    		MainClass.main.getLogger().severe(String.format("» MySQL - Database not found: " + database, MainClass.main.getDescription().getName()));
    	}

		return con;
	}

	public void executeScript(String resource) {
		String currentScript = null;
		try {
			String[] databaseStructure = Resources.toString(Resources.getResource(MainClass.class, resource), Charsets.UTF_8).split(";;");

			Statement st = getConnection().createStatement();

			for(int i = 0; i < databaseStructure.length; i++){
				if(!databaseStructure[i].trim().equals("")){
					currentScript = databaseStructure[i];
					st.executeUpdate(currentScript);
				}
			}
		} catch (Exception e) {
			System.out.println("*** Error : " + e.toString());
			System.out.println("*** ");
			System.out.println("*** Script : " + currentScript);
			System.out.println("################################################");
		}
	}
}