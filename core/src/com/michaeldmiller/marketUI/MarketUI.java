package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MarketUI extends Game {
	SpriteBatch batch;
	public Screen mainMenu;
	public Screen mainInterface;
	public Screen creditsScreen;
	public Screen creatorScreen;
	public int worldWidth;
	public int worldHeight;
	public int standardButtonWidth = 150;
	public int standardButtonHeight = 50;
	
	@Override
	public void create () {
		// Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		batch = new SpriteBatch();
		worldWidth = 1600;
		worldHeight = 900;


		mainMenu = new MainMenu(this);
		mainInterface = new MainInterface(this);
		creditsScreen = new CreditsScreen(this);
		creatorScreen = new MarketCreationScreen(this);

		setScreen(mainMenu);




	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
