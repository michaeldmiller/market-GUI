package com.michaeldmiller.marketgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.michaeldmiller.economicagents.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.michaeldmiller.economicagents.MarketMain.*;

public class MarketInterface implements Screen {

    final MarketGUI marketGUI;
    Stage stage;
    Skin firstSkin;
    Market market;
    HashMap<String, Color> colorLookup;
    Label prices;
    Label errorLabel;
    TextField goodField;
    TextField costField;
    int scale;
    int frame;
    double secondFraction;
    int priceX;
    int priceY;
    int priceWidth;
    int priceHeight;
    ArrayList<GraphPoint> priceDots;
    ArrayList<Label> priceLabels;




    public MarketInterface (final MarketGUI marketGUI) {
        this.marketGUI = marketGUI;
        firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
        frame = 0;
        secondFraction = 0.0167;
        scale = 3;
        priceX = (int) (0.1 * marketGUI.worldWidth);
        priceY = (int) (0.1 * marketGUI.worldHeight);
        priceWidth = (int) (0.6 * marketGUI.worldWidth);
        priceHeight = (int) (0.6 * marketGUI.worldHeight);
        priceDots = new ArrayList<GraphPoint>();
        priceLabels = new ArrayList<Label>();

        // setup color lookup table
        colorLookup = new HashMap<String, Color>();
        colorLookup.put("Fish", new Color(0, 0, 0.7f, 1));
        colorLookup.put("Lumber", new Color(0, 0.7f, 0, 1));
        colorLookup.put("Grain", new Color(0.7f, 0.7f, 0, 1));
        colorLookup.put("Metal", new Color(0.7f, 0.7f, 0.7f, 1));

        stage = new Stage(new FitViewport(marketGUI.worldWidth, marketGUI.worldHeight));

        // add buttons
        addButtons();

        // instantiate market
        instantiateMarket();

        // make adjustment fields
        makeAdjustmentFields();

        // add price graph
        makePriceGraph();
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
            graphPrices();
            removeGraphDots(priceX, priceDots);
            removeGraphLabels(priceX, priceLabels);
            prices.setText(market.getPrices().toString());

        }
        priceGraphLabels();
        stage.act(delta);
        stage.draw();
    }


    public void addButtons(){
        Button menuButton = new TextButton("Menu", firstSkin);
        menuButton.setPosition(marketGUI.worldWidth - marketGUI.standardButtonWidth,
                marketGUI.worldHeight - marketGUI.standardButtonHeight);
        menuButton.setSize(marketGUI.standardButtonWidth, marketGUI.standardButtonHeight);
        menuButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketGUI.setScreen(marketGUI.mainMenu);
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(menuButton);

        Button printButton = new TextButton("Print", firstSkin);
        printButton.setPosition(marketGUI.worldWidth - marketGUI.standardButtonWidth,
                marketGUI.worldHeight - 2* marketGUI.standardButtonHeight);
        printButton.setSize(marketGUI.standardButtonWidth, marketGUI.standardButtonHeight);
        printButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                System.out.println(priceDots.size());
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(printButton);

    }

    public void instantiateMarket(){
        MarketInfo fish = new MarketInfo("Fish", 0.35, -0.5, 0.7,
                9, 1, "Fisherman", 0.4);
        MarketInfo lumber = new MarketInfo("Lumber", 0.2, -0.5, 0.8,
                15, 1, "Lumberjack", 0.2);
        MarketInfo grain = new MarketInfo("Grain", 0.45, -0.5, 0.4,
                7, 1, "Farmer", 0.4);
        MarketInfo metal = new MarketInfo("Metal", 0.10, -1.2, 1.5,
                50, 1, "Blacksmith", 0.05);
        ArrayList<MarketInfo> currentMarketProfile = new ArrayList<MarketInfo>();
        currentMarketProfile.add(fish);
        currentMarketProfile.add(lumber);
        currentMarketProfile.add(grain);
        currentMarketProfile.add(metal);

        int numberOfAgents = 2000;
        ArrayList<Agent> marketAgents = new ArrayList<Agent>();
        marketAgents = makeAgents(currentMarketProfile, numberOfAgents);

        market = makeMarket(currentMarketProfile, marketAgents);

        prices = new Label ("Prices", firstSkin);
        prices.setPosition(100, marketGUI.worldHeight - 50);
        stage.addActor(prices);

    }

    public void makeAdjustmentFields(){
        goodField = new TextField("", firstSkin);
        goodField.setPosition(marketGUI.worldWidth - 2 * marketGUI.standardButtonWidth,
                marketGUI.worldHeight - 3* marketGUI.standardButtonHeight);
        stage.addActor(goodField);
        costField = new TextField("", firstSkin);
        costField.setPosition(marketGUI.worldWidth - marketGUI.standardButtonWidth,
                marketGUI.worldHeight - 3* marketGUI.standardButtonHeight);
        stage.addActor(costField);

        Button changeCostButton = new TextButton("Update Cost", firstSkin);
        changeCostButton.setPosition(marketGUI.worldWidth - marketGUI.standardButtonWidth,
                marketGUI.worldHeight - 4* marketGUI.standardButtonHeight);
        changeCostButton.setSize(marketGUI.standardButtonWidth, marketGUI.standardButtonHeight);
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
        errorLabel.setPosition(marketGUI.worldWidth - marketGUI.standardButtonWidth,
                marketGUI.worldHeight - 5* marketGUI.standardButtonHeight);
        stage.addActor(errorLabel);




    }
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

    public void removeGraphDots(int xThreshold, ArrayList<GraphPoint> dots){
        for (int i = 0; i < dots.size(); i++){
            if (dots.get(i).getX() < xThreshold){
                dots.get(i).remove();
                dots.remove(i);
            }
        }
    }

    public void removeGraphLabels(int xThreshold, ArrayList<Label> labels){
        for (int i = 0; i < labels.size(); i++){
            if (labels.get(i).getX() < xThreshold){
                labels.get(i).remove();
                labels.remove(i);
            }
        }
    }

    public void graphPrices(){
        // access and store all current prices, adjusted to scale with the size of the graph
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Price p : market.getPrices()){
            priceCoordinates.put(p.getGood(), (int) p.getCost() * scale);
        }
        // for each price coordinate pair, lookup the appropriate color, make a dot on the graph, then set it to scroll
        // off to the left of the screen
        for (Map.Entry<String, Integer> priceCoord : priceCoordinates.entrySet()){
            // find color
            Color dotColor = colorLookup.get(priceCoord.getKey());
            // make dot
            GraphPoint dot = new GraphPoint(priceX + priceWidth, priceCoord.getValue() + priceY, 2, 2, dotColor);

            // make actor leave screen
            MoveToAction leaveScreen = new MoveToAction();
            leaveScreen.setPosition(priceX - 10, priceY + priceCoord.getValue());
            leaveScreen.setDuration(50);
            dot.addAction(leaveScreen);
            priceDots.add(dot);

            stage.addActor(dot);

        }

    }

    public void makePriceGraph(){
        GraphPoint xAxis = new GraphPoint(priceX, priceY, priceWidth, 3, new Color (0, 0, 0, 1));
        stage.addActor(xAxis);
        GraphPoint yAxis = new GraphPoint(priceX, priceY, 3, priceHeight, new Color (0, 0, 0, 1));
        stage.addActor(yAxis);

    }

    public void priceGraphLabels(){
        if (frame % 200 == 0 || frame == 1){
            Label timeLabel = new Label(String.valueOf(frame), firstSkin);
            timeLabel.setPosition(priceX + priceWidth, priceY - 20);

            MoveToAction leaveScreen = new MoveToAction();
            leaveScreen.setPosition(priceX - 10, priceY - 20);
            leaveScreen.setDuration(50);
            timeLabel.addAction(leaveScreen);
            priceLabels.add(timeLabel);

            stage.addActor(timeLabel);
        }
    }



    @Override
    public void resize(int width, int height) {

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
}
