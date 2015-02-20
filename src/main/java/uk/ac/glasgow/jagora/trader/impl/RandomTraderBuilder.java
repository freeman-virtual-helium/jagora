package uk.ac.glasgow.jagora.trader.impl;

import java.util.HashMap;
import java.util.Map;

import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.trader.impl.RandomTrader.StockData;
import uk.ac.glasgow.jagora.util.Random;

public class RandomTraderBuilder {
	private String name;
	private Double cash;
	
	private Map<Stock, Integer> inventory;
	
	private Integer seed;
	private Map<Stock, StockData> stockDatas;
	
	public RandomTraderBuilder(String name, Double cash, Integer seed){
		this.name = name;
		this.cash = cash;
		this.inventory = new HashMap<Stock,Integer>();
		this.seed = seed;
		stockDatas = new HashMap<Stock,StockData>();
	}
	
	public RandomTraderBuilder addStock(Stock stock, Integer quantity){
		inventory.put(stock, quantity);
		return this;
	}
	
	public RandomTraderBuilder addTradeRange(
		Stock stock, Double low, Double high, Integer minQuantity, Integer maxQuantity){
		
		stockDatas.put(stock, new StockData(stock, low, high, minQuantity, maxQuantity));
		return this;
	}
	
	public RandomTraderBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public RandomTraderBuilder setCash(Double cash){
		this.cash = cash;
		return this;
	}
	
	public RandomTrader build(){
		return new RandomTrader(name, cash, inventory, new Random(seed), stockDatas);
	}
}
