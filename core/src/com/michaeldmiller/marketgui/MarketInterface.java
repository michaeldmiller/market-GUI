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
    Market market;
    int frame;
    double secondFraction;
    Label prices;
    HashMap<String, Color> colorLookup;
    int scale;
    ArrayList<GraphPoint> priceDots;
    TextField goodField;
    TextField costField;
    Label errorLabel;

    public MarketInterface (final MarketGUI marketGUI) {
        this.marketGUI = marketGUI;
        frame = 0;
        secondFraction = 0.0167;
        scale = 3;
        priceDots = new ArrayList<GraphPoint>();

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
            // System.out.println(frame);
            try {
                runMarket(market, frame);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            graphPrices();
            removeGraphDots(200, priceDots);

            if (frame % 100 == 0){
                System.out.println(priceDots.size());
            }

            prices.setText(market.getPrices().toString());

        }

        stage.act(delta);
        stage.draw();


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
            GraphPoint dot = new GraphPoint(1400, priceCoord.getValue(), 2, 2, dotColor);

            // make actor leave screen
            MoveToAction leaveScreen = new MoveToAction();
            leaveScreen.setPosition(-10, priceCoord.getValue());
            leaveScreen.setDuration(50);
            dot.addAction(leaveScreen);
            priceDots.add(dot);

            stage.addActor(dot);

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

    public void addButtons(){
        Skin firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
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
        Skin firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
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

    public void changeBaseCost(String good, int newCost){
        // change the base cost of a good
        for (Price p : market.getPrices()){
            if (p.getGood().equals(good)){
                p.setOriginalCost(newCost);
            }
        }
    }

    public void makeAdjustmentFields(){
        Skin firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
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
