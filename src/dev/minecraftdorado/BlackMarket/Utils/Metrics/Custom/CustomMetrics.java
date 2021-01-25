package dev.minecraftdorado.BlackMarket.Utils.Metrics.Custom;

import java.util.concurrent.Callable;

import dev.minecraftdorado.BlackMarket.Utils.Config;
import dev.minecraftdorado.BlackMarket.Utils.Metrics.Metrics;

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
