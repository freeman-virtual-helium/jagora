package uk.ac.glasgow.jagora.experiment;

import static java.lang.String.format;
import static java.util.stream.IntStream.range;
import static uk.ac.glasgow.jagora.experiment.ExperimentalReportsPathsUtil.experimentalPricesDatFilePath;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import uk.ac.glasgow.jagora.MarketFactory;
import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.StockExchange;
import uk.ac.glasgow.jagora.StockExchangeLevel1View;
import uk.ac.glasgow.jagora.engine.TradingEngine;
import uk.ac.glasgow.jagora.engine.impl.SerialRandomEngineBuilder;
import uk.ac.glasgow.jagora.impl.ContinuousOrderDrivenMarketFactory;
import uk.ac.glasgow.jagora.impl.DefaultStockExchange;
import uk.ac.glasgow.jagora.impl.LimitBuyOrder;
import uk.ac.glasgow.jagora.impl.LimitSellOrder;
import uk.ac.glasgow.jagora.pricer.TradePricer;
import uk.ac.glasgow.jagora.pricer.impl.OldestOrderPricer;
import uk.ac.glasgow.jagora.test.stub.StubTraderBuilder;
import uk.ac.glasgow.jagora.ticker.impl.SerialTickerTapeObserver;
import uk.ac.glasgow.jagora.trader.Level1Trader;
import uk.ac.glasgow.jagora.trader.impl.InstitutionalInvestorTraderBuilder;
import uk.ac.glasgow.jagora.trader.impl.RandomTrader;
import uk.ac.glasgow.jagora.trader.impl.RandomTraderBuilder;
import uk.ac.glasgow.jagora.world.World;
import uk.ac.glasgow.jagora.world.impl.SimpleSerialWorld;

/**
 * Demonstrates random traders establishing a price
 * equilibrium and then responding to a large buy order by
 * increasing the equilibrium price.
 * 
 * @author Tim
 *
 */
public class Experiment0002 {
		

	private final String pricesDatFilePath = experimentalPricesDatFilePath(this.getClass());
		
	private World world;
	private Stock lemons;
	private StockExchange stockExchange;
	
	private SerialTickerTapeObserver tickerTapeObserver;
	
	private TradingEngine engine;
	
	@Before
	public void setUp() throws Exception {

		Random r = new Random(1);

		lemons = new Stock("lemons");
		
		
		world = new SimpleSerialWorld(2000000l);
		
		TradePricer tradePricer = new OldestOrderPricer ();
		
		MarketFactory marketFactory = new ContinuousOrderDrivenMarketFactory(tradePricer);
		
		tickerTapeObserver = new SerialTickerTapeObserver();
		
		stockExchange = new DefaultStockExchange(world, tickerTapeObserver, marketFactory);

		Set<Level1Trader> traders = new HashSet<Level1Trader>();
						
		Level1Trader dan = new StubTraderBuilder("stub")
			.setCash(200l)
			.addStock(lemons, 1)
			.build();
		
		StockExchangeLevel1View dansView = stockExchange.createLevel1View();
		dansView.placeBuyOrder(new LimitBuyOrder(dan, lemons, 1, 99l));
		dansView.placeSellOrder(new LimitSellOrder(dan, lemons, 1, 101l));
				

		for (Integer i : range(0, 49).toArray()){
			
			RandomTrader trader = 
				new RandomTraderBuilder()
				.setName(format("RandomTrader[%d]", i))
				.setCash(200l)
				.setSeed(r.nextInt())
				.addStock(lemons, 1)
				.setSellOrderRange(lemons, 1, 2, -1l, 10l)
				.setBuyOrderRange (lemons, 1, 2, -9l, 2l)
				.build();
			
			traders.add(trader);
		}
													
		Level1Trader institutionalInvestorTrader = 
			new InstitutionalInvestorTraderBuilder()
			.setName("InstitutionalInvestorTrader")
			.setCash(100000l)
			.addScheduledLimitBuyOrder(1000000l, world, lemons, 25, 400l)
			.build();
		traders.add(institutionalInvestorTrader);


		PrintStream printStream = new PrintStream(new FileOutputStream(pricesDatFilePath));
		
		GnuPlotPriceDATLogger gnuPlotPriceDATLogger = new GnuPlotPriceDATLogger(printStream);
		
		tickerTapeObserver.registerTradeListener(gnuPlotPriceDATLogger);
		tickerTapeObserver.registerOrderListener(gnuPlotPriceDATLogger);
		
		engine = new SerialRandomEngineBuilder(world, 1)
			.addStockExchange(stockExchange)
			.addTraders(traders)
			.build();
	}
	
	@Test
	public void test() {
		engine.run();
	}

}
