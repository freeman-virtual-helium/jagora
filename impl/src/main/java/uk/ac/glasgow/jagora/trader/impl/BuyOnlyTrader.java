package uk.ac.glasgow.jagora.trader.impl;

import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.StockExchangeLevel1View;
import uk.ac.glasgow.jagora.impl.LimitBuyOrder;
import uk.ac.glasgow.jagora.trader.Level1Trader;

import java.util.HashMap;

public class BuyOnlyTrader extends SafeAbstractTrader implements Level1Trader{

	private Stock stock; //can only do one stock?
	private Integer quantity;
	private Long price; //what's the point of these?

	public BuyOnlyTrader(String name, Long cash,	Stock stock, Long price, Integer quantity) {
		super(name, cash, new HashMap<Stock,Integer>());
		this.stock = stock;
		this.price = price;
		this.quantity = quantity;
		
	}

	@Override
	public void speak(StockExchangeLevel1View traderView) {
		LimitBuyOrder limitBuyOrder = new LimitBuyOrder(this, stock, quantity, price);
		this.placeSafeBuyOrder(traderView, limitBuyOrder);
	}
}
