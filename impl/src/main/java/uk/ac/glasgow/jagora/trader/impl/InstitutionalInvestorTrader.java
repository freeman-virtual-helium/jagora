package uk.ac.glasgow.jagora.trader.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import uk.ac.glasgow.jagora.LimitBuyOrder;
import uk.ac.glasgow.jagora.Order;
import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.StockExchangeLevel1View;
import uk.ac.glasgow.jagora.trader.Level1Trader;

/**
 * Follows a fixed trading schedule, typically placing very
 * large 'strategic' (i.e. obvious, stupid) orders onto the
 * market. This agent represents the detectable behaviour of
 * a very large institutional investor, such as a pension
 * fund.
 * 
 * @author Tim
 */
public class InstitutionalInvestorTrader extends SafeAbstractTrader implements Level1Trader {
	
	private PriorityQueue<ScheduledLimitBuyOrder> scheduledOrders;	
	
	private Collection<Order> placedOrders = new ArrayList<Order>();
	
	public InstitutionalInvestorTrader(String name, Long cash, Map<Stock, Integer> inventory,
			List<ScheduledLimitBuyOrder> scheduledOrders) {
		super(name, cash, inventory);
		this.scheduledOrders = new PriorityQueue<ScheduledLimitBuyOrder>(scheduledOrders);		
	}

	@Override
	public void speak(StockExchangeLevel1View traderMarketView) {
		ScheduledLimitBuyOrder nextScheduledOrder = scheduledOrders.peek();
		
		while (nextScheduledOrder != null && nextScheduledOrder.shouldBeExecuted() ){

			scheduledOrders.poll();
			LimitBuyOrder order = nextScheduledOrder.createBuyOrder(this);
			
			traderMarketView.placeLimitBuyOrder(order);	
			placedOrders.add(order);
			
			nextScheduledOrder = scheduledOrders.peek();
		}
	}
}
