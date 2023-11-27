package dev.minecraftdorado.blackmarket.utils.entities.hologram;

import java.util.ArrayList;

public class HologramManager {
	
	public ArrayList<HologramAbs> list = new ArrayList<>();
	
	public void add(HologramAbs holo) {
		list.add(holo);
	}
	
	public void remove(HologramAbs holo) {
		list.remove(holo);
	}
}
