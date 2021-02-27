package dev.minecraftdorado.blackmarket.utils.database.mysql;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
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
    		Class.forName("com.mysql.jdbc.Driver");
    		con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", user, pass);
    	}catch(Exception ex) {
    		MainClass.main.getLogger().severe(String.format("» MySQL - Database not found: " + database, MainClass.main.getDescription().getName()));
    	}
		
		return con;
	}
	
    public void createTables(URL url){
    	try{
    		if(con == null || con.isClosed()) con = getConnection();
    		String[] databaseStructure = Resources.toString(url, Charsets.UTF_8).split(";");
    		
    		if (databaseStructure.length == 0)
    			return;
    		
    		Statement statement = null;
    		
    		try {
    			con.setAutoCommit(false);
    			statement = con.createStatement();
    			
    			for (String query : databaseStructure) {
    				query = query.trim();
    				
    				if (query.isEmpty()) {
    					continue;
    				}
    				
    				statement.execute(query);
    			}
    			
    			con.commit();
    			
    		} finally {
    			con.setAutoCommit(true);
    			
    			if (statement != null && !statement.isClosed())
    				statement.close();
    		}
    	}catch(Exception ex){
    		String[] s = url.toString().split("/");
    		MainClass.main.getLogger().severe(String.format("» MySQL can't create the table or already exist: " + s[s.length-1], MainClass.main.getDescription().getName()));
    	}
    }
}