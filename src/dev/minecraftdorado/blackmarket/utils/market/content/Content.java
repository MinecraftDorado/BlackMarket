package dev.minecraftdorado.blackmarket.utils.market.content;

import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.inventory.InventoryManager.Inv;
import dev.minecraftdorado.blackmarket.utils.market.BlackItem;

public class Content {
	
	public static String getTitle() {
		return Config.getMessage("menus.content.title");
	}
	
	public static Inv getInventory(Player player, BlackItem bItem) {
		Inv inv = new Inv(getTitle(), 6);
		
		inv.setBackgroud(Config.getContentMenuBorder(), true);
		
		if(bItem.getOriginal().getType().name().contains("SHULKER_BOX"))
			if(bItem.getOriginal().getItemMeta() instanceof BlockStateMeta) {
				BlockStateMeta bMeta = (BlockStateMeta) bItem.getOriginal().getItemMeta();
				if(bMeta.getBlockState() instanceof ShulkerBox) {
					ShulkerBox shulker = (ShulkerBox) bMeta.getBlockState();
					
					ItemStack[] l = shulker.getInventory().getContents();
					
					int slot = 9;
					int items = 0;
					
					for (int i = 0; i < 27; i++) {
						if(l.length <= i)
							break;
						if(items%7 == 0 && items != 0)
							slot = slot + 3;
						else
							slot++;
						inv.setItem(slot, l[i]);
						items++;
					}
				}
			}
		
		inv.addBlackItem(bItem, 48);
		inv.setItem(48, bItem.getItemStack(player, false));
		inv.setItem(Config.getSlot("content.back"), Config.getItemStack("content.back", "menus.content.items.back"));
		return inv;
	}
}
