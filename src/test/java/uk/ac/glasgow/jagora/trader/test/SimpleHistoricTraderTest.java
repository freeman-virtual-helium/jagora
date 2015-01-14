package uk.ac.glasgow.jagora.trader.test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import uk.ac.glasgow.jagora.BuyOrder;
import uk.ac.glasgow.jagora.MarketFactory;
import uk.ac.glasgow.jagora.SellOrder;
import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.Trade;
import uk.ac.glasgow.jagora.impl.ContinuousOrderDrivenMarketFactory;
import uk.ac.glasgow.jagora.impl.DefaultStockExchange;
import uk.ac.glasgow.jagora.StockExchange;
import uk.ac.glasgow.jagora.test.stub.SerialTickerTapeObserver;
import uk.ac.glasgow.jagora.test.stub.StubTraderBuilder;
import uk.ac.glasgow.jagora.trader.Trader;
import uk.ac.glasgow.jagora.trader.impl.RandomTraderBuilder;
import uk.ac.glasgow.jagora.trader.impl.SimpleHistoricTrader;
import uk.ac.glasgow.jagora.trader.impl.SimpleHistoricTraderBuilder;
import uk.ac.glasgow.jagora.world.TickEvent;
import uk.ac.glasgow.jagora.world.World;
import uk.ac.glasgow.jagora.world.impl.SimpleSerialWorld;

public class SimpleHistoricTraderTest {
	
	private final Integer numberOfTraderActions = 1000;
	private final Double initialTraderCash = 1000000.00;
	private final Integer initialNumberOfLemons = 10000;
	private final Integer seed = 1;

	private Stock lemons;
	private StockExchange marketForLemons;
	
	private SimpleHistoricTrader alice;
	private Trader bob;
	private Trader charlie;
	
	private Trader dan;
	
	private World world;
	
	private SerialTickerTapeObserver tickerTapeObserver;

	@Before
	public void setUp() throws Exception {
		world = new SimpleSerialWorld(numberOfTraderActions*5l);
		lemons = new Stock("lemons");
		
		MarketFactory marketFactory = new ContinuousOrderDrivenMarketFactory();
		
		tickerTapeObserver = new SerialTickerTapeObserver();
		
		marketForLemons = new DefaultStockExchange(world, tickerTapeObserver, marketFactory);

		alice = new SimpleHistoricTraderBuilder("alice",initialTraderCash, seed)
			.addStock(lemons, initialNumberOfLemons)
			.build();
		
		bob = new RandomTraderBuilder("bob", initialTraderCash, seed)
			.addStock(lemons, initialNumberOfLemons)
			.addTradeRange(lemons, 0.1, -.1, 0, 100)
			.build();
		
		charlie = new RandomTraderBuilder("charlie", initialTraderCash, seed)
			.addStock(lemons, initialNumberOfLemons)
			.addTradeRange(lemons, 0.1, -.1, 0, 100)
			.build();
		
		dan = new StubTraderBuilder("dan", initialTraderCash)
			.addStock(lemons, 10).build();
		
		marketForLemons.addTicketTapeListener(alice, lemons);
	}

	@Test
	public void test() {
		
		//Create initial market conditions
		BuyOrder seedBuyOrder = new BuyOrder(dan, lemons, 10, 5.0);
		marketForLemons.createTraderStockExchangeView().placeBuyOrder(seedBuyOrder);
		SellOrder seedSellOrder = new SellOrder(dan, lemons, 10, 5.0);
		marketForLemons.createTraderStockExchangeView().placeSellOrder(seedSellOrder);
		
		//Allow two random traders to create a liquid market.
		for (Integer i = 0; i < numberOfTraderActions/2; i++){
			bob.speak(marketForLemons.createTraderStockExchangeView());
			charlie.speak(marketForLemons.createTraderStockExchangeView());
			marketForLemons.doClearing();
		}
		
		//Alice now participates.
		for (Integer i = 0; i < numberOfTraderActions/2; i++){
			bob.speak(marketForLemons.createTraderStockExchangeView());
			marketForLemons.doClearing();
			charlie.speak(marketForLemons.createTraderStockExchangeView());
			marketForLemons.doClearing();
			alice.speak(marketForLemons.createTraderStockExchangeView());
			marketForLemons.doClearing();
		}		
			
		List<TickEvent<Trade>> executedTrades = tickerTapeObserver.getTradeHistory(lemons);
		
		assertThat(executedTrades.size(), greaterThan(0));
		
		List<TickEvent<Trade>> aliceSellTrades = 
			executedTrades.stream()
			.filter(executedTrade -> executedTrade.event.getSeller().equals(alice))
			.collect(Collectors.toList());
		
		List<TickEvent<Trade>> aliceBuyTrades = 
				executedTrades.stream()
				.filter(executedTrade -> executedTrade.event.getBuyer().equals(alice))
				.collect(Collectors.toList());
			
		
		Double averageLemonPrice = 
			executedTrades.stream()
				.mapToDouble(executedTrade->executedTrade.event.price)
				.average()
				.getAsDouble();
		
		Double aliceSellAveragePrice = 
			aliceSellTrades.stream()
				.mapToDouble(executedTrade->executedTrade.event.price)
				.average()
				.getAsDouble();
		
		Double aliceBuyAveragePrice = 
				aliceBuyTrades.stream()
					.mapToDouble(executedTrade->executedTrade.event.price)
					.average()
					.getAsDouble();
		
		assertThat("", aliceSellAveragePrice, greaterThan(averageLemonPrice));
		assertThat("", aliceBuyAveragePrice, lessThan(averageLemonPrice));

	}
}
