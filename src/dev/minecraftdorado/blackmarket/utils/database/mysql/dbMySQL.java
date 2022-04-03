package dev.minecraftdorado.blackmarket.utils.database.mysql;

import dev.minecraftdorado.blackmarket.mainclass.MainClass;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.ItemStackSerializer;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.SerializeInventory;
import dev.minecraftdorado.blackmarket.utils.inventory.utils.UMaterial;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem.Status;
import dev.minecraftdorado.blackmarket.utils.market.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.UUID;

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
		sql.executeScript("/resources/sql/blackmarket.sql");
	}

	public static ArrayList<BlackItem> loadBlackItems(String category, String orderType, boolean invert, int offset) {
		ArrayList<BlackItem> list = new ArrayList<>();

		con = sql.getConnection();

		try {
			String query = "call bm_load_items(null, 'ON_SALE', '" + category + "', '" + orderType + "', " + invert + ", 25," + offset + ")";

			PreparedStatement preparedStatement = con.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet != null && resultSet.next()) {
				list.add(getItem(resultSet));
			}

			resultSet.close();
			preparedStatement.close();
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return list;
	}

	public static int loadMarketSize(String category, UUID uuid) {
		int size = 0;

		con = sql.getConnection();

		try {
			String query = "call bm_load_items_size('" + uuid.toString() + "', 'ON_SALE', '" + category + "')";

			PreparedStatement preparedStatement = con.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			size = resultSet.getInt("size");

			resultSet.close();
			preparedStatement.close();
		} catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return size;
	}
	
	public static void updateStatus(BlackItem bItem) {
		con = sql.getConnection();
        
        try {
        	PreparedStatement preparedStatement = null;
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE blackmarket_items SET status_id = ? WHERE id = ?");
			
			preparedStatement = con.prepareStatement(queryBuilder.toString());
			preparedStatement.setInt(1, bItem.status.getId());
			preparedStatement.setInt(2, bItem.getId());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateNotified(BlackItem bItem) {
		con = sql.getConnection();
        
        try {
        	PreparedStatement preparedStatement = null;
			StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("UPDATE blackmarket_items SET notified = ? WHERE id = ?");
			
			preparedStatement = con.prepareStatement(queryBuilder.toString());
			preparedStatement.setBoolean(1, bItem.isNotified());
			preparedStatement.setInt(2, bItem.getId());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean sellBlackItem(BlackItem bItem) {
		con = sql.getConnection();
        
        try {
        	PreparedStatement preparedStatement = null;
			StringBuilder queryBuilder = new StringBuilder();

			queryBuilder.append("call bm_sell_item(?,?,?,?,?,?)");
			
			preparedStatement = con.prepareStatement(queryBuilder.toString());
			preparedStatement.setString(1, bItem.getOwner().toString());
			preparedStatement.setDouble(2, bItem.getValue());
			preparedStatement.setString(3, ItemStackSerializer.serialize(bItem.getOriginal()));

			Object content = null;
			if(bItem.getOriginal().getType().name().contains("SHULKER_BOX")){
				if(bItem.getOriginal().getItemMeta() instanceof BlockStateMeta) {
					BlockStateMeta meta = (BlockStateMeta) bItem.getOriginal().getItemMeta();
					if(meta.getBlockState() instanceof ShulkerBox) {
						ShulkerBox shulker = (ShulkerBox) meta.getBlockState();
						content = SerializeInventory.itemStackArrayToBase64(shulker.getInventory().getContents());
					}
				}
			}
			preparedStatement.setObject(4, content);

			preparedStatement.setTimestamp(5, bItem.getExpirationDate());
			preparedStatement.setInt(6, PlayerData.get(bItem.getOwner()).getLimit());
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			// ignore post limit exception
			if(!"45000".equals(e.getSQLState())) {
				e.printStackTrace();
			}

			return false;
		}
		return true;
	}
	
	public static void checkUnnotified(UUID uuid) {
		if(!Bukkit.getOfflinePlayer(uuid).isOnline())
			return;

		con = sql.getConnection();
		
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
			String query = "call bm_load_items('" + uuid.toString() + "', 'SOLD', null, null, false, 10000,0)";

            preparedStatement = con.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet != null && resultSet.next()) {
            	BlackItem bItem = getItem(resultSet);
            	if(bItem != null)
            		bItem.sendNotification();
            }

            resultSet.close();
            preparedStatement.close();
        } catch (final SQLException sqlException) {
            sqlException.printStackTrace();
		}
	}
	
	public static ArrayList<BlackItem> loadStorage(UUID uuid, int page) {
		ArrayList<BlackItem> list = new ArrayList<>();

		con = sql.getConnection();
        
        try {
			// 29 to know if has next page
			String query = "call bm_load_items('" + uuid.toString() + "', 'TIME_OUT', null, null, false, 29," + (page * 28) + ")";

			PreparedStatement preparedStatement = con.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet != null && resultSet.next()) {
				list.add(getItem(resultSet));
			}

            resultSet.close();
            preparedStatement.close();
        } catch (final SQLException sqlException) {
            sqlException.printStackTrace();
		}
		return list;
	}

	public static int loadStorageSize(UUID uuid) {
		int size = 0;

		con = sql.getConnection();

        try {
			String query = "call bm_load_items_size('" + uuid.toString() + "', 'TIME_OUT', 'all')";

			PreparedStatement preparedStatement = con.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			size = resultSet.getInt("size");

            resultSet.close();
            preparedStatement.close();
        } catch (final SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return size;
	}

	private static BlackItem getItem(ResultSet resultSet) {
		BlackItem bItem = null;
		try {
			ItemStack item = ItemStackSerializer.deserialize(resultSet.getString("item_data"));

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

				bItem = new BlackItem(item, resultSet.getDouble("price"), UUID.fromString(resultSet.getString("user")), Status.valueOf(resultSet.getString("status")), resultSet.getTimestamp("expiration_date"), resultSet.getInt("item_id"), false);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return bItem;
	}

	public static void deleteCategories() {
		con = sql.getConnection();

		try {
			String query = "delete from blackmarket_category";

			PreparedStatement preparedStatement = con.prepareStatement(query);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void loadCategory(String categoryName, ArrayList<UMaterial> materials) {
		if(materials == null || materials.isEmpty()) {return;}

		con = sql.getConnection();
		
		try {
			StringJoiner values = new StringJoiner(",");

			for(UMaterial material : materials){
				values.add("('" + categoryName + "', '" + material.name() + "')");
			}

			String query = "insert into blackmarket_category(category, materials) values " + values + ";";

			PreparedStatement preparedStatement = con.prepareStatement(query);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
