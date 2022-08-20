package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainMenu implements Screen {

    final MarketUI marketUI;
    Stage stage;
    Table masterTable;


    public MainMenu (final MarketUI marketUI){
        // store main program and create stage
        this.marketUI = marketUI;
        stage = new Stage(new FitViewport(marketUI.worldWidth, marketUI.worldHeight));
        // initialize button skin
        Skin firstSkin = new Skin(Gdx.files.internal("skin/cloud-form/cloud-form-ui.json"));
        // create master table, set it to fill the screen
        masterTable = new Table();
        masterTable.setFillParent(true);
        stage.addActor(masterTable);
        masterTable.top().left();
        // enable debugging for design purposes
        // masterTable.setDebug(true);

        // initialize first row with creator logo
        Texture creatorInfoTexture = new Texture(Gdx.files.internal("CreatorInformation.png"));
        Drawable creatorInfoDrawable = new TextureRegionDrawable(new TextureRegion(creatorInfoTexture));
        Image creatorInfo = new Image(creatorInfoDrawable);
        masterTable.add(creatorInfo).padTop(5).top().right();
        masterTable.row();

        // create second row, with the main logo
        Texture mainLogoTexture = new Texture(Gdx.files.internal("MarketSimulatorLogo.png"));
        Drawable mainLogoDrawable = new TextureRegionDrawable(new TextureRegion(mainLogoTexture));
        Image mainLogo = new Image(mainLogoDrawable);
        Table mainLogoTable = new Table();
        masterTable.add(mainLogo).padTop(250).padBottom(75).expandX();
        masterTable.row();

        // create third row with the start button
        Button startButton = new TextButton("Start", firstSkin);
        startButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketUI.setScreen(marketUI.creatorScreen);
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        masterTable.add(startButton).size(marketUI.standardButtonWidth, marketUI.standardButtonHeight).top().expandY();
        masterTable.row();

        // create bottom row
        // if there is a market to resume, add the button to go to it. If there is no market yet, add an empty
        // cell. This main menu is rebuilt when the status of the market's existence changes.
        if(marketUI.marketExists){
            Button resumeMarketButton = new TextButton("Resume Market", firstSkin);
            resumeMarketButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
            resumeMarketButton.addListener(new InputListener(){
                @Override
                public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                    marketUI.setScreen(marketUI.marketInterface);
                    dispose();
                }
                @Override
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                    return true;
                }
            });
            masterTable.add(resumeMarketButton).size(marketUI.standardButtonWidth, marketUI.standardButtonHeight).right().bottom();
        } else{
            // make size of the empty cell identical to resume market button for location consistency
            masterTable.add().size(marketUI.standardButtonWidth, marketUI.standardButtonHeight).right().bottom();
        }


        /*
        startButton.setPosition((int) ((marketUI.worldWidth/2) - (marketUI.standardButtonWidth/2)),
                (int) ((marketUI.worldHeight/2) - (marketUI.standardButtonHeight/2) - marketUI.standardButtonHeight));
        startButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        startButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketUI.setScreen(marketUI.creatorScreen);
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(startButton);


        if(marketUI.marketExists){
            Button temporaryCreatorButton = new TextButton("Resume Market", firstSkin);
            temporaryCreatorButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                    marketUI.standardButtonHeight);
            temporaryCreatorButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
            temporaryCreatorButton.addListener(new InputListener(){
                @Override
                public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                    marketUI.setScreen(marketUI.marketInterface);
                    dispose();
                }
                @Override
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                    return true;
                }
            });
            stage.addActor(temporaryCreatorButton);
        }

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

         */
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
