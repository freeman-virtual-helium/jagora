package uk.ac.glasgow.jagora.trader.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalDouble;

import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.StockExchangeLevel1View;
import uk.ac.glasgow.jagora.impl.LimitBuyOrder;
import uk.ac.glasgow.jagora.impl.LimitSellOrder;
import uk.ac.glasgow.jagora.ticker.TradeExecutionEvent;
import uk.ac.glasgow.jagora.ticker.TradeListener;
import uk.ac.glasgow.jagora.trader.Level1Trader;
import uk.ac.glasgow.jagora.util.Random;

/**
 * A trader that places a bid if the current best offer is below the historic
 * trading price; or places an offer if the current best bid is below the
 * historic trading price. The historic average is calculated from all executed
 * trades, without time limit.
 * 
 * @author tws
 *
 */
public class SimpleHistoricTrader extends SafeAbstractTrader implements Level1Trader, TradeListener {
	
	private Collection<TradeExecutionEvent> tradeExecutionEvents;

	private Random random;
	
	public SimpleHistoricTrader(
		String name, Long cash, Map<Stock, Integer> inventory, Random random) {
		
		super(name, cash, inventory);
		this.random = random;
		tradeExecutionEvents = new ArrayList<TradeExecutionEvent>();
	}

	@Override
	public void tradeExecuted(TradeExecutionEvent tradeExecutionEvent) {
		synchronized(tradeExecutionEvents){
			tradeExecutionEvents.add(tradeExecutionEvent);
		}
	}

	@Override
	public void speak(StockExchangeLevel1View traderMarketView) {
		Stock randomStock = random.chooseElement(inventory.keySet());
		
		Long averageTradePrice = 
			computeAverageTradePrice(randomStock);
		if (averageTradePrice == null) return;
		
		Long bestBidPrice =
			traderMarketView.getBestBidPrice(randomStock);
		
		Long bestOfferPrice = 
			traderMarketView.getBestOfferPrice(randomStock);
				
		if (bestOfferPrice != null && bestOfferPrice < averageTradePrice)
			placeBuyOrder(traderMarketView, randomStock, bestOfferPrice);
		else if (bestBidPrice != null && bestBidPrice > averageTradePrice)
			placeSellOrder(traderMarketView, randomStock, bestBidPrice);		
	}

	private void placeBuyOrder(
		StockExchangeLevel1View traderMarketView, Stock randomStock, Long bestOfferPrice) {
		
		Integer quantity = computeAverageQuantity(randomStock);
		
		LimitBuyOrder limitBuyOrder = 
			new LimitBuyOrder(this, randomStock, quantity, bestOfferPrice);
		placeSafeBuyOrder(traderMarketView, limitBuyOrder);
	}

	private void placeSellOrder(
		StockExchangeLevel1View traderMarketView, Stock randomStock, Long bestOfferPrice) {
		
		Integer quantity = computeAverageQuantity(randomStock);
		
		LimitSellOrder limitSellOrder = 
			new LimitSellOrder(this, randomStock, quantity, bestOfferPrice);
		placeSafeSellOrder(traderMarketView, limitSellOrder);
	}

	
	private Long computeAverageTradePrice(Stock randomStock) {
		// TODO Recalculating the average like this each time isn't very efficient.  
		// It would be better to maintain a running average.
		
		synchronized (tradeExecutionEvents){
			OptionalDouble average = 
				tradeExecutionEvents.stream()
				.filter(tradeExecutionEvent -> tradeExecutionEvent.stock == randomStock)
				.mapToLong(tradeExecutionEvent -> tradeExecutionEvent.price)
				.average();
			return (average.isPresent()? (long)average.getAsDouble() : null);
		}
	}
	
	private Integer computeAverageQuantity(Stock stock) {
		// TODO Recalculating the average like this each time isn't very efficient.  
		// It would be better to maintain a running average.
		
		synchronized (tradeExecutionEvents){
			OptionalDouble average = 
				tradeExecutionEvents.stream()
				.filter(tradeExecutionEvent -> tradeExecutionEvent.stock == stock)
				.mapToInt(tradeExecutionEvent -> tradeExecutionEvent.quantity)
				.average();
			return average.isPresent()? (int)average.getAsDouble() : null;
		}
	}
}