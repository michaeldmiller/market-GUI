package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.michaeldmiller.economicagents.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.michaeldmiller.economicagents.MarketMain.*;

public class MainInterface implements Screen {
    final MarketUI marketUI;
    Stage stage;
    Skin firstSkin;
    Market market;
    HashMap<String, Color> colorLookup;
    Label prices;
    Label errorLabel;
    TextField goodField;
    TextField costField;
    double scale;
    int frame;
    double secondFraction;
    int numberOfAgents;
    ScrollingGraph priceGraph;
    ScrollingGraph professionGraph;
    ScrollingGraph moneyGraph;
    ScrollingGraph unmetNeedGraph;
    ScrollingGraph marketInventoryGraph;

    public MainInterface (final MarketUI marketUI) {
        this.marketUI = marketUI;
        firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
        frame = 0;
        secondFraction = 0.0167;
        //secondFraction = 1;
        scale = 1.75;
        // set number of agents
        numberOfAgents = 2000;

        // setup color lookup table
        colorLookup = new HashMap<String, Color>();
        colorLookup.put("Fish", new Color(0, 0, 0.7f, 1));
        colorLookup.put("Lumber", new Color(0, 0.7f, 0, 1));
        colorLookup.put("Grain", new Color(0.7f, 0.7f, 0, 1));
        colorLookup.put("Metal", new Color(0.7f, 0.7f, 0.7f, 1));
        // MarketProperty is a reserved good name, used for graphing data which corresponds to the market, not a good
        colorLookup.put("MarketProperty", new Color(0.2f, 0.2f, 0.2f, 1));

        stage = new Stage(new FitViewport(marketUI.worldWidth, marketUI.worldHeight));

        // add buttons
        addButtons();

        // instantiate market
        instantiateMarket();

        // make adjustment fields
        makeAdjustmentFields();

        // add price graph
        priceGraph = new ScrollingGraph((int) (0.025 * marketUI.worldWidth), (int) (0.55 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, scale, "Prices", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        // add profession graph
        professionGraph = new ScrollingGraph((int) (0.025 * marketUI.worldWidth), (int) (0.15 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 500.0 / numberOfAgents, "# of Producers", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        // add money graph
        moneyGraph = new ScrollingGraph((int) (0.425 * marketUI.worldWidth), (int) (0.55 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 0.000005, "Money", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        // unmet needs graph
        unmetNeedGraph = new ScrollingGraph((int) (0.425 * marketUI.worldWidth), (int) (0.15 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 0.1, "Unmet Needs", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        // market inventory graph
        marketInventoryGraph = new ScrollingGraph((int) (0.425 * marketUI.worldWidth), (int) (0.55 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 0.005, "Inventory", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);


        priceGraph.makeGraph();
        professionGraph.makeGraph();
        moneyGraph.makeGraph();
        unmetNeedGraph.makeGraph();
        //marketInventoryGraph.makeGraph();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.9f, 0.9f, 0.9f, 1);
        Gdx.input.setInputProcessor(stage);
        frame += 1;
        // use second fraction to determine how often to call run market
        if (frame % ((int) (secondFraction * 60)) == 0) {
            try {
                runMarket(market, frame);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            updatePriceGraph();
            updateProfessionGraph();
            updateMoneyGraph();
            updateUnmetNeedGraph();
            // updateMarketInventoryGraph();
            prices.setText(market.getPrices().toString());

        }
        priceGraph.graphLabels();
        professionGraph.graphLabels();
        moneyGraph.graphLabels();
        unmetNeedGraph.graphLabels();
        // marketInventoryGraph.graphLabels();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    // UI Instantiation
    public void addButtons(){
        Button menuButton = new TextButton("Menu", firstSkin);
        menuButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - marketUI.standardButtonHeight);
        menuButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        menuButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketUI.setScreen(marketUI.mainMenu);
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(menuButton);

        Button printButton = new TextButton("Print", firstSkin);
        printButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 2* marketUI.standardButtonHeight);
        printButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        printButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                System.out.println(priceGraph.getDots().size());
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(printButton);

    }

    public void makeAdjustmentFields(){
        goodField = new TextField("Good", firstSkin);
        goodField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (2.5 * marketUI.standardButtonHeight));
        stage.addActor(goodField);
        costField = new TextField("New Cost", firstSkin);
        costField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 3 * marketUI.standardButtonHeight);
        stage.addActor(costField);

        Button changeCostButton = new TextButton("Update Cost", firstSkin);
        changeCostButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 4* marketUI.standardButtonHeight);
        changeCostButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        changeCostButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                changePrice();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(changeCostButton);

        errorLabel = new Label ("Errors Here", firstSkin);
        errorLabel.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (4.5 * marketUI.standardButtonHeight));
        stage.addActor(errorLabel);

    }
    // Market instantiation
    public void instantiateMarket(){
        // set up market profile
        MarketInfo fish = new MarketInfo("Fish", 0.35, 1, -0.4, 0.7,
                10, 1, "Fisherman", 0.4);
        MarketInfo lumber = new MarketInfo("Lumber", 0.2, 50,-0.7, 0.8,
                10, 1, "Lumberjack", 0.2);
        MarketInfo grain = new MarketInfo("Grain", 0.45, 1.5, -0.5, 0.4,
                10, 1, "Farmer", 0.4);
        MarketInfo metal = new MarketInfo("Metal", 0.10, 0.25, -1.2, 1.5,
                10, 1, "Blacksmith", 0.05);
        ArrayList<MarketInfo> currentMarketProfile = new ArrayList<MarketInfo>();
        currentMarketProfile.add(fish);
        currentMarketProfile.add(lumber);
        currentMarketProfile.add(grain);
        currentMarketProfile.add(metal);


        // create agents
        ArrayList<Agent> marketAgents = makeAgents(currentMarketProfile, numberOfAgents);
        // create market
        market = makeMarket(currentMarketProfile, marketAgents);

        prices = new Label ("Prices", firstSkin);
        prices.setPosition(100, marketUI.worldHeight - 50);
        stage.addActor(prices);

    }

    // modifier function
    public void changePrice(){
        // given information in good and cost text fields, attempt to change the corresponding cost in the market
        boolean costOK = false;
        int costValue = 0;
        String good = goodField.getText();
        String cost = costField.getText();

        // make sure the user entered value is an integer
        try{
            System.out.println(cost);
            costValue = Integer.parseInt(cost);
            costOK = true;
        } catch (NumberFormatException e){
            errorLabel.setText("Not a valid cost!");
        }
        // if value is ok, check goods for match and assign cost
        if (costOK){
            for (Price p : market.getPrices()){
                if (p.getGood().equals(good)){
                    p.setOriginalCost(costValue);
                }
            }
        }

    }
    // graph update functions
    public void updatePriceGraph(){
        // update function for the price graph, gets price data from market and then turns it into coordinates
        priceGraph.setFrame(frame);
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Price p : market.getPrices()){
            priceCoordinates.put(p.getGood(), (int) (p.getCost() * (priceGraph.getScale())));
        }
        priceGraph.setDataCoordinates(priceCoordinates);
        priceGraph.graphData();
        priceGraph.removeGraphDots(priceGraph.getX(), priceGraph.getDots());
        priceGraph.removeGraphLabels(priceGraph.getX(), priceGraph.getLabels());
    }

    public void updateProfessionGraph(){
        // update function for the profession graph, gets job data from market and then turns it into coordinates
        professionGraph.setFrame(frame);

        HashMap<String, Integer> jobsTotal = new HashMap<String, Integer>();
        // get total amount in each job by looping through all agents
        for (Agent a : market.getAgents()){
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
            for (JobOutput j : market.getJobOutputs()){
                if (j.getJob().equals(professionTotal.getKey())){
                    professionCoordinates.put(j.getGood(), (int) (professionTotal.getValue() *
                            (professionGraph.getScale())));
                }
            }
        }
        professionGraph.setDataCoordinates(professionCoordinates);
        professionGraph.graphData();
        professionGraph.removeGraphDots(professionGraph.getX(), professionGraph.getDots());
        professionGraph.removeGraphLabels(professionGraph.getX(), professionGraph.getLabels());
    }
    public void updateMoneyGraph(){
        // update function for the money graph, gets agent money data from market and then turns it into coordinates
        moneyGraph.setFrame(frame);

        int moneyTotal = 0;
        // get total funds of all agents
        for (Agent a : market.getAgents()) {
            moneyTotal += a.getMoney();
        }

        // convert profession names to good names to match with color lookup
        HashMap<String, Integer> professionCoordinates = new HashMap<String, Integer>();

        // generate non-zero coordinate:
        int moneyCoordinate = 0;
        if (((int) (moneyTotal * (moneyGraph.getScale()))) == 0){
            moneyCoordinate = 1;
        }
        else {
            moneyCoordinate = (int) (moneyTotal * (moneyGraph.getScale()));
        }
        // System.out.println(moneyCoordinate);
        professionCoordinates.put("MarketProperty", moneyCoordinate);

        moneyGraph.setDataCoordinates(professionCoordinates);
        moneyGraph.graphData();
        moneyGraph.removeGraphDots(moneyGraph.getX(), moneyGraph.getDots());
        moneyGraph.removeGraphLabels(moneyGraph.getX(), moneyGraph.getLabels());
    }

    public void updateUnmetNeedGraph(){
        // update function for the unmet need graph, gets unmet need data from each agent and then turns it into coordinates
        unmetNeedGraph.setFrame(frame);

        HashMap<String, Double> unmetNeedTotal = new HashMap<String, Double>();
        // get total amount of unmet needs by looping through each unmet need list in each good for each agent
        // this feels like it is extremely performance intensive!
        for (Agent a : market.getAgents()){
            for (Map.Entry<String, Consumption> consumptionEntry : a.getConsumption().entrySet()) {
                for (UnmetConsumption unmetConsumption : consumptionEntry.getValue().getUnmetNeeds()){
                    if (!unmetNeedTotal.containsKey(consumptionEntry.getKey())){
                        unmetNeedTotal.put(consumptionEntry.getKey(), unmetConsumption.getMissingQuantity());
                    }
                    else {
                        String key = consumptionEntry.getKey();
                        unmetNeedTotal.put(key, unmetNeedTotal.get(key) + unmetConsumption.getMissingQuantity());
                    }
                }
            }
        }
        // set doubles to integers
        HashMap<String, Integer> professionCoordinates = new HashMap<String, Integer>();
        for (Map.Entry<String, Double> unmetEntryTotal : unmetNeedTotal.entrySet()){
            int coordinateValue = (int) (unmetEntryTotal.getValue() * (unmetNeedGraph.getScale()));
            professionCoordinates.put(unmetEntryTotal.getKey(), coordinateValue);
        }

        unmetNeedGraph.setDataCoordinates(professionCoordinates);
        unmetNeedGraph.graphData();
        unmetNeedGraph.removeGraphDots(unmetNeedGraph.getX(), unmetNeedGraph.getDots());
        unmetNeedGraph.removeGraphLabels(unmetNeedGraph.getX(), unmetNeedGraph.getLabels());
    }
    public void updateMarketInventoryGraph(){
        // update function for the price graph, gets price data from market and then turns it into coordinates
        marketInventoryGraph.setFrame(frame);
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Map.Entry<String, Double> good : market.getInventory().entrySet()){
            priceCoordinates.put(good.getKey(), (int) (good.getValue() * (marketInventoryGraph.getScale())));
        }
        marketInventoryGraph.setDataCoordinates(priceCoordinates);
        marketInventoryGraph.graphData();
        marketInventoryGraph.removeGraphDots(marketInventoryGraph.getX(), marketInventoryGraph.getDots());
        marketInventoryGraph.removeGraphLabels(marketInventoryGraph.getX(), marketInventoryGraph.getLabels());
    }

}
