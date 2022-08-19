package com.michaeldmiller.marketUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.michaeldmiller.economicagents.Agent;
import com.michaeldmiller.economicagents.Consumption;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TotalUnmetNeedsGraph extends ScrollingGraph{
    public TotalUnmetNeedsGraph(int x, int y, int width, int height, int worldWidth, int worldHeight, double scale,
                           String title, HashMap<String, Integer> dataCoordinates, HashMap<String, Color> colorLookup,
                           Skin skin, int frame, Stage stage, boolean graphNewlyCreated) {
        super(x, y, width, height, worldWidth, worldHeight, scale, title, dataCoordinates, colorLookup, skin, frame, stage, graphNewlyCreated);
    }

    @Override
    public void update(MarketInterface marketInterface) {
        // update function for the unmet need graph, gets unmet need data from each agent and then turns it into coordinates
        this.setFrame(marketInterface.frame);

        HashMap<String, Double> unmetNeedTotal = new HashMap<String, Double>();
        // update 0.2.7: reflecting removal of performance intensive individual unmet needs, just grab the new agent total
        for (Agent a : marketInterface.market.getAgents()){
            for (Map.Entry<String, Consumption> consumptionEntry : a.getConsumption().entrySet()) {
                if (!unmetNeedTotal.containsKey(consumptionEntry.getKey())){
                    unmetNeedTotal.put(consumptionEntry.getKey(), consumptionEntry.getValue().getTotalUnmetNeed());
                }
                else {
                    String key = consumptionEntry.getKey();
                    unmetNeedTotal.put(key, unmetNeedTotal.get(key) + consumptionEntry.getValue().getTotalUnmetNeed());
                }
            }
        }
        // set doubles to integers
        HashMap<String, Integer> unmetNeedCoordinates = new HashMap<>();
        for (Map.Entry<String, Double> unmetEntryTotal : unmetNeedTotal.entrySet()){
            int coordinateValue = (int) (unmetEntryTotal.getValue() * (this.getScale()));
            unmetNeedCoordinates.put(unmetEntryTotal.getKey(), coordinateValue);
        }

        this.setDataCoordinates(unmetNeedCoordinates);
        // remove over height data points
        for (Iterator<Map.Entry<String, Integer>> iterator = unmetNeedCoordinates.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, Integer> entry = iterator.next();
            if (entry.getValue() > this.getHeight()){
                iterator.remove();
            }

        }
        this.graphData();
        this.removeGraphDots(this.getX(), this.getDots());
        this.removeGraphLabels(this.getX(), this.getLabels());
    }
}
