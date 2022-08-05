package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MarketCreationScreen implements Screen {
    final MarketUI marketUI;
    Stage stage;
    Table masterTable;
    VerticalGroup marketGoods;
    Skin firstSkin;
    Label infoLabel;

    public MarketCreationScreen(final MarketUI marketUI) {
        // initialize the marketUI property, stage, and button skin
        this.marketUI = marketUI;
        stage = new Stage(new FitViewport(marketUI.worldWidth, marketUI.worldHeight));
        firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));

        // create the main UI table
        masterTable = new Table();
        masterTable.setFillParent(true);
        stage.addActor(masterTable);
        // set alignment
        masterTable.top().left();
        masterTable.padTop(10);
        // enable debugging for design purposes
        masterTable.setDebug(true);



        // create labels
        Label title = new Label("Market Creator:", firstSkin);
        Label title2 = new Label("Market Creator:", firstSkin);
        title2.setSize(100, 1000);
        Label title3 = new Label("Market Creator:", firstSkin);
        title3.setSize(100, 1000);

        // information label, wrap is true for multiple information lines
        infoLabel = new Label("Information", firstSkin);
        infoLabel.setWrap(true);


        // create menu button
        Button menuButton = new TextButton("Menu", firstSkin);
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
        // add the button to the first row
        masterTable.add();
        masterTable.add(menuButton).expandX().top().right();
        masterTable.row();

        // create vertical group, for adding and removing good properties
        marketGoods = new VerticalGroup();
        marketGoods.align(Align.left);

        // create good button
        Button createGoodButton = new TextButton("Add Good", firstSkin);
        createGoodButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // create a new good table, add it to the marketGoods group
                marketGoods.addActor(createGood());
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        marketGoods.addActor(createGoodButton);
        // create labels
        marketGoods.addActor(createCategoryLabels());

        // create scroll pane
        ScrollPane testPane = new ScrollPane(marketGoods);
        // set to fill whole rest of the screen (might be changed later), align to the top
        masterTable.add(testPane).expandY().top();

        // info box, add information label
        masterTable.add(infoLabel).width(250).center().top();
    }
    public Table createCategoryLabels(){
        // create labels and information toggle buttons for the market info categories
        Table labels = new Table();

        // good name label
        Label name = new Label ("Good Name", firstSkin);
        labels.add(name);
        // info button
        TextButton nameButton = new TextButton("I", firstSkin);
        nameButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("This is the basic name of the good.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(nameButton).padRight(25);


        // good consumption label
        Label consumption = new Label ("Base Consumption", firstSkin);
        labels.add(consumption);
        // info button
        TextButton consumptionButton = new TextButton("I", firstSkin);
        consumptionButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Base Consumption:\n" +
                        "This value is the amount of the good that each agent will consume " +
                        "per tick. This value is incredibly important, as it is one of two critical values which determine " +
                        "whether a market equilibrium can be obtained. For equilibrium to exist, the sum of the base consumptions " +
                        "of each good must be less than the sum of the base productions for each good. If this condition is met, " +
                        "the market is in surplus and the market can naturally find an equilibrium. If it is not met, " +
                        "wild behavior will ensue as the market is in a fundamental deficit. This value should be a " +
                        "positive number.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(consumptionButton).padRight(10);

        // good production label
        Label production = new Label ("Base Production", firstSkin);
        labels.add(production);
        // info button
        TextButton productionButton = new TextButton("I", firstSkin);
        productionButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Base Production:\n" +
                        "This value is the amount of the good that each agent will produce " +
                        "per tick. This value is incredibly important, as it is one of two critical values which determine " +
                        "whether a market equilibrium can be obtained. For equilibrium to exist, the sum of the base consumptions " +
                        "of each good must be less than the sum of the base productions for each good. If this condition is met, " +
                        "the market is in surplus and the market can naturally find an equilibrium. If it is not met, " +
                        "wild behavior will ensue as the market is in a fundamental deficit. This value should be a " +
                        "positive number.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(productionButton).padRight(35);

        // price elasticity label
        Label priceElasticity = new Label ("Price Elasticities", firstSkin);
        labels.add(priceElasticity);
        // info button
        TextButton priceElasticityButton = new TextButton("I", firstSkin);
        priceElasticityButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                // information source: https://en.wikipedia.org/w/index.php?title=Price_elasticity_of_demand&oldid=1100433334
                infoLabel.setText("Price Elasticities:\n" +
                        "Price Elasticity of Demand (Left Slider), Price Elasticity of Supply (Right Dropdown Menu)\n" +
                        "These values are the elasticities of supply and demand for the given good. Essentially, it " +
                        "quantifies how sensitive an agent is to price when deciding whether to consume (demand) or " +
                        "produce (supply) the good. All demand elasticities are negative, since people in general " +
                        "want to buy less of a given thing if it is more expensive, and supply elasticities are " +
                        "positive, since producers in general would like to make more of a given product if it is " +
                        "more expensive and they can therefore turn a bigger profit. For demand elasticity, the " +
                        "largest possible value here is almost, but not quite zero. A larger magnitude number means " +
                        "that the agent is very sensitive to price on this particular good, this is said to be elastic " +
                        "and in the real world describes things like luxury and consumer goods. Sample real values " +
                        "include -1.5 for vacation travel and -2.8 for automobiles. Smaller magnitude numbers mean the " +
                        "agent is very insensitive to price, this is said to be inelastic and in the real world " +
                        "describes staple goods like food (-0.5), oil (-0.4) and medicine (-0.31).\nSince agents " +
                        "cannot currently change the amount of a good they produce in the short term, this creation " +
                        "menu forces the elasticity of supply to be zero (the only option in the drop down menu on " +
                        "the right) to reflect this inflexibility.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(priceElasticityButton).padRight(50);

        // good base cost label
        Label baseCost = new Label ("Base Cost", firstSkin);
        labels.add(baseCost);
        // info button
        TextButton baseCostButton = new TextButton("I", firstSkin);
        baseCostButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Base Cost:\n" +
                        "This value is a multiplier which represents a user defined offset in the intrinsic " +
                        "worth assigned to a good. It cannot be negative.\n" +
                        "In practice, it is best to set all base costs in a market to the " +
                        "same value. If they are different, the market has a very difficult time finding " +
                        "equilibrium, as agents will habitually prefer goods with higher base costs without any" +
                        "kind of justification internal to the market.\nIt is strongly recommended that this value " +
                        "be set to 10 for all goods.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(baseCostButton).padRight(30);

        // good profession label
        Label profession = new Label ("Profession Name", firstSkin);
        labels.add(profession);
        // info button
        TextButton professionButton = new TextButton("I", firstSkin);
        professionButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("This is the name of the profession associated with producing this good, e.g. " +
                        "a fisherman produces fish and a farmer produces grain.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(professionButton).padRight(30);

        // job chance label
        Label chance = new Label ("Job Chance", firstSkin);
        labels.add(chance);
        // info button
        TextButton chanceButton = new TextButton("I", firstSkin);
        chanceButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("This is the chance that an agent in the market will have the profession which " +
                        "produces the specified good at the start of the simulation. The system which makes this " +
                        "random choice works based upon the ratios between the given values and will therefore work " +
                        "for any number. However, to understand the choice it is making, it is best to make sure " +
                        "that the sum of all of these values is 1. In that case, the job chance can be read as the" +
                        "percent chance each agent will have the given profession.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(chanceButton).padRight(140);





        return labels;
    }

    public Table createGood(){
        // create table
        Table good = new Table();

        // get good name
        TextField goodName = new TextField("Good", firstSkin);
        good.add(goodName).padRight(10);

        // get baseConsumption
        TextField baseConsumption = new TextField("Base Consumption", firstSkin);
        good.add(baseConsumption).padRight(10);

        // get baseProduction
        TextField baseProduction = new TextField("Base Production", firstSkin);
        good.add(baseProduction).padRight(10);

        // get price elasticity of demand
        final Label elasticityDemandLabel = new Label("-5", firstSkin);
        // set fixed size so value change does not slightly resize the label
        good.add(elasticityDemandLabel).size(33);

        final Slider elasticityDemand = new Slider(-5f, -0.01f, 0.1f, false, firstSkin);
        good.add(elasticityDemand).padRight(10);
        elasticityDemand.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // get the value of the slider, round it to two decimal places, set it as the label text
                elasticityDemandLabel.setText(String.format("%.2f", elasticityDemand.getValue()));
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });

        // get price elasticity of supply (must be 0 at present)
        SelectBox<Integer> supplyElasticity = new SelectBox<>(firstSkin);
        Array<Integer> supplyChoices = new Array<>();
        supplyChoices.add(0);
        supplyElasticity.setItems(supplyChoices);
        good.add(supplyElasticity).padRight(10);

        // get good base cost multiplier
        TextField baseCost = new TextField("Base Cost", firstSkin);
        good.add(baseCost).padRight(10);

        // get profession name
        TextField professionName = new TextField("Profession Name", firstSkin);
        good.add(professionName).padRight(10);

        // get job chance (between 0 and 1)
        final Label jobChanceLabel = new Label("0", firstSkin);
        // set fixed size so value change does not slightly resize the label
        good.add(jobChanceLabel).size(25);

        final Slider jobChance = new Slider(0f, 1f, 0.05f, false, firstSkin);
        good.add(jobChance).padRight(10);
        jobChance.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // get the value of the slider, round it to two decimal places, set it as the label text
                jobChanceLabel.setText(String.format("%.2f", jobChance.getValue()));
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });


        // remove button
        Button remove = new TextButton("Remove Good", firstSkin);
        good.add(remove);
        remove.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // get the table that the good belongs to and remove it from the marketGoods group
                marketGoods.removeActor(event.getListenerActor().getParent());
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });

        // return the finished good table
        return good;

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
