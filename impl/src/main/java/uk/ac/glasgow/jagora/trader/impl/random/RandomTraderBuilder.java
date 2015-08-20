package uk.ac.glasgow.jagora.trader.impl.random;

import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.trader.impl.AbstractTraderBuilder;
import uk.ac.glasgow.jagora.util.Random;

import java.util.HashMap;
import java.util.Map;

public class RandomTraderBuilder extends AbstractTraderBuilder {
	protected String name;
	protected Long cash;
	
	protected Map<Stock, Integer> inventory;
	
	protected Integer seed;
	protected Map<Stock, RangeData> sellRangeData;
	protected Map<Stock, RangeData> buyRangeData;
	
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

	public RandomTraderBuilder setSellOrderRange(
		Stock stock, Integer minQuantity, Integer maxQuantity,
		Long sellLow, Long sellHigh) {
				
		sellRangeData.put(stock, new RangeData(stock, sellLow, sellHigh, minQuantity, maxQuantity));
		return this;
	}
	
	public RandomTraderBuilder setBuyOrderRange(
		Stock stock, Integer minQuantity, Integer maxQuantity, Long buyLow, Long buyHigh){
		
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