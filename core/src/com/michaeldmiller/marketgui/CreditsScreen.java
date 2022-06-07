package com.michaeldmiller.marketgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class CreditsScreen implements Screen {
    final MarketGUI marketGUI;
    Stage stage;
    Label musicCredits;


    public CreditsScreen(final MarketGUI marketGUI){
        this.marketGUI = marketGUI;
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

        musicCredits = new Label ("Credits", firstSkin);
        musicCredits.setPosition(100, marketGUI.worldHeight - 50);
        stage.addActor(musicCredits);


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
