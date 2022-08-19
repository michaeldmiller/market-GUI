package com.michaeldmiller.marketUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.michaeldmiller.economicagents.Agent;
import com.michaeldmiller.economicagents.MarketInfo;
import com.michaeldmiller.economicagents.Priority;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PriorityGraph extends ScrollingGraph{
    public PriorityGraph(int x, int y, int width, int height, int worldWidth, int worldHeight, double scale,
                         String title, HashMap<String, Integer> dataCoordinates, HashMap<String, Color> colorLookup,
                         Skin skin, int frame, Stage stage, boolean graphNewlyCreated) {
        super(x, y, width, height, worldWidth, worldHeight, scale, title, dataCoordinates, colorLookup, skin, frame, stage, graphNewlyCreated);
    }

    @Override
    public void update(MarketInterface marketInterface) {
        // get total agent buying priorities for each good
        this.setFrame(marketInterface.frame);
        // create output coordinate hashmap
        HashMap<String, Integer> priorityDataCoordinates = new HashMap<>();

        HashMap<String, Double> priorityAccumulator = new HashMap<>();
        // for each good, add it to the priority accumulator
        for (MarketInfo marketInfo : marketInterface.market.getMarketProfile()){
            priorityAccumulator.put(marketInfo.getGood(), 0.0);
        }
        // now that all possible goods are present in the accumulator, loop through each agent, get its priority
        // values for each good, and add them to the appropriate accumulator. This saves needing to loop
        // through the entire agent list many times to get the data.
        for (Agent a : marketInterface.market.getAgents()){
            for (Priority priority : a.getPriorities()){
                priorityAccumulator.put(priority.getGood(), priorityAccumulator.get(priority.getGood()) + priority.getWeight());
            }
        }
        // convert to coordinates
        for (Map.Entry<String, Double> priorityEntry : priorityAccumulator.entrySet()){
            priorityDataCoordinates.put(priorityEntry.getKey(), (int) (priorityEntry.getValue() * (this.getScale())));
        }

        this.setDataCoordinates(priorityDataCoordinates);
        // remove over height data points
        for (Iterator<Map.Entry<String, Integer>> iterator = priorityDataCoordinates.entrySet().iterator(); iterator.hasNext();) {
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
