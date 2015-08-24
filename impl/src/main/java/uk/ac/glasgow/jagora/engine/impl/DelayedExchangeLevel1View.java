package uk.ac.glasgow.jagora.engine.impl;

import uk.ac.glasgow.jagora.LimitBuyOrder;
import uk.ac.glasgow.jagora.LimitSellOrder;
import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.StockExchangeLevel1View;
import uk.ac.glasgow.jagora.impl.MarketBuyOrder;
import uk.ac.glasgow.jagora.impl.MarketSellOrder;
import uk.ac.glasgow.jagora.ticker.TradeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A transparent delay layer between a trader and an
 * underlying stock exchange view. The class is useful for
 * simulating differential network latency experienced by
 * different trading agents in different locales.
 * 
 * @author Ivelin
 * @author tws
 */
public class DelayedExchangeLevel1View implements StockExchangeLevel1View {

	interface DelayedOrderExecutor extends Comparable<DelayedOrderExecutor> {
		public void execute() ;
		
		public Long getDelayedTick ();

		@Override
		default int compareTo(DelayedOrderExecutor delayedOrderExecutor){
			return this.getDelayedTick().compareTo(delayedOrderExecutor.getDelayedTick());
		}
	}

	private final Long delayedTick;

	private List<DelayedOrderExecutor> orderExecutors = new ArrayList<DelayedOrderExecutor>();
	private StockExchangeLevel1View wrappedView;


	public DelayedExchangeLevel1View(StockExchangeLevel1View wrappedView, Long delayedTick) {
		this.wrappedView = wrappedView;
		this.delayedTick = delayedTick;
	}


	public List<DelayedOrderExecutor> getOrderExecutors (){
		return orderExecutors;
	}


	@Override
	public void placeLimitBuyOrder(LimitBuyOrder limitBuyOrder) {
		this.orderExecutors.add(
				new DelayedOrderExecutor() {
					@Override
					public void execute() {
						wrappedView.placeLimitBuyOrder(limitBuyOrder);
					}

					@Override
					public Long getDelayedTick() {
						return delayedTick;
					}
				 }
		);
	}

	@Override
	public void placeLimitSellOrder(LimitSellOrder limitSellOrder) {
		this.orderExecutors.add(
				new DelayedOrderExecutor() {
					@Override
					public void execute() {
						wrappedView.placeLimitSellOrder(limitSellOrder);
					}

					@Override
					public Long getDelayedTick() {
						return delayedTick;
					}
				}
		);
	}

	@Override
	public void cancelLimitBuyOrder(LimitBuyOrder limitBuyOrder) {
		this.orderExecutors.add(
				new DelayedOrderExecutor() {
					@Override
					public void execute() {
						wrappedView.cancelLimitBuyOrder(limitBuyOrder);
					}

					@Override
					public Long getDelayedTick() {
						return delayedTick;
					}
				}
		);
	}

	@Override
	public void cancelLimitSellOrder(LimitSellOrder limitSellOrder) {
		this.orderExecutors.add (
				new DelayedOrderExecutor() {
					@Override
					public void execute() {
						wrappedView.cancelLimitSellOrder(limitSellOrder);
					}

					@Override
					public Long getDelayedTick() {
						return delayedTick;
					}
				}
		);
	}

	@Override
	public void placeMarketBuyOrder(MarketBuyOrder marketBuyOrder) {
		this.orderExecutors.add (
			new DelayedOrderExecutor() {
				@Override
				public void execute() {
					wrappedView.placeMarketBuyOrder(marketBuyOrder);
				}

				@Override
				public Long getDelayedTick() {
					return delayedTick;
				}
			}
		);
	}

	@Override
	public void placeMarketSellOrder(MarketSellOrder marketSellOrder) {
		this.orderExecutors.add (
			new DelayedOrderExecutor() {
				@Override
				public void execute() {
					wrappedView.placeMarketSellOrder(marketSellOrder);
				}

				@Override
				public Long getDelayedTick() {
					return delayedTick;
				}
			}
		);
	}
	
	@Override
	public Long getBestOfferPrice(Stock stock) {
		return wrappedView.getBestOfferPrice(stock);
	}

	@Override
	public Long getBestBidPrice(Stock stock) {
		return wrappedView.getBestBidPrice(stock);
	}

	@Override
	public Long getLastKnownBestOfferPrice(Stock stock) {
		return wrappedView.getLastKnownBestOfferPrice(stock);
	}

	@Override
	public Long getLastKnownBestBidPrice(Stock stock) {
		return wrappedView.getLastKnownBestBidPrice(stock);
	}

	@Override
	public void registerTradeListener(TradeListener tradeListener) {
		wrappedView.registerTradeListener(tradeListener);
	}
}
