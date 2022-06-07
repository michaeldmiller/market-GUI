package com.michaeldmiller.marketgui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class MarketGUI extends Game {
	SpriteBatch batch;
	public Screen mainMenu;
	public Screen marketInterface;
	public Screen creditsScreen;
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
		marketInterface = new MarketInterface(this);
		creditsScreen = new CreditsScreen(this);

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
