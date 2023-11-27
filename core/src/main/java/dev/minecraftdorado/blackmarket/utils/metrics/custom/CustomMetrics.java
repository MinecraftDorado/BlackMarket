package dev.minecraftdorado.blackmarket.utils.metrics.custom;

import java.util.concurrent.Callable;

import dev.minecraftdorado.blackmarket.utils.Config;
import dev.minecraftdorado.blackmarket.utils.metrics.Metrics;

public class CustomMetrics {
	
	private Metrics metrics;
	
	public CustomMetrics(Metrics metrics) {
		this.metrics = metrics;
		
		Langs();
	}
	
	public void Langs() {
		metrics.addCustomChart(new Metrics.SimplePie("language", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	            return Config.getLangFile().getName().replace(".yml", "");
	        }
	    }));
	}
}
