package dev.minecraftdorado.blackmarket.utils.inventory.utils;

import java.util.Locale;

public enum OrderType {
	ID, AMOUNT, VALUE, TYPE;

	public String getName() {
			return this.name().toLowerCase(new Locale("en", "US"));
		}
}