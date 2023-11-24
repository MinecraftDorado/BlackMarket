package dev.minecraftdorado.blackmarket.utils.database.mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;
import dev.minecraftdorado.blackmarket.utils.market.Market;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;

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
		loadBlackItems();
	}
	
	public static void loadBlackItems() {
		try {
			if(con == null || con.isClosed()) con = sql.getConnection();
		} catch(Exception e) {e.printStackTrace();}
		
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM `blackitems` WHERE notified = false AND status != 'TAKED' AND id > " + Market.getId());
            
            preparedStatement = con.prepareStatement(queryBuilder.toString());
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet != null && resultSet.next())
            	addItem(resultSet, UUID.fromString(resultSet.getString("owner")));
            
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
			preparedStatement.setString(1, bItem.status.name());
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
					}
				}
			preparedStatement.setObject(6, content != null ? content : null);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void checkUnnotified(UUID uuid) {
		if(!Bukkit.getOfflinePlayer(uuid).isOnline())
			return;
		
		try {
			if(con == null || con.isClosed()) con = sql.getConnection();
		} catch(Exception e) {e.printStackTrace();}
		
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM `blackitems` WHERE notified = false AND status = 'SOLD' AND owner = '" + uuid.toString() + "'");
            
            preparedStatement = con.prepareStatement(queryBuilder.toString());
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet != null && resultSet.next()) {
            	BlackItem bItem = addItem(resultSet, uuid);
            	if(bItem != null)
            		bItem.sendNotification();
            }
            
            resultSet.close();
            preparedStatement.close();
        } catch (final SQLException sqlException) {
            sqlException.printStackTrace();
        }
	}
	
	public static void checkStorage(UUID uuid) {
		if(!Bukkit.getOfflinePlayer(uuid).isOnline())
			return;
		
		try {
			if(con == null || con.isClosed()) con = sql.getConnection();
		} catch(Exception e) {e.printStackTrace();}
		
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM `blackitems` WHERE status = 'TIME_OUT' AND notified = false AND owner = '" + uuid.toString() + "'");
            
            preparedStatement = con.prepareStatement(queryBuilder.toString());
            resultSet = preparedStatement.executeQuery();
            
            ArrayList<Integer> ids = new ArrayList<>();
            
            PlayerData.get(uuid).getStorage().forEach(bItem -> ids.add(bItem.getId()));
            
            while (resultSet != null && resultSet.next()) {
            	if(!ids.contains(resultSet.getInt("id")))
            		addItem(resultSet, uuid);
            }
            
            resultSet.close();
            preparedStatement.close();
        } catch (final SQLException sqlException) {
            sqlException.printStackTrace();
        }
	}
	
	public static Status getStatus(int id) {
		try {
			if(con == null || con.isClosed()) con = sql.getConnection();
		} catch(Exception e) {e.printStackTrace();}
		
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        Status status = Status.ON_SALE;
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM `blackitems` WHERE id = " + id);
            
            preparedStatement = con.prepareStatement(queryBuilder.toString());
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet != null && resultSet.next())
            	status = Status.valueOf(resultSet.getString("status"));
            
            resultSet.close();
            preparedStatement.close();
        } catch (final SQLException sqlException) {
            sqlException.printStackTrace();
        }
        
        return status;
	}
	
	private static BlackItem addItem(ResultSet resultSet, UUID uuid) {
		BlackItem bItem = null;
		Integer id = null;
		try {
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
				
				id = resultSet.getInt("id");
				
				bItem = new BlackItem(item, resultSet.getDouble("value"), uuid, Status.valueOf(resultSet.getString("status")), new Date(resultSet.getLong("date")), id, resultSet.getBoolean("notified"));
				
				PlayerData.get(uuid).setItem(bItem);
			}
		}catch(Exception e) {
			Bukkit.getConsoleSender().sendMessage("§c[BlackMarket] §7» Corrupt item: ID#" + id);
			return null;
		}
		return bItem;
	}
}
