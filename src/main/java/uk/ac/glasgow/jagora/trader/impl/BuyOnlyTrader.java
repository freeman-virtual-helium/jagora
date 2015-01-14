package uk.ac.glasgow.jagora.trader.impl;

import java.util.HashMap;

import uk.ac.glasgow.jagora.BuyOrder;
import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.StockExchangeTraderView;

public class BuyOnlyTrader extends SafeAbstractTrader {

	private Stock stock;
	private Integer quantity;
	private Double price;

	public BuyOnlyTrader(String name, Double cash,	Stock stock, Double price, Integer quantity) {
		super(name, cash, new HashMap<Stock,Integer>());
		this.stock = stock;
		this.price = price;
		this.quantity = quantity;
		
	}

	@Override
	public void speak(StockExchangeTraderView traderView) {
		BuyOrder buyOrder = new BuyOrder(this, stock, quantity, price);
		this.placeSafeBuyOrder(traderView, buyOrder);
	}

}