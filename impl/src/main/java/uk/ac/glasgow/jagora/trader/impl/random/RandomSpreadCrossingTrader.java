package uk.ac.glasgow.jagora.trader.impl.random;

import uk.ac.glasgow.jagora.LimitBuyOrder;
import uk.ac.glasgow.jagora.LimitSellOrder;
import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.StockExchangeLevel1View;
import uk.ac.glasgow.jagora.impl.DefaultLimitBuyOrder;
import uk.ac.glasgow.jagora.impl.DefaultLimitSellOrder;
import uk.ac.glasgow.jagora.trader.Level1Trader;
import uk.ac.glasgow.jagora.trader.impl.SafeAbstractTrader;
import uk.ac.glasgow.jagora.util.Random;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class RandomSpreadCrossingTrader extends SafeAbstractTrader implements Level1Trader {
	
	protected static class TradeRange {
		public final Integer minQuantity;
		public final Integer maxQuantity;
		public final Long price;
		
		public TradeRange(Integer minQuantity, Integer maxQuantity, Long price){
			this.minQuantity = minQuantity;
			this.maxQuantity = maxQuantity;
			this.price = price;
		}
	}
		
	private final Map<Stock,TradeRange> tradeRanges;
	protected final Random random;
	
	protected RandomSpreadCrossingTrader(
		String name, Long cash, Map<Stock, Integer> inventory,
		Random random, Map<Stock,TradeRange> tradeRanges) {
		
		super(name, cash, inventory);
		this.random = random;
		this.tradeRanges = new HashMap<Stock,TradeRange>(tradeRanges);
	}

	@Override
	public void speak(StockExchangeLevel1View traderMarketView) {
		Stock randomStock = random.chooseElement(inventory.keySet());
		
		if (random.nextBoolean())
			performRandomSellAction(randomStock, traderMarketView);
		else 
			performRandomBuyAction(randomStock, traderMarketView);
	}

	/**
     * Either sells a random quantity of stock at random price
     * or it cancels an open sell order
     * @param stock
     * @param stockExchangeLevel1View
     */
	private void performRandomSellAction(
		Stock stock, StockExchangeLevel1View stockExchangeLevel1View) {
		
		Long bestBidPrice = stockExchangeLevel1View.getBestBidPrice(stock);
		if (bestBidPrice == null) return;
		
		Integer uncommittedQuantity = 
			getAvailableQuantity(stock);
		
		Integer quantity = 
			createRandomQuantity(stock, uncommittedQuantity);

		if (quantity > 0){
			
			Long price = createRandomPrice(stock, bestBidPrice, true);

			LimitSellOrder limitSellOrder =
				new DefaultLimitSellOrder(this, stock, quantity, price);

			placeSafeSellOrder(stockExchangeLevel1View, limitSellOrder);
			
		} else {
			LimitSellOrder randomSellOrder = random.chooseElement(openSellOrders);
			if (randomSellOrder != null){
				cancelSafeSellOrder(stockExchangeLevel1View, randomSellOrder);
			}
		}
	}

    /**
     * Either buys a random quantity of stock at random price
     * or cancels a SafeBuyOrder
     * @param stock
     * @param stockExchangeLevel1View
     */
	private void performRandomBuyAction(
		Stock stock, StockExchangeLevel1View stockExchangeLevel1View) {
		
		Long bestOfferPrice = stockExchangeLevel1View.getBestOfferPrice(stock);
		if (bestOfferPrice == null) return;

		Long price = createRandomPrice(stock, bestOfferPrice, false);

		Long availableCash = getAvailableCash();
		
		Integer quantity = createRandomQuantity(stock, (int)(availableCash/price));
		
		if (quantity > 0){
			
			LimitBuyOrder limitBuyOrder =
				new DefaultLimitBuyOrder(this, stock, quantity, price);
			
			placeSafeBuyOrder(stockExchangeLevel1View, limitBuyOrder);
			
		} else {
			LimitBuyOrder limitBuyOrder = random.chooseElement(openBuyOrders);
			if (limitBuyOrder != null)
				cancelSafeBuyOrder(stockExchangeLevel1View, limitBuyOrder);
		}
		
	}

    /**
     *
     * @param stock
     * @param basePrice
     * @param isSell
     * @return returns random price for stock lower the best bid (for sell) or higher than lowest ask (for buy)
     */
	protected Long createRandomPrice(Stock stock, Long basePrice, boolean isSell) {

		TradeRange tradeRange = tradeRanges.get(stock);
		Long randomPrice = 
			(isSell?-1:1) * (long)(random.nextDouble() *  tradeRange.price) + basePrice;
		
		return max(randomPrice, 0l);
	}

    /**
     *
     * @param stock
     * @param ceiling maximum available quantity in the stock
     * @return random quantity of stock, which is in its tradeRange for the particular trader
     */
	protected Integer createRandomQuantity(Stock stock, Integer ceiling) {
		TradeRange stockData = tradeRanges.get(stock);
		
		Integer tradeQuantityRange = stockData.maxQuantity - stockData.minQuantity;

		return min(random.nextInt(tradeQuantityRange) + stockData.minQuantity, ceiling);
	}

}