package uk.ac.glasgow.jagora.trader.impl;

import java.util.HashMap;
import java.util.Map;

import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.trader.impl.RandomTrader.RangeData;
import uk.ac.glasgow.jagora.util.Random;

public class RandomTraderBuilder extends AbstractTraderBuilder {
	private String name;
	private Long cash;
	
	private Map<Stock, Integer> inventory;
	
	private Integer seed;
	private Map<Stock, RangeData> sellRangeData;
	private Map<Stock, RangeData> buyRangeData;
	
	public RandomTraderBuilder(){
		this.inventory = new HashMap<Stock,Integer>();
		sellRangeData = new HashMap<Stock,RangeData>();
		buyRangeData = new HashMap<Stock,RangeData>();

	}
	@Override
	public RandomTraderBuilder addStock(Stock stock, Integer quantity){
		inventory.put(stock, quantity);
		return this;
	}

	/**
	 * setTradeRange of a stock
	 * @param stock
	 * @param minQuantity
	 * @param maxQuantity
	 * @param sellLow needs to be a negative (meaning low end of the margin)
	 * @param sellHigh positive
	 * @param buyLow negative
	 * @param buyHigh positive
	 * @return
	 */
	public RandomTraderBuilder setTradeRange(
		Stock stock, Integer minQuantity, Integer maxQuantity,
		Long sellLow, Long sellHigh, Long buyLow, Long buyHigh){
		
		sellRangeData.put(stock, new RangeData(stock, sellLow, sellHigh, minQuantity, maxQuantity));
		buyRangeData.put(stock, new RangeData(stock, buyLow, buyHigh, minQuantity, maxQuantity));
		return this;
	}
	@Override
	public RandomTraderBuilder setName(String name) {
		this.name = name;
		return this;
	}
	@Override
	public RandomTraderBuilder setCash(Long cash){
		this.cash = cash;
		return this;
	}
	
	public RandomTrader build(){
		return new RandomTrader(name, cash, inventory, new Random(seed), sellRangeData, buyRangeData);
	}

	public RandomTraderBuilder setSeed(Integer seed) {
		this.seed = seed;
		return this;
	}
}
