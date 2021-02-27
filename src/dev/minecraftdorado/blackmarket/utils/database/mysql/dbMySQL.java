package dev.minecraftdorado.blackmarket.utils.database.mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import com.google.common.io.Resources;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.ItemStackSerializer;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.SerializeInventory;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData.Data;

public class dbMySQL {
	
	private static MySQL sql;
	private static Connection con;
	
	public static void load() {
		if(sql == null) {
			File file = new File(MainClass.main.getDataFolder(), "config.yml");
			YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
			
			sql = new MySQL(
					yml.isSet("mysql.host") ? yml.getString("mysql.host") : "localhost",
					yml.isSet("mysql.user") ? yml.getString("mysql.user") : "root",
					yml.isSet("mysql.pass") ? yml.getString("mysql.pass") : "",
					yml.isSet("mysql.database") ? yml.getString("mysql.database") : "server",
					yml.isSet("mysql.port") ? yml.getInt("mysql.port") : 3306);
		}
		sql.createTables(Resources.getResource(MainClass.class, "/resources/sql/blackmarket.sql"));
		con = sql.getConnection();
		try {
			loadBlackItems();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void save() {
		for(Data data : PlayerData.list.values())
			for(BlackItem bItem : data.getItems()) {
				updateStatus(bItem);
				updateNotified(bItem);
			}
	}
	
	private static void loadBlackItems() throws SQLException {
		if(con == null || con.isClosed()) con = sql.getConnection();
		
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT *");
            queryBuilder.append("FROM `blackitems` WHERE notified = false");

            preparedStatement = con.prepareStatement(queryBuilder.toString());
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet != null && resultSet.next()) {
            	Status status = Status.valueOf(resultSet.getString("status"));
            	if(status.equals(Status.ON_SALE) || status.equals(Status.TIME_OUT) || status.equals(Status.SOLD)) {
            		UUID uuid = UUID.fromString(resultSet.getString("owner"));
            		
            		ItemStack item = ItemStackSerializer.deserialize(resultSet.getString("item"));
            		
            		if(item != null) {
            			if(resultSet.getString("content") != null && item.getType().name().contains("SHULKER_BOX") && item.getItemMeta() instanceof BlockStateMeta) {
            				BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            				if(meta.getBlockState() instanceof ShulkerBox) {
            					ShulkerBox shulker = (ShulkerBox) meta.getBlockState();
            					
            					try {
            						shulker.getInventory().setContents(SerializeInventory.itemStackArrayFromBase64(resultSet.getString("content")));
            					} catch (Exception ex) {
            						ex.printStackTrace();
            					}
            					
            					meta.setBlockState(shulker);
            					item.setItemMeta(meta);
            				}
            			}
            			
            			
            			BlackItem bItem = new BlackItem(item, resultSet.getDouble("value"), uuid, status, new Date(resultSet.getLong("date")), resultSet.getInt("id"), resultSet.getBoolean("notified"));
            			
            			PlayerData.get(uuid).addItem(bItem);
            		}
            	}
            }
            
            resultSet.close();
            preparedStatement.close();
        } catch (final SQLException sqlException) {
            sqlException.printStackTrace();
        }
	}
	
	public static void updateStatus(BlackItem bItem) {
		try {
			if(con == null || con.isClosed()) con = sql.getConnection();
		} catch(Exception e) {e.printStackTrace();}
        
        try {
        	PreparedStatement preparedStatement = null;
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE `blackitems` SET status = ? WHERE id = ?");
			
			preparedStatement = con.prepareStatement(queryBuilder.toString());
			preparedStatement.setString(1, bItem.getStatus().name());
			preparedStatement.setInt(2, bItem.getId());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
        }
	}
	
	public static void updateNotified(BlackItem bItem) {
		try {
			if(con == null || con.isClosed()) con = sql.getConnection();
		} catch(Exception e) {e.printStackTrace();}
        
        try {
        	PreparedStatement preparedStatement = null;
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE `blackitems` SET notified = ? WHERE id = ?");
			
			preparedStatement = con.prepareStatement(queryBuilder.toString());
			preparedStatement.setBoolean(1, bItem.isNotified());
			preparedStatement.setInt(2, bItem.getId());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
        }
	}
	
	public static void addBlackItem(BlackItem bItem) {
		try {
			if(con == null || con.isClosed()) con = sql.getConnection();
		} catch(Exception e) {e.printStackTrace();}
        
        try {
        	PreparedStatement preparedStatement = null;
			StringBuilder queryBuilder = new StringBuilder();
			
			queryBuilder.append("SELECT * FROM `blackitems` WHERE id = ?");
			preparedStatement = con.prepareStatement(queryBuilder.toString());
			preparedStatement.setInt(1, bItem.getId());
			ResultSet resultSet = preparedStatement.executeQuery();
			
			if(resultSet != null && resultSet.next()) {
				updateStatus(bItem);
				updateNotified(bItem);
				return;
			}
			
			resultSet.close();
			preparedStatement.close();
			queryBuilder = new StringBuilder();
			
			queryBuilder.append("INSERT INTO `blackitems` ");
			
			queryBuilder.append("(`owner`,`value`,`date`,`status`,`item`,`content`) ");
			queryBuilder.append("VALUES ");
			queryBuilder.append("(?,?,?,?,?,?);");
			
			preparedStatement = con.prepareStatement(queryBuilder.toString());
			preparedStatement.setString(1, bItem.getOwner().toString());
			preparedStatement.setDouble(2, bItem.getValue());
			preparedStatement.setLong(3, bItem.getDate().getTime());
			preparedStatement.setString(4, bItem.getStatus().name());
			preparedStatement.setString(5, ItemStackSerializer.serialize(bItem.getOriginal()));
			
			Object content = null;
			if(bItem.getOriginal().getType().name().contains("SHULKER_BOX"))
				if(bItem.getOriginal().getItemMeta() instanceof BlockStateMeta) {
					BlockStateMeta meta = (BlockStateMeta) bItem.getOriginal().getItemMeta();
					if(meta.getBlockState() instanceof ShulkerBox) {
						ShulkerBox shulker = (ShulkerBox) meta.getBlockState();
						content = SerializeInventory.itemStackArrayToBase64(shulker.getInventory().getContents());
						Bukkit.getConsoleSender().sendMessage("§4" + content);
					}
				}
			preparedStatement.setObject(6, content != null ? content : null);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
