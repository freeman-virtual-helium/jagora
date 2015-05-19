package uk.ac.glasgow.jagora.ticker;

import static java.lang.String.format;
import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.trader.Trader;

public class OrderEntryEvent {
	public final Long tick;
	public final Trader trader;
	public final Stock stock;
	public final Double price;
	public final Boolean isOffer;
	
	public OrderEntryEvent(Long tick, Trader trader, Stock stock, Double price, Boolean isOffer) {
		this.tick = tick;
		this.trader = trader;
		this.stock = stock;
		this.price = price;
		this.isOffer = isOffer;
	}
	
	@Override
	public String toString (){
		String template = "[tick=%d,trader=%s,stock=%s,direction=%s,price=%.2f]";
		return format(template, tick, trader, stock, isOffer?"S":"B", price);
	}
}
