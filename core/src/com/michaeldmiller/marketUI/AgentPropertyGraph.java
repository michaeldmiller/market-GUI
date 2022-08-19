package com.michaeldmiller.marketUI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.michaeldmiller.economicagents.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        /*
        int moneyCoordinate = (int) (chosenAgent.getMoney() * (this.getScale()));
        agentDataCoordinates.put("MarketProperty", moneyCoordinate);
         */
        // get good priority coordinates

        // In identical equilibrium, with all weights clustered together, graph clutter is a significant issue.
        // Solution: calculate average difference between the priority weights, if it is low, only graph
        // the values every fifth frame
        double totalWeight = 0;
        for (Priority p : chosenAgent.getPriorities()){
            totalWeight += p.getWeight();
        }
        double averageWeight = totalWeight / chosenAgent.getPriorities().size();
        double totalDifference = 0;
        for (Priority p : chosenAgent.getPriorities()){
            totalDifference += Math.abs(p.getWeight() - averageWeight);

        }
        double averageDifference = totalDifference/ chosenAgent.getPriorities().size();
        // through testing, average priority difference of two seems to be a cutoff value
        if (averageDifference < 2) {
            if (getFrame() % 5 == 0){
                for (Priority priority : chosenAgent.getPriorities()) {
                    int priorityWeightCoordinate = (int) (priority.getWeight() * this.getScale());
                    agentDataCoordinates.put(priority.getGood(), priorityWeightCoordinate);

                }
            }

        } else {
            for (Priority priority : chosenAgent.getPriorities()) {
                int priorityWeightCoordinate = (int) (priority.getWeight() * this.getScale());
                agentDataCoordinates.put(priority.getGood(), priorityWeightCoordinate);

            }
        }
        /*
        // Graph agent unmet needs
        for (Map.Entry<String, Consumption> consumptionEntry : chosenAgent.getConsumption().entrySet()){
            int unmetNeedCoordinate = (int) (consumptionEntry.getValue().getTotalUnmetNeed() * this.getScale());
            agentDataCoordinates.put(consumptionEntry.getKey(), unmetNeedCoordinate);
        }
         */
        /*
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
         */

        this.setDataCoordinates(agentDataCoordinates);
        this.graphData();
        this.removeGraphDots(this.getX(), this.getDots());
        this.removeGraphLabels(this.getX(), this.getLabels());
    }
}
