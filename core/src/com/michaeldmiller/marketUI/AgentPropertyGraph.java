package com.michaeldmiller.marketUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.michaeldmiller.economicagents.Agent;
import com.michaeldmiller.economicagents.JobOutput;
import com.michaeldmiller.economicagents.MarketInfo;

import java.util.ArrayList;
import java.util.HashMap;

import static com.michaeldmiller.economicagents.MarketMain.makeAgents;

public class AgentPropertyGraph extends ScrollingGraph{

    public AgentPropertyGraph(int x, int y, int width, int height, int worldWidth, int worldHeight, double scale, String title,
                      HashMap<String, Integer> dataCoordinates, HashMap<String, Color> colorLookup, Skin skin,
                      int frame, Stage stage, boolean graphNewlyCreated) {
        super(x, y, width, height, worldWidth, worldHeight, scale, title, dataCoordinates, colorLookup, skin, frame, stage, graphNewlyCreated);
    }

    @Override
    public void update(MarketInterface marketInterface) {
        // given the Agent ID, get properties about it for display
        // start with available money and profession
        // to start: get the agent
        this.setFrame(marketInterface.frame);
        MarketInfo fish = new MarketInfo("Fish", 0.35, 1, -0.4, 0.7,
                10, 1, "Fisherman", 1);
        ArrayList<MarketInfo> currentMarketProfile = new ArrayList<>();
        currentMarketProfile.add(fish);
        Agent chosenAgent = makeAgents(currentMarketProfile, 1).get(0);
        for (Agent a : marketInterface.market.getAgents()){
            if (a.getId().equals(marketInterface.agentID)){
                chosenAgent = a;
            }
        }
        // get properties of the chosen agent
        HashMap<String, Integer> agentDataCoordinates = new HashMap<>();

        // get money coordinate
        int moneyCoordinate = (int) (chosenAgent.getMoney() * (this.getScale()));
        agentDataCoordinates.put("MarketProperty", moneyCoordinate);

        // get profession information
        int professionCoordinate = (int) (this.getHeight() / 2);
        String profession = chosenAgent.getProfession().getJob();
        String professionGood = "MarketProperty";
        for (JobOutput j : marketInterface.market.getJobOutputs()){
            if (j.getJob().equals(profession)){
                professionGood = j.getGood();
            }
        }
        agentDataCoordinates.put(professionGood, professionCoordinate);
        // System.out.println(agentDataCoordinates);

        this.setDataCoordinates(agentDataCoordinates);
        this.graphData();
        this.removeGraphDots(this.getX(), this.getDots());
        this.removeGraphLabels(this.getX(), this.getLabels());
    }
}
