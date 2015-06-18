package uk.ac.glasgow.jagora.trader.impl;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.Trade;
import uk.ac.glasgow.jagora.TradeExecutionException;
import uk.ac.glasgow.jagora.trader.Trader;

/**
 * Implements basic trader functionality. Sub-classes implement trader specific
 * strategies in the speak () operation.
 * 
 * @author tws
 *
 */
public abstract class AbstractTrader implements Trader {
	
	/**
	 * A unique identifier for the trader.
	 */
	private final String name;

	private Long cash; 
	protected final Map<Stock,Integer> inventory;
	
	private List<Trade> mySellTrades;
	private List<Trade> myBuyTrades;

	public AbstractTrader(String name, Long cash, Map<Stock,Integer> inventory) {
		this.name = name;
		this.cash = cash;
		this.inventory = new HashMap<Stock,Integer>(inventory);
		this.mySellTrades = new ArrayList<Trade>();
		this.myBuyTrades = new ArrayList<Trade>();
	}
	
	public String getName (){
		return name;
	}
	
	/**
	 * @see uk.ac.glasgow.jagora.trader.Trader#getCash()
	 */
	@Override
	public Long getCash (){
		return cash;
	}
	
	/**
	 * @see uk.ac.glasgow.jagora.trader.Trader#getInventory(Stock)
	 */
	@Override
	public Integer getInventory(Stock stock) {
		return inventory.getOrDefault(stock, 0);
	}

	@Override
	public String toString (){
		//return format("trader[%s:$%.2f:%s]",name, cash, inventory);
		return name;
	}

	/**
	 * @see uk.ac.glasgow.jagora.trader.Trader#sellStock(uk.ac.glasgow.jagora.Trade)
	 */
	@Override
	public void sellStock(Trade trade) throws TradeExecutionException {
		Integer currentQuantity = inventory.getOrDefault(trade.getStock(), 0);
		if (currentQuantity < trade.getQuantity()){ 
			String message = format("Seller [%s] cannot satisfy trade [%s] because remaining quantity is [%d].", name, trade, currentQuantity);
			throw new TradeExecutionException (message, trade, this);
		} else {
			inventory.put(trade.getStock(), currentQuantity - trade.getQuantity());
			cash += trade.getPrice() * trade.getQuantity();
			mySellTrades.add(trade);
		}
	}
	
	/**
	 * @see uk.ac.glasgow.jagora.trader.Trader#buyStock(uk.ac.glasgow.jagora.Trade)
	 */
	@Override
	public void buyStock(Trade trade) throws TradeExecutionException {
		Long totalPrice = trade.getPrice() * trade.getQuantity();
		
		if (totalPrice > cash){
			String message = format("Buyer [%s] cannot satisfy trade [%s] with remaining cash [%d].", name, trade, getCash());
			throw new TradeExecutionException (message, trade, this);		
		} else {
			cash -= totalPrice;
			Integer currentQuantity = inventory.getOrDefault(trade.getStock(), 0);
			inventory.put(trade.getStock(), currentQuantity + trade.getQuantity());
			myBuyTrades.add(trade);
		}
	}
	
}