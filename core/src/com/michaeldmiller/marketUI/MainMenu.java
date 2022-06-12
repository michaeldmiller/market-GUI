package com.michaeldmiller.marketUI;

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

    final MarketUI marketUI;
    Stage stage;


    public MainMenu (final MarketUI marketUI){
        this.marketUI = marketUI;
        stage = new Stage(new FitViewport(marketUI.worldWidth, marketUI.worldHeight));

        Skin firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
        Button startButton = new TextButton("Start", firstSkin);
        startButton.setPosition((int) ((marketUI.worldWidth/2) - (marketUI.standardButtonWidth/2)),
                (int) ((marketUI.worldHeight/2) - (marketUI.standardButtonHeight/2)));
        startButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        startButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketUI.setScreen(marketUI.mainInterface);
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(startButton);

        Button creditsButton = new TextButton("Credits", firstSkin);
        creditsButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                0);
        creditsButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        creditsButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketUI.setScreen(marketUI.creditsScreen);
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
}
