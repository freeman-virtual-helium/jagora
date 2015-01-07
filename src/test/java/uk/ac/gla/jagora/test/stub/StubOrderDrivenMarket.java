package uk.ac.gla.jagora.test.stub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.gla.jagora.BuyOrder;
import uk.ac.gla.jagora.ExecutedTrade;
import uk.ac.gla.jagora.SellOrder;
import uk.ac.gla.jagora.Stock;
import uk.ac.gla.jagora.orderdriven.OrderDrivenStockExchange;
import uk.ac.gla.jagora.orderdriven.OrderDrivenStockExchangeTraderView;

public class StubOrderDrivenMarket implements OrderDrivenStockExchange {

	private final Map<Stock,List<BuyOrder>> allBuyOrders;
	private final Map<Stock,List<SellOrder>> allSellOrders;

	/**
	 * Does nothing.
	 */
	@Override
	public void doClearing() {	}
	
	public StubOrderDrivenMarket (){
		allBuyOrders = new HashMap<Stock,List<BuyOrder>> ();
		allSellOrders = new HashMap<Stock,List<SellOrder>>();
	}
	
	@Override
	public OrderDrivenStockExchangeTraderView createTraderMarketView() {

		return new OrderDrivenStockExchangeTraderView (){

			@Override
			public Double getBestOfferPrice(Stock stock) {
				return getSellOrders(stock)
					.stream()
					.mapToDouble(sellOrder -> sellOrder.price)
					.min()
					.getAsDouble();
			}

			@Override
			public Double getBestBidPrice(Stock stock) {
				return getBuyOrders(stock)
					.stream()
					.mapToDouble(buyOrder -> buyOrder.price)
					.max()
					.getAsDouble();
			}

			@Override
			public void registerBuyOrder(BuyOrder buyOrder) {
				getBuyOrders(buyOrder.stock).add(buyOrder);
			}

			@Override
			public void registerSellOrder(SellOrder sellOrder) {
				getSellOrders(sellOrder.stock).add(sellOrder);
			}

			@Override
			public void cancelBuyOrder(BuyOrder buyOrder) {
				getBuyOrders(buyOrder.stock).remove(buyOrder);
				
			}

			@Override
			public void cancelSellOrder(SellOrder sellOrder) {
				getSellOrders(sellOrder.stock).remove(sellOrder);
			}

			@Override
			public List<SellOrder> getOpenSellOrders(Stock stock) {
				return getSellOrders(stock);
			}

			@Override
			public List<BuyOrder> getOpenBuyOrders(Stock stock) {
				return getBuyOrders(stock);
			}
			
		};
		
	}

	public List<BuyOrder> getBuyOrders(Stock stock) {
		List<BuyOrder> buyOrders = allBuyOrders.get(stock);
		if (buyOrders == null){
			buyOrders = new ArrayList<BuyOrder>();
			allBuyOrders.put(stock, buyOrders);
		}
		return buyOrders;
	}

	public List<SellOrder> getSellOrders(Stock stock) {
		List<SellOrder> sellOrders = allSellOrders.get(stock);
		if (sellOrders == null){
			sellOrders = new ArrayList<SellOrder>();
			allSellOrders.put(stock, sellOrders);
		}
		return sellOrders;
	}

	public List<ExecutedTrade> getTradeHistory(Stock stock) {
		// Does nothing as no trades are ever executed.
		return null;
	}
}
