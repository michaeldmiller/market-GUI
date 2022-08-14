package com.michaeldmiller.marketUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.michaeldmiller.economicagents.Price;

import java.util.HashMap;

public class PriceGraph extends ScrollingGraph{
    public PriceGraph(int x, int y, int width, int height, int worldWidth, int worldHeight, double scale, String title,
                      HashMap<String, Integer> dataCoordinates, HashMap<String, Color> colorLookup, Skin skin,
                      int frame, Stage stage, boolean graphNewlyCreated) {
        super(x, y, width, height, worldWidth, worldHeight, scale, title, dataCoordinates, colorLookup, skin, frame, stage, graphNewlyCreated);
    }

    @Override
    public void update(MarketInterface marketInterface) {
        // update function for the price graph, gets price data from market and then turns it into coordinates
        this.setFrame(marketInterface.frame);
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Price p : marketInterface.market.getPrices()){
            priceCoordinates.put(p.getGood(), (int) (p.getCost() * (this.getScale())));
        }
        this.setDataCoordinates(priceCoordinates);
        this.graphData();
        this.removeGraphDots(this.getX(), this.getDots());
        this.removeGraphLabels(this.getX(), this.getLabels());
    }
}
