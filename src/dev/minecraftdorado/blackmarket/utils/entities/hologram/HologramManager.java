package dev.minecraftdorado.blackmarket.utils.entities.hologram;

import java.util.ArrayList;

public class HologramManager {
	
	public ArrayList<Hologram> list = new ArrayList<>();
	
	public void add(Hologram holo) {
		list.add(holo);
	}
	
	public void remove(Hologram holo) {
		list.remove(holo);
	}
}
