package com.michaeldmiller.marketgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainMenu implements Screen {

    final MarketGUI marketGUI;
    Stage stage;


    public MainMenu (final MarketGUI marketGUI){
        this.marketGUI = marketGUI;
        stage = new Stage(new FitViewport(marketGUI.worldWidth, marketGUI.worldHeight));

        Skin firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
        Button startButton = new TextButton("Start", firstSkin);
        startButton.setPosition((int) ((marketGUI.worldWidth/2) - (marketGUI.standardButtonWidth/2)),
                (int) ((marketGUI.worldHeight/2) - (marketGUI.standardButtonHeight/2)));
        startButton.setSize(marketGUI.standardButtonWidth, marketGUI.standardButtonHeight);
        startButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketGUI.setScreen(marketGUI.marketInterface);
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(startButton);

        Button creditsButton = new TextButton("Credits", firstSkin);
        creditsButton.setPosition(marketGUI.worldWidth - marketGUI.standardButtonWidth,
                0);
        creditsButton.setSize(marketGUI.standardButtonWidth, marketGUI.standardButtonHeight);
        creditsButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketGUI.setScreen(marketGUI.creditsScreen);
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(creditsButton);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.9f, 0.9f, 0.9f, 1);
        Gdx.input.setInputProcessor(stage);
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
