package uk.ac.glasgow.jagora.ticker.impl;

import static uk.ac.glasgow.jagora.ticker.OrderEntryEvent.OrderDirection.BUY;
import static uk.ac.glasgow.jagora.ticker.OrderEntryEvent.OrderDirection.SELL;

import java.util.*;

import uk.ac.glasgow.jagora.Order;
import uk.ac.glasgow.jagora.SellOrder;
import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.Trade;
import uk.ac.glasgow.jagora.ticker.*;
import uk.ac.glasgow.jagora.ticker.OrderEntryEvent.OrderDirection;
import uk.ac.glasgow.jagora.world.TickEvent;

public abstract class AbstractStockExchangeObservable implements StockExchangeObservable {

	private final Set<TradeListener> tradeListeners;
	private final Set<OrderListener> orderListeners;

	private final Map<Stock, PriorityQueue<BuyTradePriceListener>> buyPriceListeners;
	private final Map<Stock, PriorityQueue<SellTradePriceListener>> sellPriceListeners;

	private final List<TickEvent<Trade>> executedTrades;

	public AbstractStockExchangeObservable() {
		tradeListeners = new HashSet<TradeListener>();
		orderListeners = new HashSet<OrderListener>();

		buyPriceListeners = new HashMap<Stock, PriorityQueue<BuyTradePriceListener>>();
		sellPriceListeners = new HashMap<Stock, PriorityQueue<SellTradePriceListener>>();

		executedTrades = new ArrayList<TickEvent<Trade>>();
	}
	
	public List<TickEvent<Trade>> getTradeHistory(Stock stock) {
		
		List<TickEvent<Trade>> result = new ArrayList<TickEvent<Trade>>();
		
		executedTrades
			.stream()
			.filter(executedTrade -> executedTrade.event.getStock().equals(stock))
			.forEach(executedTrade -> result.add(executedTrade));

		return result;
	}


	@Override
	public void registerTradeListener(TradeListener tradeListener) {
		tradeListeners.add(tradeListener);
	}

	@Override
	public void notifyTradeListeners(List<TickEvent<Trade>> newlyExecutedTrades) {
		executedTrades.addAll(newlyExecutedTrades);
		
		for (TickEvent<Trade> executedTrade: newlyExecutedTrades)
			notifyTradeListenersOfTrade(executedTrade);
	}

	private void notifyTradeListenersOfTrade(TickEvent<Trade> executedTrade) {
		TradeExecutionEvent tradeExecutedEvent = 
			new TradeExecutionEvent(
				executedTrade.event.getStock(),
				executedTrade.event.getBuyer(),
				executedTrade.event.getSeller(),
				executedTrade.tick,
				executedTrade.event.getPrice(),
				executedTrade.event.getQuantity());
		
		List<TradeListener> randomisedTickerTapeListeners =
			getRandomisedTicketTapeListeners();
		
		for (TradeListener tradeListener: randomisedTickerTapeListeners)
			notifyTradeListenerOfTrade(tradeExecutedEvent, tradeListener);

		//notify the price listeners
		notifyPriceTradeListeners(executedTrade.event.getStock(), executedTrade.event.getPrice());
	}

	private void notifyPriceTradeListeners(Stock stock, Long price) {

		PriorityQueue queue = buyPriceListeners.get(stock);

		if (queue != null && queue.size() != 0){

			TradePriceListener listener = buyPriceListeners.get(stock).peek();
			if (listener == null) return;
			//check if one of the buyListeners is activated
			if (listener.getPrice() <= price){
				while (listener != null && listener.getPrice() <= price ) {
					listener.priceReached(); //execute the order on the market
					buyPriceListeners.get(stock).poll();
					listener = buyPriceListeners.get(stock).peek();
				}
			}
		}
		else {

			queue = sellPriceListeners.get(stock);
			if (queue == null || queue.size() == 0) return;
			//else check if one of the sellPriceListeners is activated
			TradePriceListener listener = sellPriceListeners.get(stock).peek();
			if (listener.getPrice() >= price){
				while (listener != null && listener.getPrice() >= price ) {
					listener.priceReached(); //execute the order on the market
					sellPriceListeners.get(stock).poll();
					listener = sellPriceListeners.get(stock).peek();
				}
			}
		}
		//currently the method doesn't support interfering PriceListeners(sell stop price higher than buy stop)
	}

	@Override
	public void registerPriceListener(PriceListener tradePriceListener) {
		//Maybe change implementation so that we don't use instanceof

		if (tradePriceListener instanceof BuyTradePriceListener)
			registerPriceTradeListener(buyPriceListeners, (TradePriceListener) tradePriceListener);

		else if (tradePriceListener instanceof SellTradePriceListener)
			registerPriceTradeListener(sellPriceListeners, (TradePriceListener) tradePriceListener);

	}

	private void registerPriceTradeListener  (Map map,TradePriceListener tradePriceListener){
		PriorityQueue queue =
				(PriorityQueue) map.get(( tradePriceListener).getStock());

		if (queue == null) {
			queue = new PriorityQueue<TradePriceListener>();
		}

		queue.add(tradePriceListener);
		map.put(tradePriceListener.getStock(), queue);
	}

	/**
	 * Left for implementation in child classes.
	 * @param tradeExecutedEvent
	 * @param tradeListener
	 */
	protected abstract void notifyTradeListenerOfTrade(
		TradeExecutionEvent tradeExecutedEvent, TradeListener tradeListener);


	private List<TradeListener> getRandomisedTicketTapeListeners() {
		List<TradeListener> randomisedTickerTapeListeners =
			new ArrayList<TradeListener>(tradeListeners);

		Collections.shuffle(randomisedTickerTapeListeners);
		return randomisedTickerTapeListeners;
	}
	
	@Override
	public void registerOrderListener(OrderListener orderListener) {
		orderListeners.add(orderListener);
	}
	
	@Override
	public void notifyOrderListeners(TickEvent<? extends Order> orderEvent){
		List<OrderListener> randomisedOrderListeners = 
			new ArrayList<OrderListener>(orderListeners);
		
		Collections.shuffle(randomisedOrderListeners);//why don't you just call getRandomisedTicketTapeListeners?
		
		Order event = orderEvent.event;
		
		OrderDirection direction = event instanceof SellOrder ? SELL : BUY;
		
		OrderEntryEvent orderEntryEvent = 
			new OrderEntryEvent(
				orderEvent.tick,
				event.getTrader(), 
				event.getStock(), 
				event.getRemainingQuantity(),
				event.getPrice(), 
				direction);
		
		for (OrderListener orderListener : randomisedOrderListeners)
			notifyOrderListenerOfOrder(orderEntryEvent, orderListener);
	}

    /**
     * Left for implementation in child classes.
     * @param orderEntryEvent
     * @param orderListener
     */
	public abstract void notifyOrderListenerOfOrder(
		OrderEntryEvent orderEntryEvent, OrderListener orderListener);
	
}