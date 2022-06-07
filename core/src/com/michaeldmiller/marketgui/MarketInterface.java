package com.michaeldmiller.marketgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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

    public MarketInterface (final MarketGUI marketGUI){
        this.marketGUI = marketGUI;
        frame = 0;
        secondFraction = 0.1;

        // setup color lookup table
        colorLookup = new HashMap<String, Color>();
        colorLookup.put("Fish", new Color(0, 0, 0.7f, 1));
        colorLookup.put("Lumber", new Color(0, 0.7f, 0, 1));
        colorLookup.put("Grain", new Color(0.7f, 0.7f, 0, 1));
        colorLookup.put("Metal", new Color(0.7f, 0.7f, 0.7f, 1));




        stage = new Stage(new FitViewport(marketGUI.worldWidth, marketGUI.worldHeight));

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


        // instantiate market
        MarketInfo fish = new MarketInfo("Fish", 0.35, -0.5, 0.7,
                10, 1, "Fisherman", 0.4);
        MarketInfo lumber = new MarketInfo("Lumber", 0.2, -0.5, 0.8,
                15, 1, "Lumberjack", 0.2);
        MarketInfo grain = new MarketInfo("Grain", 0.45, -0.5, 0.4,
                7, 1, "Farmer", 0.4);
        MarketInfo metal = new MarketInfo("Metal", 0.10, -1.2, 1.5,
                40, 1, "Blacksmith", 0.05);
        ArrayList<MarketInfo> currentMarketProfile = new ArrayList<MarketInfo>();
        currentMarketProfile.add(fish);
        currentMarketProfile.add(lumber);
        currentMarketProfile.add(grain);
        currentMarketProfile.add(metal);

        int numberOfAgents = 5000;
        ArrayList<Agent> marketAgents = new ArrayList<Agent>();
        marketAgents = makeAgents(currentMarketProfile, numberOfAgents);

        // Lastly, define com.michaeldmiller.economicagents.Market
        market = makeMarket(currentMarketProfile, marketAgents);

        prices = new Label ("Prices", firstSkin);
        prices.setPosition(100, marketGUI.worldHeight - 50);
        stage.addActor(prices);
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
            System.out.println(frame);
            try {
                runMarket(market, frame);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        int scale = 5;

        // access and store all current prices, adjusted to scale with the size of the graph
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Price p : market.getPrices()){
            priceCoordinates.put(p.getGood(), (int) p.getCost() * scale);
        }
        for (Map.Entry<String, Integer> priceCoord : priceCoordinates.entrySet()){
            // find color
            Color dotColor = colorLookup.get(priceCoord.getKey());
            // make dot
            GraphPoint dot = new GraphPoint(700, priceCoord.getValue(), 5, 5, dotColor);

            // make actor leave screen
            MoveToAction leaveScreen = new MoveToAction();
            leaveScreen.setPosition(0, priceCoord.getValue());
            leaveScreen.setDuration(100);
            dot.addAction(leaveScreen);

            stage.addActor(dot);

        }
        GraphPoint testDot = new GraphPoint(100, 100, 5, 5, new Color(0, 0, 0,1));
        stage.addActor(testDot);


        prices.setText(market.getPrices().toString());
        stage.act(delta);
        stage.draw();

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
