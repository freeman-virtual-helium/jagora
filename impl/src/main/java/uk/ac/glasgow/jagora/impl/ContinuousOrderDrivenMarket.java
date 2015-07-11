package uk.ac.glasgow.jagora.impl;

import java.util.ArrayList;
import java.util.List;

import uk.ac.glasgow.jagora.*;
import uk.ac.glasgow.jagora.pricer.TradePricer;
import uk.ac.glasgow.jagora.trader.Trader;
import uk.ac.glasgow.jagora.world.TickEvent;
import uk.ac.glasgow.jagora.world.World;
import uk.ac.glasgow.jagora.StockWarehouse;

/**
 * @author tws
 *
 */
public class ContinuousOrderDrivenMarket implements Market {


	public final Stock stock;
	public final World world;

	private final OrderBook<SellOrder> sellBook;
	private final OrderBook<BuyOrder> buyBook;
	
	private final TradePricer tradePricer;

	private final StockWarehouse stockWarehouse;

	private List <TickEvent<Order>> marketSellOrders = new ArrayList<>();
	private List <TickEvent <Order>> marketBuyOrders = new ArrayList<>();

	private Long lastUsedPrice;

	public ContinuousOrderDrivenMarket (StockWarehouse stockWarehouse, World world, TradePricer tradePricer){
		this.world = world;
		this.tradePricer = tradePricer;

		sellBook = new OrderBook<SellOrder>(world);
		buyBook = new OrderBook<BuyOrder>(world);

		lastUsedPrice = 0l;//very arbitrary

		this.stockWarehouse = stockWarehouse;
		this.stock = stockWarehouse.getStock();
	}

	@Override
	public Integer getStock(Integer quantity) throws Exception{
		return stockWarehouse.getStock(quantity);
	}

	@Override
	public Integer getRemainingStock() {
		return stockWarehouse.getRemainingStock();
	}

	@Override
	public Integer getTotalStockQuantity() {
		return stockWarehouse.getInitialQuantity();
	}

	@Override
	public TickEvent<BuyOrder> recordBuyOrder(BuyOrder order) {
		TickEvent <BuyOrder> toReturn;

		if (order instanceof  MarketBuyOrder)
			toReturn = recordMarketOrder(marketBuyOrders,order);
		else toReturn = buyBook.recordOrder(order);

		return toReturn;
	}
	
	@Override
	public TickEvent<SellOrder> recordSellOrder(SellOrder order) {
		TickEvent<SellOrder> toReturn;

		if (order instanceof MarketSellOrder)
			toReturn = recordMarketOrder(marketSellOrders,order);
		else
			toReturn = sellBook.recordOrder(order);

		return toReturn ;
	}
	
	@Override
	public void cancelBuyOrder(BuyOrder order) {
		buyBook.cancelOrder(order);
	}
	
	@Override
	public void cancelSellOrder(SellOrder order) {
		sellBook.cancelOrder(order);
	}


	/**
	 * The operation executes trades,
	 * if the lowest offer is lower than the highest bid.
     * There is a possibility of failed offer if one of the sides
     * cancels its order.
	 */
	@Override
	public List<TickEvent<Trade>> doClearing (){
		
		List<TickEvent<Trade>> executedTrades =
			new ArrayList<TickEvent<Trade>>();

		TickEvent<BuyOrder> highestBuyEvent = null;
		TickEvent<SellOrder> lowestSellEvent = getBestMarketEvent(marketSellOrders);
		if (lowestSellEvent == null) {
			lowestSellEvent = sellBook.getBestOrder();
			highestBuyEvent = getBestMarketEvent(marketBuyOrders);
		}
		if (highestBuyEvent == null) highestBuyEvent = buyBook.getBestOrder();


		while (aTradeCanBeExecuted(lowestSellEvent, highestBuyEvent)){
			SellOrder lowestSell = lowestSellEvent.event;
			BuyOrder highestBid = highestBuyEvent.event;
			Integer quantity = 
				Math.min(
					lowestSell.getRemainingQuantity(), 
					highestBid.getRemainingQuantity()
				);
			
			Long price = tradePricer.priceTrade(highestBuyEvent, lowestSellEvent);
			
			Trade trade = 
				new DefaultTrade (stock, quantity, price, lowestSell, highestBid);
			
			try {
				TickEvent<Trade> executedTrade = trade.execute(world);
				executedTrades.add(executedTrade);
				lastUsedPrice = price;
											
			} catch (TradeExecutionException e) {
				Trader culprit = e.getCulprit();
				if (culprit.equals(lowestSell.getTrader())){
					sellBook.cancelOrder(lowestSell);
				}
				else if (culprit.equals(highestBid.getTrader()))
					buyBook.cancelOrder(highestBid);
				
				//TODO Penalise the trader that caused the trade to fail.
				e.printStackTrace();
				System.out.println("Failed order " + highestBid);
				System.exit(0);	
			}

			highestBuyEvent = null;
			lowestSellEvent = getBestMarketEvent(marketSellOrders);
			if (lowestSellEvent == null) {
				lowestSellEvent = sellBook.getBestOrder();
				highestBuyEvent = getBestMarketEvent(marketBuyOrders);
			}
			if (highestBuyEvent == null) highestBuyEvent = buyBook.getBestOrder();
			
		}
		return executedTrades;
	}

	private boolean aTradeCanBeExecuted(TickEvent<SellOrder> lowestSell, TickEvent<BuyOrder> highestBuy) {
		return 
			lowestSell != null &&
			highestBuy != null &&
			highestBuy.event.getPrice() >= lowestSell.event.getPrice();
	}
	
	@Override
	public List<BuyOrder> getBuyOrders() {
		return buyBook.getOpenOrders();
	}

	@Override
	public List<SellOrder> getSellOrders() {
		return sellBook.getOpenOrders();
	}

	@Override
	public String toString() {
		return String.format("best bid: %d, best offer: %d", getBestBidPrice(), getBestOfferPrice());
	}

	@Override
	public Long getBestBidPrice() {	return buyBook.getBestPrice();}

	@Override
	public Long getBestOfferPrice() {return sellBook.getBestPrice();}

	@Override
	public Long getLastKnownBestBidPrice() {
		return buyBook.getLastKnownBestPrice();
	}

	@Override
	public Long getLastKnownBestOfferPrice() {
		return sellBook.getLastKnownBestPrice();
	}

	public Long getLastUsedPrice() { return lastUsedPrice;}

	private TickEvent getBestMarketEvent (List<TickEvent<Order>> list) {
		if (list.isEmpty()) return null;

		TickEvent <Order> order = list.get(0);
		//TODO don't we just remove it after one try???
		while (order != null && order.event.getRemainingQuantity() <= 0){
			list.remove(0);
			if (list.isEmpty()) return  null;
			order = list.get(0);
		}

		return order;
	}

	private TickEvent recordMarketOrder (List list, Order order){
		TickEvent event = world.getTick(order);
		list.add(event);
		return event;
	}
}
