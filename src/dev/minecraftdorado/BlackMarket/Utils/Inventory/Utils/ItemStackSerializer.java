package dev.minecraftdorado.BlackMarket.Utils.Inventory.Utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import dev.minecraftdorado.BlackMarket.Utils.Packets.Reflections;

@SuppressWarnings("deprecation")
public class ItemStackSerializer {
	
	public static String serialize(ItemStack item){
        StringBuilder builder = new StringBuilder();
        builder.append(UMaterial.match(item).name());
        if(item.getDurability() != 0) builder.append(":" + item.getDurability());
        builder.append(" " + item.getAmount());
        String ench = getEnchantments(item);
        if(ench != null) builder.append(" ench:" + ench);
        String stored = getStored(item);
        if(stored != null) builder.append(" stored:" + stored);
        String name = getName(item);
        if(name != null) builder.append(" name:" + name);
        String lore = getLore(item);
        if(lore != null) builder.append(" lore:" + lore);
        Color color = getArmorColor(item);
        if(color != null) builder.append(" rgb:" + color.getRed() + "|" + color.getGreen() + "|" + color.getBlue());
        String owner = getOwner(item);
        if(owner != null) builder.append(" owner:" + owner);
        String flag = getFlags(item);
        if(flag != null) builder.append(" flag:" + flag);
        String potions = getPotions(item);
        if(potions != null) builder.append(" potion:" + potions);
        return builder.toString();
    }
    public static ItemStack deserialize(String serializedItem){
        String[] strings = serializedItem.split(" ");
        String[] args;
        ItemStack item = new ItemStack(Material.AIR);
        
        // Material
        for (String str: strings) {
            args = str.split(":");
            if(UMaterial.match(args[0]) != null && item.getType() == Material.AIR){
                item = UMaterial.match(args[0]).getItemStack();
                if(args.length == 2) item.setDurability(Short.parseShort(args[1]));
                break;
            }
        }
        if (item.getType() == Material.AIR) {
            Bukkit.getLogger().info("Could not find a valid material for the item in \"" + serializedItem + "\"");
            return null;
        }
        
        for(String str:strings){
            args = str.split(":", 2);
            
            // Amount
            
            if(isNumber(args[0])) item.setAmount(Integer.parseInt(args[0]));
            if(args.length == 1) continue;
            
            // Name
            
            if(args[0].equalsIgnoreCase("name")){
                setName(item, ChatColor.translateAlternateColorCodes('&', args[1]));
                continue;
            }
            
            // Lore
            
            if(args[0].equalsIgnoreCase("lore")){
                setLore(item, ChatColor.translateAlternateColorCodes('&', args[1]));
                continue;
            }
            
            // Color
            
            if(args[0].equalsIgnoreCase("rgb")){
                setArmorColor(item, args[1]);
                continue;
            }
            
            // Skull Owner
            
            if(args[0].equalsIgnoreCase("owner")){
                setOwner(item, args[1]);
                continue;
            }
            
            // Enchantment List
            
            if(args[0].equalsIgnoreCase("ench")) {
            	setEnchantments(item, args[1]);
            	continue;
            }
            
            // Stored Enchantment List
            
            if(args[0].equalsIgnoreCase("stored")) {
            	setStored(item, args[1]);
            	continue;
            }

            // Flags
            
            if(args[0].equalsIgnoreCase("flag")) {
            	setFlags(item, args[1]);
            	continue;
            }

            // Potions
            
            if(args[0].equalsIgnoreCase("potion")) {
            	setPotions(item, args[1]);
            	continue;
            }
        }
        return item.getType().equals(Material.AIR) ? null : item;
    }
    
    private static String getOwner(ItemStack item){
        if(!(item.getItemMeta() instanceof SkullMeta)) return null;
        return ((SkullMeta)item.getItemMeta()).getOwner();
    }
    
    private static void setOwner(ItemStack item, String owner){
        try{
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(owner);
            item.setItemMeta(meta);
        }catch(Exception exception){
            return;
        }
    }
    
    private static String getName(ItemStack item){
        if(!item.hasItemMeta()) return null;
        if(!item.getItemMeta().hasDisplayName()) return null;
        return item.getItemMeta().getDisplayName().replace(" ", "_").replace(ChatColor.COLOR_CHAR, '&');
    }
    
    private static void setName(ItemStack item, String name){
        name = name.replace("_", " ");
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }
    
    private static String getLore(ItemStack item){
        if(!item.hasItemMeta()) return null;
        if(!item.getItemMeta().hasLore()) return null;
        StringBuilder builder = new StringBuilder();
        List<String> lore = item.getItemMeta().getLore();
        for(int ind = 0;ind<lore.size();ind++){
            builder.append((ind > 0 ? "|" : "") + lore.get(ind).replace(" ", "_").replace(ChatColor.COLOR_CHAR, '&'));
        }
        return builder.toString();
    }
    
    private static void setLore(ItemStack item, String lore){
        lore = lore.replace("_", " ");
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Arrays.asList(lore.split("\\|")));
        item.setItemMeta(meta);
    }
    
    private static Color getArmorColor(ItemStack item){
        if(!(item.getItemMeta() instanceof LeatherArmorMeta)) return null;
        return ((LeatherArmorMeta)item.getItemMeta()).getColor();
    }
    
    private static void setArmorColor(ItemStack item, String str){
        try{
            String[] colors = str.split("\\|");
            int red = Integer.parseInt(colors[0]);
            int green = Integer.parseInt(colors[1]);
            int blue = Integer.parseInt(colors[2]);
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(Color.fromRGB(red, green, blue));
            item.setItemMeta(meta);
        }catch(Exception exception){
            return;
        }
    }
    
    private static String getEnchantments(ItemStack item) {
    	if(!item.hasItemMeta()) return null;
        if(!item.getItemMeta().hasEnchants()) return null;
        String ench = "";
        for(Enchantment e : item.getEnchantments().keySet()) {
        	ench = ench + "@" + e.getName() + "-" + item.getEnchantmentLevel(e);
		}
        if(ench.startsWith("@")) ench = ench.replaceFirst("@", "");
        if(ench == "") ench = null;
        return ench;
    }
    
    private static void setEnchantments(ItemStack item, String ench) {
    	ItemMeta meta = item.getItemMeta();
    	for(String s : ench.split("@")) {
    		String[] e = s.split("-");
    		Enchantment type = Enchantment.getByName(e[0]);
    		int level = Integer.valueOf(e[1]);
    		meta.addEnchant(type, level, true);
    	}
    	item.setItemMeta(meta);
    }
    
    private static String getStored(ItemStack item) {
    	if(!item.hasItemMeta()) return null;
    	if(!(item.getItemMeta() instanceof EnchantmentStorageMeta)) return null;
    	String ench = "";
    	EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
    	for(Enchantment e : meta.getStoredEnchants().keySet())
    		ench = ench + "@" + e.getName() + "-" + meta.getStoredEnchants().get(e);
        if(ench.startsWith("@")) ench = ench.replaceFirst("@", "");
        if(ench == "") ench = null;
        return ench;
    }
    
    private static void setStored(ItemStack item, String ench) {
    	EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
    	for(String s : ench.split("@")) {
    		String[] e = s.split("-");
    		Enchantment type = Enchantment.getByName(e[0]);
    		int level = Integer.valueOf(e[1]);
    		meta.addStoredEnchant(type, level, true);
    	}
    	item.setItemMeta(meta);
    }
    
	private static String getFlags(ItemStack item) {
    	if(!item.hasItemMeta()) return null;
        String flag = "";
        for(ItemFlag f : item.getItemMeta().getItemFlags())
			flag = flag + "@" + f.name();
		if(flag.startsWith("@")) flag = flag.replaceFirst("@", "");
		if(flag == "") flag = null;
        return flag;
    }
    
    private static void setFlags(ItemStack item, String flags) {
    	ItemMeta meta = item.getItemMeta();
    	for(String f : flags.split("@"))
    		meta.addItemFlags(ItemFlag.valueOf(f));
    	item.setItemMeta(meta);
    }
    
	private static String getPotions(ItemStack item) {
    	if(!item.hasItemMeta()) return null;
    	if(!(item.getItemMeta() instanceof PotionMeta)) return null;
        String potion = "";
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        
        if(Reflections.existMethod(meta.getClass().toString(), "getBasePotionData")) {
        	try {
        		Method getBasePotionData = meta.getClass().getMethod("getBasePotionData");
        		PotionData pd = (PotionData) getBasePotionData.invoke(meta);
        		potion = pd.getType().name() + "-" + pd.isUpgraded() + "-" + pd.isExtended();
        	} catch(Exception e) {
        		e.printStackTrace();
        	}
        }
        
        if(meta.hasCustomEffects()) {
        	for(PotionEffect p : meta.getCustomEffects())
        		potion = potion + "@" + p.getType().getName() + "-" + p.getAmplifier() + "-" + p.getDuration();
        	if(potion.startsWith("@")) potion = potion.replaceFirst("@", "");
        }
        
        /*
        if(meta.getBasePotionData() != null) {
        	PotionData pd = meta.getBasePotionData();
        	potion = pd.getType().name() + "-" + pd.isUpgraded() + "-" + pd.isExtended();
        }
        
        */
		if(potion == "") potion = null;
        return potion;
    }
    
    private static void setPotions(ItemStack item, String potions) {
    	PotionMeta meta = (PotionMeta) item.getItemMeta();
    	String[] p = potions.split("@");
    	
    	for (int i = 0; i < p.length; i++) {
    		String[] args = p[i].split("-");
    		
			if(i == 0) {
				try {
					if(Reflections.existMethod(meta.getClass().toString(), "setBasePotionData", PotionData.class)) {
						try {
							PotionType type = PotionType.valueOf(args[0]);
							Method setBasePotionData = meta.getClass().getMethod("setBasePotionData", PotionData.class);
							setBasePotionData.invoke(meta, new PotionData(type, Boolean.valueOf(args[2]), Boolean.valueOf(args[1])));
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}catch(Exception ex) {
					Bukkit.getConsoleSender().sendMessage("§6" + potions);
					ex.printStackTrace();
				}
				
				continue;
			}
			PotionEffectType type = PotionEffectType.getByName(args[0]);
			int amplifier = Integer.valueOf(args[1]);
			int duration = Integer.valueOf(args[2]);
			meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
		}
    	item.setItemMeta(meta);
    }
    
    private static boolean isNumber(String str){
        try{
            Integer.parseInt(str);
        }catch(NumberFormatException exception){
            return false;
        }
        return true;
    }
}