package uk.ac.glasgow.jagora.trader.impl.MarketMakerBasic;


import uk.ac.glasgow.jagora.Stock;
import uk.ac.glasgow.jagora.StockWarehouse;
import uk.ac.glasgow.jagora.util.Random;

import java.util.HashMap;
import java.util.Map;

public class MarketMakerBasicBuilder {

    private String name;
    private Long cash = 0l;
    private Integer seed;

    private Map<Stock,Integer> inventory;

    private StockWarehouse stockWarehouse;

    private Float marketShare;
    private Double spread;

    private Double liquidityAdjustmentInfluence = 1.0;
    private Double inventoryAdjustmentInfluence = 1.0;

    public MarketMakerBasicBuilder (String name) {
        this.name = name;
        inventory = new HashMap<>();
    }

    public MarketMakerBasicBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public MarketMakerBasicBuilder setCash(Long cash) {
        this.cash = cash;
        return this;
    }

    public MarketMakerBasicBuilder setSeed(Integer seed) {
        this.seed = seed;
        return this;
    }

    public MarketMakerBasicBuilder addStock(Stock stock, Integer quantity) {
        inventory.put(stock, quantity);
        return this;
    }

    public MarketMakerBasicBuilder addStockWarehouse(StockWarehouse stockWarehouse){
        this.stockWarehouse = stockWarehouse;
        return this;
    }

    public MarketMakerBasicBuilder setMarketShare(Float marketShare) {
        this.marketShare = marketShare;
        return this;
    }

    public MarketMakerBasicBuilder setSpread(Double spread) {
        this.spread = spread;
        return this;
    }

    public MarketMakerBasicBuilder setLiquidityAdjustmentInfluence(Double liquidityAdjustmentInfluence) {
        this.liquidityAdjustmentInfluence = liquidityAdjustmentInfluence;
        return this;
    }

    public MarketMakerBasicBuilder setInventoryAdjustmentInfluence(Double inventoryAdjustmnetInfluence) {
        this.inventoryAdjustmentInfluence = inventoryAdjustmnetInfluence;
        return this;
    }

    public MarketMakerBasic build() {
       return new MarketMakerBasic(
               name,cash,inventory, stockWarehouse,
               marketShare, new Random(seed),spread,
               inventoryAdjustmentInfluence, liquidityAdjustmentInfluence);
    }
}
