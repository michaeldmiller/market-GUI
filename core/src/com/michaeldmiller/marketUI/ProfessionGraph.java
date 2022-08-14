package com.michaeldmiller.marketUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.michaeldmiller.economicagents.Agent;
import com.michaeldmiller.economicagents.JobOutput;

import java.util.HashMap;
import java.util.Map;

public class ProfessionGraph extends ScrollingGraph{
    public ProfessionGraph(int x, int y, int width, int height, int worldWidth, int worldHeight, double scale,
                           String title, HashMap<String, Integer> dataCoordinates, HashMap<String, Color> colorLookup,
                           Skin skin, int frame, Stage stage, boolean graphNewlyCreated) {
        super(x, y, width, height, worldWidth, worldHeight, scale, title, dataCoordinates, colorLookup, skin, frame, stage, graphNewlyCreated);
    }

    @Override
    public void update(MarketInterface marketInterface) {
        // update function for the profession graph, gets job data from market and then turns it into coordinates
        this.setFrame(marketInterface.frame);

        HashMap<String, Integer> jobsTotal = new HashMap<String, Integer>();
        // get total amount in each job by looping through all agents
        for (Agent a : marketInterface.market.getAgents()){
            if (!jobsTotal.containsKey(a.getProfession().getJob())){
                jobsTotal.put(a.getProfession().getJob(), 1);
            }
            else {
                String key = a.getProfession().getJob();
                jobsTotal.put(key, jobsTotal.get(key) + 1);
            }
        }
        // convert profession names to good names to match with color lookup
        HashMap<String, Integer> professionCoordinates = new HashMap<String, Integer>();
        for (Map.Entry<String, Integer> professionTotal : jobsTotal.entrySet()){
            for (JobOutput j : marketInterface.market.getJobOutputs()){
                if (j.getJob().equals(professionTotal.getKey())){
                    professionCoordinates.put(j.getGood(), (int) (professionTotal.getValue() *
                            (this.getScale())));
                }
            }
        }
        this.setDataCoordinates(professionCoordinates);
        this.graphData();
        this.removeGraphDots(this.getX(), this.getDots());
        this.removeGraphLabels(this.getX(), this.getLabels());
    }
}
