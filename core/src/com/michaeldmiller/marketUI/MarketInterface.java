package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.michaeldmiller.economicagents.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.michaeldmiller.economicagents.MarketMain.*;

public class MarketInterface implements Screen {
    final MarketUI marketUI;
    public Stage stage;
    Skin firstSkin;
    Market market;
    ArrayList<MarketInfo> currentMarketProfile;
    HashMap<String, Color> colorLookup;
    Label prices;
    Label moreInfo;
    Label errorLabel;
    TextField goodField;
    TextField consumptionField;
    TextField costField;
    TextField consumptionCostField;
    TextField agentField;
    double scale;
    int frame;
    double secondFraction;
    int numberOfAgents;
    ScrollingGraph priceGraph;
    ScrollingGraph professionGraph;
    ScrollingGraph moneyGraph;
    ScrollingGraph unmetNeedGraph;
    ScrollingGraph marketInventoryGraph;
    ScrollingGraph agentPropertyGraph;
    String agentID;
    Drawable infoIcon;
    Drawable infoIconClicked;
    Table masterTable;
    VerticalGroup marketGoods;
    Label infoLabel;
    HashMap<Integer, ScrollingGraph> graphs;


    public MarketInterface (final MarketUI marketUI, int specifiedNumberOfAgents,
                          ArrayList<MarketInfo> specifiedMarketProfile) {
        this.marketUI = marketUI;
        this.graphs = new HashMap<>();
        firstSkin = new Skin(Gdx.files.internal("skin/cloud-form/cloud-form-ui.json"));
        frame = 0;
        secondFraction = 0.0167;
        // secondFraction = 1;
        scale = 1.75;
        // set number of agents
        numberOfAgents = specifiedNumberOfAgents;
        currentMarketProfile = specifiedMarketProfile;
        // set initial agent
        agentID = "1";

        // info button texture and drawable
        Texture infoIconTexture = new Texture(Gdx.files.internal("info-button-icon-26.png"));
        infoIcon = new TextureRegionDrawable(new TextureRegion(infoIconTexture));
        Texture infoIconClickedTexture = new Texture(Gdx.files.internal("info-button-icon-clicked-26.png"));
        infoIconClicked = new TextureRegionDrawable(new TextureRegion(infoIconClickedTexture));

        // TODO: Add dynamic color lookup
        // setup color lookup table
        colorLookup = new HashMap<String, Color>();
        colorLookup.put("Fish", new Color(0, 0, 0.7f, 1));
        colorLookup.put("Lumber", new Color(0, 0.7f, 0, 1));
        colorLookup.put("Grain", new Color(0.7f, 0.7f, 0, 1));
        colorLookup.put("Metal", new Color(0.7f, 0.7f, 0.7f, 1));
        colorLookup.put("Brick", new Color(0.7f, 0, 0, 1));
        // MarketProperty is a reserved good name, used for graphing data which corresponds to the market, not a good
        colorLookup.put("MarketProperty", new Color(0.2f, 0.2f, 0.2f, 1));

        stage = new Stage(new FitViewport(marketUI.worldWidth, marketUI.worldHeight));

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
        Label title = new Label("Market Creator", firstSkin);

        // information label, wrap is true for multiple information lines
        infoLabel = new Label("------------Information------------", firstSkin);
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

        // add the title and button to the first row
        Table titleInstructions = new Table();
        titleInstructions.add(title).padLeft(50);
        titleInstructions.add().expand();
        titleInstructions.add(createInstructions());
        masterTable.add(titleInstructions).align(Align.left).fill();


        masterTable.add(menuButton).top().right().width(100);
        masterTable.row();

        // create vertical group, with one or two rows, each containing a horizontal group, for the graphs
        marketGoods = new VerticalGroup();
        marketGoods.align(Align.left);

        HorizontalGroup graphRow1 = new HorizontalGroup();



        // Texture testTexture = new Texture(Gdx.files.internal("test.png"));
        // Drawable testDraw = new TextureRegionDrawable(new TextureRegion(testTexture));

        // Image x = new Image(testDraw);
        // x.setHeight(4000);
        // x.setSize(1000, 400);
        // graphRow1.addActor(x);

        marketGoods.setSize(2000, 400);

        marketGoods.addActor(graphRow1);
        marketGoods.expand();

        // create labels
        // marketGoods.addActor(createCategoryLabels());

        // create scroll pane for goods
        // set to fill whole rest of the screen (might be changed later), align to the top
        // masterTable.add(marketGoods).expandY().top();

        // create invisible table to provide size suggestions to the graphs, which are positioned outside
        // the master table directly on the stage.

        Table graphDisplayTable = new Table();
        masterTable.add(graphDisplayTable).expand();
        // the code is unnecessary, as the graphs are placed using absolute coordinates, but the graphs are placed
        // within a virtual 5x5 table, in (2, 2), (4, 2), (2, 4), and (4, 4). Row and Column 1, 3, and 5, explicitly
        // control the left, center, and right (top, middle, and bottom) padding of the graph cells.

        // using tables is inefficient for the task, as there does not seem to be an easy way to access the
        // absolute x and y coordinates, relative to the screen, of any given cell.
        // Proposed solution: through trial and error, with the aid of the table measurements gathered from earlier,
        // determine the absolute coordinates of the four graph locations for the screen size. This will mean that
        // this program is fixed to a display size of 1600 x 900, although converting them back into world size ratios,
        // as done previously, may be possible.


        // Left, Center, and Right X padding of 70, Left, Center, and Right Padding of 45, noting the 55 height
        // of the lower bar. Given a target width of 1350, confined by 250 wide info box, these are the resulting
        // coordinates of each graph slot:
        // Slot 1: 70, 480;
        // Slot 2: 70, 100;
        // Slot 3: 710, 480;
        // Slot 4: 710, 100;

        // inferred dimensions of each graph given the number of graphs:
        // three or four: (570, 335)
        // two: (1210, 335)
        // one: (1210, 715)

        // add price graph;
        /*
        priceGraph = new ScrollingGraph(70,  480, 570, 335, marketUI.worldWidth,
                marketUI.worldHeight, scale, "Prices", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        priceGraph.makeGraph();

         */

        Skin secondSkin = new Skin(Gdx.files.internal("skin/cloud-form/cloud-form-ui.json"));
        int secondFrame = 0;
        // setup color lookup table
        HashMap<String,Color> secondColorLookup = new HashMap<String, Color>();
        secondColorLookup.put("Fish", new Color(0, 0, 0.7f, 1));
        secondColorLookup.put("Lumber", new Color(0, 0.7f, 0, 1));
        secondColorLookup.put("Grain", new Color(0.7f, 0.7f, 0, 1));
        secondColorLookup.put("Metal", new Color(0.7f, 0.7f, 0.7f, 1));
        secondColorLookup.put("Brick", new Color(0.7f, 0, 0, 1));
        // MarketProperty is a reserved good name, used for graphing data which corresponds to the market, not a good
        secondColorLookup.put("MarketProperty", new Color(0.2f, 0.2f, 0.2f, 1));
        PriceGraph notAnAttributePriceGraph = new PriceGraph(70,  480, 570, 335, 1600,
                900, 1.75, "Prices", new HashMap<String, Integer>(),
                secondColorLookup, secondSkin, 1, stage);
        notAnAttributePriceGraph.makeGraph();
        ArrayList<PriceGraph> test = new ArrayList<>();
        graphs.put(0, notAnAttributePriceGraph);
        // add profession graph
        professionGraph = new ProfessionGraph(70, 100, 570, 335, marketUI.worldWidth,
                marketUI.worldHeight, 500.0 / numberOfAgents, "# of Producers", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        professionGraph.makeGraph();
        graphs.put(1, professionGraph);
        // agent property graph
        agentPropertyGraph = new AgentPropertyGraph(710, 480, 570, 335, marketUI.worldWidth,
                marketUI.worldHeight, 0.005, "Agent: " + agentID, new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        agentPropertyGraph.makeGraph();
        graphs.put(2, agentPropertyGraph);
        // unmet needs graph
        unmetNeedGraph = new UnmetNeedsGraph(710, 100, 570,335, marketUI.worldWidth,
                marketUI.worldHeight, 0.01, "Unmet Needs", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        unmetNeedGraph.makeGraph();
        graphs.put(3, unmetNeedGraph);


        // information panel: graph selectors and information label
        VerticalGroup informationPanel = new VerticalGroup();
        Button zero = new CheckBox("0", firstSkin);
        Button one = new CheckBox("1", firstSkin);
        Button two = new CheckBox("2", firstSkin);
        Button three = new CheckBox("3", firstSkin);
        Button four = new CheckBox("4", firstSkin);
        final ButtonGroup<Button> numberOfGraphs = new ButtonGroup<>();
        numberOfGraphs.add(zero, one, two, three, four);
        // set zero to checked
        numberOfGraphs.setChecked("0");
        // make sure one box is checked at all times
        numberOfGraphs.setMinCheckCount(1);
        numberOfGraphs.setMaxCheckCount(1);
        // create options
        Label numberOfGraphsPrompt = new Label("Number of Graphs", firstSkin);
        informationPanel.addActor(numberOfGraphsPrompt);
        Table graphNumberCheckBoxes = new Table();
        graphNumberCheckBoxes.add(zero).padRight(10);
        graphNumberCheckBoxes.add(one).padRight(10);
        graphNumberCheckBoxes.add(two).padRight(10);
        graphNumberCheckBoxes.add(three).padRight(10);
        graphNumberCheckBoxes.add(four).padRight(10);

        informationPanel.addActor(graphNumberCheckBoxes);

        // graph type selector horizontal group
        HorizontalGroup graphTypeGroup = new HorizontalGroup();
        informationPanel.addActor(graphTypeGroup);



        final SelectBox<Integer> numberOfGraphsSelector = new SelectBox<>(firstSkin);
        Array<Integer> graphChoices = new Array<>();
        graphChoices.add(0);
        graphChoices.add(1);
        graphChoices.add(2);
        graphChoices.add(3);
        graphChoices.add(4);
        numberOfGraphsSelector.setItems(graphChoices);
        informationPanel.addActor(numberOfGraphsSelector);

        final VerticalGroup typeSelectors = new VerticalGroup();
        // typeSelectors.addActor(new Label("", firstSkin));
        informationPanel.addActor(typeSelectors);

        numberOfGraphsSelector.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                System.out.println(numberOfGraphsSelector.getSelected());
                // get current amount of graphs
                int currentNumberOfSelectors = typeSelectors.getChildren().size;
                // determine if difference is positive or negative
                int numberOfChange = numberOfGraphsSelector.getSelected() - currentNumberOfSelectors;
                // if negative, remove from the end the requested number of times
                if (numberOfChange < 0){
                    for (int i = 0; i < (numberOfChange * -1); i++){
                        // remove actor at the end of the list (have to adjust from size to index)
                        Actor removed = typeSelectors.removeActorAt(currentNumberOfSelectors - 1, false);
                        // clear the removed actor
                        removed.clear();
                        currentNumberOfSelectors--;
                    }
                } else if(numberOfChange > 0){
                    // if positive, add the requested number to the type selector
                    for (int i = 0; i < numberOfChange; i++){
                        // create select box
                        final SelectBox<String> graphTypeSelector = new SelectBox<String>(firstSkin);
                        Array<String> graphTypes = new Array<>();
                        graphTypes.add("Price");
                        graphTypes.add("Producers");
                        graphTypes.add("Agent");
                        graphTypes.add("Unmet Needs");
                        graphTypeSelector.setItems(graphTypes);

                        // create table
                        Table graphTypeSelectorTable = new Table();
                        // create label for graph number
                        int graphNumber = currentNumberOfSelectors + i + 1;

                        // create initial graph (if not using starting blank choice)
                        // createGraph(graphNumber, graphTypes.get(0));

                        Label graphNumberLabel = new Label("Graph #" + graphNumber + ":", firstSkin);
                        graphTypeSelectorTable.add(graphNumberLabel).padRight(10);


                        graphTypeSelector.addListener(new ChangeListener(){
                            @Override
                            public void changed(ChangeEvent changeEvent, Actor actor) {
                                // get current graph number
                                int index = 0;
                                int location = 0;
                                // since each of these buttons is nested in a table, and we want to see the button's
                                // index in the vertical group, need to get the button's grandparent and check
                                // it against the grandchildren
                                for (Actor tableActor : actor.getParent().getParent().getChildren()){
                                    // find the actor from the list of actors in its parent
                                    // loop through each child
                                    Table table = (Table) tableActor;
                                    for (Actor labelOrBox : table.getChildren()){
                                        if (labelOrBox.equals(actor)){
                                            location = index;
                                        }
                                    }
                                    // if this box was not found yet, increment index
                                    index++;
                                }

                                // with the location in hand, run the graph creation function
                                System.out.println(location + " " + graphTypeSelector.getSelected());
                                // createGraph(index,
                                createGraph(0, "");

                            }
                        });
                        graphTypeSelectorTable.add(graphTypeSelector);

                        typeSelectors.addActor(graphTypeSelectorTable);
                    }

                }
            }
        });

        // need to create select boxes of the appropriate amount

        // info box, add information label
        // create scroll pane
        final ScrollPane informationScrollPane = new ScrollPane(infoLabel);
        informationPanel.addActor(informationScrollPane);
        masterTable.add(informationPanel).width(250).center().top();

        // add bottom row
        masterTable.row();
        // left align
        // masterTable.add(createBottomRow()).align(Align.left).top().padLeft(10);
        // center align
        masterTable.add(createBottomRow()).top();

        instantiateMarket();


        /*

        // add buttons
        addButtons();


        // make adjustment fields
        makeAdjustmentFields();

        // add price graph
        priceGraph = new ScrollingGraph((int) (0.025 * marketUI.worldWidth), (int) (0.55 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, scale, "Prices", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        // add profession graph
        professionGraph = new ScrollingGraph((int) (0.025 * marketUI.worldWidth), (int) (0.15 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 500.0 / numberOfAgents, "# of Producers", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        // add money graph
        moneyGraph = new ScrollingGraph((int) (0.425 * marketUI.worldWidth), (int) (0.55 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 0.000005, "Money", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        // unmet needs graph
        unmetNeedGraph = new ScrollingGraph((int) (0.425 * marketUI.worldWidth), (int) (0.15 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 0.01, "Unmet Needs", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        // market inventory graph
        marketInventoryGraph = new ScrollingGraph((int) (0.425 * marketUI.worldWidth), (int) (0.55 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 0.005, "Inventory", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        // agent property graph
        agentPropertyGraph = new ScrollingGraph((int) (0.425 * marketUI.worldWidth), (int) (0.55 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 0.005, "Agent: " + agentID, new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);


        priceGraph.makeGraph();
        professionGraph.makeGraph();
        // moneyGraph.makeGraph();
        unmetNeedGraph.makeGraph();
        //marketInventoryGraph.makeGraph();
        agentPropertyGraph.makeGraph();

         */
    }

    private void createGraph(int index, String graphType){
        // determine if graph already exists with that index
        /*
        if (graphs.containsKey(index)){
            // if it does, get it and delete it.
            ScrollingGraph oldGraph = graphs.get(index);
            // remove the dots (test)
            for (GraphPoint g : oldGraph.getDots()){
                g.remove();
            }
        }

         */
        System.out.println("fired");
        // priceGraph.deleteAll(priceGraph.getDots(), priceGraph.getLabels(), priceGraph.getOtherComponents());


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

            // update the graphs, if any exist
            for (ScrollingGraph graph : graphs.values()){
                graph.update(this);
                graph.graphLabels();
            }

        }

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

    // UI Instantiation
    public void addButtons(){
        Button menuButton = new TextButton("Menu", firstSkin);
        menuButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - marketUI.standardButtonHeight);
        menuButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
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
        stage.addActor(menuButton);

        Button printButton = new TextButton("Print", firstSkin);
        printButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 2* marketUI.standardButtonHeight);
        printButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        printButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // System.out.println(priceGraph.getDots().size());
                // for (Agent a : market.getAgents()){
                //     System.out.println(a.getConsumption());
                // }
                System.out.println(market.getMarketProfile());
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(printButton);

    }

    public void makeAdjustmentFields(){
        goodField = new TextField("Good", firstSkin);
        goodField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (2.5 * marketUI.standardButtonHeight));
        stage.addActor(goodField);
        costField = new TextField("New Cost", firstSkin);
        costField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 3 * marketUI.standardButtonHeight);
        stage.addActor(costField);

        Button changeCostButton = new TextButton("Update Cost", firstSkin);
        changeCostButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 4* marketUI.standardButtonHeight);
        changeCostButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
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

        // change agent (requires agent property graph)
        agentField = new TextField("New Agent ID:", firstSkin);
        agentField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (5 * marketUI.standardButtonHeight));
        stage.addActor(agentField);

        Button changeAgentButton = new TextButton("Update Agent", firstSkin);
        changeAgentButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (6 * marketUI.standardButtonHeight));
        changeAgentButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        changeAgentButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                changeAgent();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(changeAgentButton);

        consumptionField = new TextField("Good", firstSkin);
        consumptionField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (7.5 * marketUI.standardButtonHeight));
        stage.addActor(consumptionField);
        consumptionCostField = new TextField("New Consumption", firstSkin);
        consumptionCostField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 8 * marketUI.standardButtonHeight);
        stage.addActor(consumptionCostField);

        Button changeConsumptionButton = new TextButton("Update Consumption", firstSkin);
        changeConsumptionButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 9* marketUI.standardButtonHeight);
        changeConsumptionButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        changeConsumptionButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                changeConsumption();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(changeConsumptionButton);

        errorLabel = new Label ("Errors Here", firstSkin);
        errorLabel.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (10 * marketUI.standardButtonHeight));
        stage.addActor(errorLabel);

    }
    // Market instantiation
    public void instantiateMarket(){
        // create agents
        ArrayList<Agent> marketAgents = makeAgents(currentMarketProfile, numberOfAgents);
        // create market
        market = makeMarket(currentMarketProfile, marketAgents);

        moreInfo = new Label ("", firstSkin);
        moreInfo.setPosition((int) (0.025 * marketUI.worldWidth), marketUI.worldHeight - 100);
        stage.addActor(moreInfo);

    }

    // modifier functions
    public void changePrice(){
        // given information in good and cost text fields, attempt to change the corresponding cost in the market
        boolean costOK = false;
        int costValue = 0;
        String good = goodField.getText();
        String cost = costField.getText();

        // make sure the user entered value is an integer
        try{
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

    public void changeConsumption(){
        // given information in good and cost text fields, attempt to change the corresponding cost in the market
        boolean costOK = false;
        double consumptionValue = 0;
        String good = consumptionField.getText();
        String cost = consumptionCostField.getText();

        // make sure the user entered value is an integer
        try{
            consumptionValue = Double.parseDouble(cost);
            costOK = true;
        } catch (NumberFormatException e){
            errorLabel.setText("Not a valid consumption!");
        }
        // if value is ok, set the matching consumption of each agent to the new consumption value
        if (costOK){
            for (Agent a : market.getAgents()){
                a.getConsumption().get(good).setTickConsumption(consumptionValue);
            }
        }
    }

    public void changeAgent(){
        // given information in good and cost text fields, attempt to change the corresponding cost in the market
        String proposedAgentID = agentField.getText();

        // if value is ok, check goods for match and assign cost
        for (Agent a : market.getAgents()){
            if (a.getId().equals(proposedAgentID)){
                agentID = proposedAgentID;
                agentPropertyGraph.setTitle(proposedAgentID);
                agentPropertyGraph.updateGraphTitle();
            }
        }
    }


    // graph update functions
    public void updatePriceGraph(){
        // update function for the price graph, gets price data from market and then turns it into coordinates
        priceGraph.setFrame(frame);
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Price p : market.getPrices()){
            priceCoordinates.put(p.getGood(), (int) (p.getCost() * (priceGraph.getScale())));
        }
        priceGraph.setDataCoordinates(priceCoordinates);
        priceGraph.graphData();
        priceGraph.removeGraphDots(priceGraph.getX(), priceGraph.getDots());
        priceGraph.removeGraphLabels(priceGraph.getX(), priceGraph.getLabels());
    }

    public void updateProfessionGraph(){
        // update function for the profession graph, gets job data from market and then turns it into coordinates
        professionGraph.setFrame(frame);

        HashMap<String, Integer> jobsTotal = new HashMap<String, Integer>();
        // get total amount in each job by looping through all agents
        for (Agent a : market.getAgents()){
            if (!jobsTotal.containsKey(a.getProfession().getJob())){
                jobsTotal.put(a.getProfession().getJob(), 1);
            }
            else {
                String key = a.getProfession().getJob();
                jobsTotal.put(key, jobsTotal.get(key) + 1);
            }
        }
        // convert profession names to good names to match with color lookup
        HashMap<String, Integer> professionCoordinates = new HashMap<String, Integer>();
        for (Map.Entry<String, Integer> professionTotal : jobsTotal.entrySet()){
            for (JobOutput j : market.getJobOutputs()){
                if (j.getJob().equals(professionTotal.getKey())){
                    professionCoordinates.put(j.getGood(), (int) (professionTotal.getValue() *
                            (professionGraph.getScale())));
                }
            }
        }
        professionGraph.setDataCoordinates(professionCoordinates);
        professionGraph.graphData();
        professionGraph.removeGraphDots(professionGraph.getX(), professionGraph.getDots());
        professionGraph.removeGraphLabels(professionGraph.getX(), professionGraph.getLabels());
    }
    public void updateMoneyGraph(){
        // update function for the money graph, gets agent money data from market and then turns it into coordinates
        moneyGraph.setFrame(frame);

        int moneyTotal = 0;
        // get total funds of all agents
        for (Agent a : market.getAgents()) {
            moneyTotal += a.getMoney();
        }

        // convert profession names to good names to match with color lookup
        HashMap<String, Integer> professionCoordinates = new HashMap<String, Integer>();

        // generate non-zero coordinate:
        int moneyCoordinate = 0;
        if (((int) (moneyTotal * (moneyGraph.getScale()))) == 0){
            moneyCoordinate = 1;
        }
        else {
            moneyCoordinate = (int) (moneyTotal * (moneyGraph.getScale()));
        }
        // System.out.println(moneyCoordinate);
        professionCoordinates.put("MarketProperty", moneyCoordinate);

        moneyGraph.setDataCoordinates(professionCoordinates);
        moneyGraph.graphData();
        moneyGraph.removeGraphDots(moneyGraph.getX(), moneyGraph.getDots());
        moneyGraph.removeGraphLabels(moneyGraph.getX(), moneyGraph.getLabels());
    }

    public void updateUnmetNeedGraph(){
        // update function for the unmet need graph, gets unmet need data from each agent and then turns it into coordinates
        unmetNeedGraph.setFrame(frame);

        HashMap<String, Double> unmetNeedTotal = new HashMap<String, Double>();
        // update 0.2.7: reflecting removal of performance intensive individual unmet needs, just grab the new agent total
        for (Agent a : market.getAgents()){
            for (Map.Entry<String, Consumption> consumptionEntry : a.getConsumption().entrySet()) {
                if (!unmetNeedTotal.containsKey(consumptionEntry.getKey())){
                    unmetNeedTotal.put(consumptionEntry.getKey(), consumptionEntry.getValue().getTotalUnmetNeed());
                }
                else {
                    String key = consumptionEntry.getKey();
                    unmetNeedTotal.put(key, unmetNeedTotal.get(key) + consumptionEntry.getValue().getTotalUnmetNeed());
                }
            }
        }
        // set doubles to integers
        HashMap<String, Integer> professionCoordinates = new HashMap<String, Integer>();
        for (Map.Entry<String, Double> unmetEntryTotal : unmetNeedTotal.entrySet()){
            int coordinateValue = (int) (unmetEntryTotal.getValue() * (unmetNeedGraph.getScale()));
            professionCoordinates.put(unmetEntryTotal.getKey(), coordinateValue);
        }

        unmetNeedGraph.setDataCoordinates(professionCoordinates);
        unmetNeedGraph.graphData();
        unmetNeedGraph.removeGraphDots(unmetNeedGraph.getX(), unmetNeedGraph.getDots());
        unmetNeedGraph.removeGraphLabels(unmetNeedGraph.getX(), unmetNeedGraph.getLabels());
    }
    public void updateMarketInventoryGraph(){
        // update function for the price graph, gets price data from market and then turns it into coordinates
        marketInventoryGraph.setFrame(frame);
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Map.Entry<String, Double> good : market.getInventory().entrySet()){
            priceCoordinates.put(good.getKey(), (int) (good.getValue() * (marketInventoryGraph.getScale())));
        }
        marketInventoryGraph.setDataCoordinates(priceCoordinates);
        marketInventoryGraph.graphData();
        marketInventoryGraph.removeGraphDots(marketInventoryGraph.getX(), marketInventoryGraph.getDots());
        marketInventoryGraph.removeGraphLabels(marketInventoryGraph.getX(), marketInventoryGraph.getLabels());
    }

    public void updateAgentPropertyGraph(String agentID){
        // given the Agent ID, get properties about it for display
        // start with available money and profession
        // to start: get the agent
        agentPropertyGraph.setFrame(frame);
        MarketInfo fish = new MarketInfo("Fish", 0.35, 1, -0.4, 0.7,
                10, 1, "Fisherman", 1);
        ArrayList<MarketInfo> currentMarketProfile = new ArrayList<>();
        currentMarketProfile.add(fish);
        Agent chosenAgent = makeAgents(currentMarketProfile, 1).get(0);
        for (Agent a : market.getAgents()){
            if (a.getId().equals(agentID)){
                chosenAgent = a;
            }
        }
        // get properties of the chosen agent
        HashMap<String, Integer> agentDataCoordinates = new HashMap<>();

        // get money coordinate
        int moneyCoordinate = (int) (chosenAgent.getMoney() * (agentPropertyGraph.getScale()));
        agentDataCoordinates.put("MarketProperty", moneyCoordinate);

        // get profession information
        int professionCoordinate = (int) (agentPropertyGraph.getHeight() / 2);
        String profession = chosenAgent.getProfession().getJob();
        String professionGood = "MarketProperty";
        for (JobOutput j : market.getJobOutputs()){
            if (j.getJob().equals(profession)){
                professionGood = j.getGood();
            }
        }
        agentDataCoordinates.put(professionGood, professionCoordinate);
        // System.out.println(agentDataCoordinates);

        agentPropertyGraph.setDataCoordinates(agentDataCoordinates);
        agentPropertyGraph.graphData();
        agentPropertyGraph.removeGraphDots(agentPropertyGraph.getX(), agentPropertyGraph.getDots());
        agentPropertyGraph.removeGraphLabels(agentPropertyGraph.getX(), agentPropertyGraph.getLabels());
    }
    public Table createInstructions(){
        // create output table
        Table instructionsTable = new Table();

        // create instruction button
        Button instructionsButton = new TextButton(" Instructions ", firstSkin);
        instructionsButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Instructions:\n" + "Welcome to the MarketUI! MarketUI dynamically simulates " +
                        "a virtual marketplace with autonomous agents, which produce and consume goods and then buy " +
                        "and sell them to one another according to basic real world motivations of needs and a " +
                        "desire for profit. Governed by the real laws of microeconomics through supply and demand " +
                        "forces, MarketUI demonstrates the power of simple rules to create emergent behaviors akin " +
                        "to those found in the real world.\n" +
                        "This is the creation screen for the virtual market to be simulated. A market consists of a " +
                        "number of agents (button on the bottom of your screen) working with a number of goods. " +
                        "Click on the add good button, towards the middle of your screen, to add at least one good. " +
                        "When you do this, several boxes will appear with space to provide specific information " +
                        "about that good. Click on the information boxes associated with each to learn more about "+
                        "them. When you are done, click the create button in the bottom right to create the market " +
                        "and begin simulating it. Alternatively, click on a preset to use a prebuilt market and begin " +
                        "simulating right away. Thank you for using MarketUI!");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        instructionsTable.add(instructionsButton);

        return instructionsTable;
    }

    public VerticalGroup createBottomRow(){
        // create output group
        VerticalGroup bottomRow = new VerticalGroup();
        // create the two tables, one for the labels and info buttons, one for the actual
        Table bottomRowLabels = new Table();
        Table bottomRowInteractive = new Table();
        bottomRow.addActor(bottomRowLabels);
        bottomRow.addActor(bottomRowInteractive);

        // create field for number of Agents, prefilled to 2000
        Label numberOfAgents = new Label ("Number of Agents", firstSkin);
        bottomRowLabels.add(numberOfAgents).padLeft(35).padRight(5);
        // info button
        ImageButton numberOfAgentsButton = new ImageButton(infoIcon, infoIconClicked);
        numberOfAgentsButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Number of Agents:\n" + "This value is the number of agents present in the " +
                        "market simulation. A larger number of agents increases the detail of the simulation, at " +
                        "the cost of performance. Only a hundred or so are necessary for a fairly detailed " +
                        "simulation, though at least a thousand is better. At the moment, no multithreading " +
                        "is implemented, on an i7-8700k utilizing a single core, 2000 agents nears the upper " +
                        "performance limit.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        bottomRowLabels.add(numberOfAgentsButton).padRight(35);

        TextField numberOfAgentsField = new TextField("2000", firstSkin);
        bottomRowInteractive.add(numberOfAgentsField).padRight(50);

        // create first preset button
        Label preset1Label = new Label("Preset 1", firstSkin);
        bottomRowLabels.add(preset1Label).padLeft(20).padRight(5);
        // info button
        ImageButton preset1InfoButton = new ImageButton(infoIcon, infoIconClicked);
        preset1InfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Preset 1:\n" + "The first preset. Using a preset will start the simulation using " +
                        "one of a few predefined markets. Preset 1 is a balanced market of five goods: Fish, " +
                        "Lumber, Grain, Metal, and Brick, each of which is functionally identical with one another; " +
                        "i.e. they are all produced and consumed at the same rate and prioritized equally. There is a " +
                        "small surplus in the market, and production is distributed proportionally on initialization " +
                        "so that the market starts in a stable equilibrium. It is a great entry point, to get used to " +
                        "how the simulation works and get used to the different display and control options that " +
                        "are available. It is also an excellent platform to demonstrate the effects of modifying " +
                        "various market attributes with the modification tools in the main interface. Note: " +
                        "to respond better to differing performance requirements, presets still use a " +
                        "custom number of agents, as specified in the box to the left of the presets.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        bottomRowLabels.add(preset1InfoButton).padRight(30);

        Button preset1CreateButton = new TextButton("Use Preset", firstSkin);
        preset1CreateButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // preset values
                int numberOfGoods = 6;
                double surplusValue = 0.01;
                MarketInfo fish = new MarketInfo("Fish", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1, -1, 0,10, 1,
                        "Fisherman", 1.0 / numberOfGoods);
                MarketInfo lumber = new MarketInfo("Lumber", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1,-1, 0, 10, 1,
                        "Lumberjack", 1.0 / numberOfGoods);
                MarketInfo grain = new MarketInfo("Grain", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1, -1, 0, 10, 1,
                        "Farmer", 1.0 / numberOfGoods);
                MarketInfo metal = new MarketInfo("Metal", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1, -1, 0, 10, 1,
                        "Blacksmith", 1.0 / numberOfGoods);
                MarketInfo brick = new MarketInfo("Brick", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1, -1, 0, 10, 1,
                        "Mason", 1.0 / numberOfGoods);
                ArrayList<MarketInfo> preset1MarketProfile = new ArrayList<MarketInfo>();

                preset1MarketProfile.add(fish);
                preset1MarketProfile.add(lumber);
                preset1MarketProfile.add(grain);
                preset1MarketProfile.add(metal);
                preset1MarketProfile.add(brick);

                // overwrite current market profile
                currentMarketProfile = preset1MarketProfile;

                StringBuilder errorText = new StringBuilder();
                errorText.append("Error(s):\n");
                // get the number of agents
                VerticalGroup bottomRow = (VerticalGroup) masterTable.getChild(4);
                Table interactiveBottomRow = (Table) bottomRow.getChild(1);
                TextField numberOfAgentsField = (TextField) interactiveBottomRow.getChild(0);
                int numberOfAgents = 0;
                try{
                    numberOfAgents = Integer.parseInt(numberOfAgentsField.getText());
                    if (numberOfAgents < 0){
                        throw new IllegalArgumentException();
                    }
                } catch(NumberFormatException e){
                    errorText.append("The number of agents must be a number.\n");
                } catch(IllegalArgumentException e){
                    errorText.append("The number of agents must be positive.\n");
                }

                // if there were no errors (i.e. the error text is still its initial value), create the market,
                // otherwise, set the information box to show the encountered error
                if(errorText.toString().equals("Error(s):\n")){
                    // create a new main interface screen using the new market profile
                    marketUI.marketInterface = new MarketInterface(marketUI, numberOfAgents, currentMarketProfile);
                    // if a market did not exist before, refresh the main menu to include an enabled resume button
                    if (!marketUI.marketExists){
                        marketUI.marketExists = true;
                        marketUI.mainMenu = new MainMenu(marketUI);
                    }
                    // set the current screen to the main interface
                    marketUI.setScreen(marketUI.marketInterface);
                } else{
                    // otherwise, display the error text in the info box
                    infoLabel.setText(errorText.toString());
                }

            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        bottomRowInteractive.add(preset1CreateButton);



        return bottomRow;
    }

    public Table createCategoryLabels(){
        // create labels and information toggle buttons for the market info categories
        Table labels = new Table();

        // good name label
        Label name = new Label ("Good Name", firstSkin);
        labels.add(name).padLeft(35).padRight(5);
        // info button
        ImageButton nameInfoButton = new ImageButton(infoIcon, infoIconClicked);
        nameInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Good Name:\n" + "This is the name of the good, e.g. Lumber, Metal, or Fish.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(nameInfoButton).padRight(30);


        // good consumption label
        Label consumption = new Label ("Base Consumption", firstSkin);
        labels.add(consumption).padRight(5);
        // info button
        ImageButton consumptionInfoButton = new ImageButton(infoIcon, infoIconClicked);
        consumptionInfoButton.addListener(new InputListener(){
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
        labels.add(consumptionInfoButton).padRight(20);

        // good production label
        Label production = new Label ("Base Production", firstSkin);
        labels.add(production).padRight(5);
        // info button
        ImageButton productionInfoButton = new ImageButton(infoIcon, infoIconClicked);
        productionInfoButton.addListener(new InputListener(){
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
        labels.add(productionInfoButton).padRight(65);

        // price elasticity label
        Label priceElasticity = new Label ("Price Elasticities", firstSkin);
        labels.add(priceElasticity).padRight(5);
        // info button
        ImageButton priceElasticityInfoButton = new ImageButton(infoIcon, infoIconClicked);
        priceElasticityInfoButton.addListener(new InputListener(){
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
        labels.add(priceElasticityInfoButton).padRight(100);

        // good base cost label
        Label baseValues = new Label ("Base Values", firstSkin);
        labels.add(baseValues).padRight(5);
        // info button
        ImageButton baseValuesInfoButton = new ImageButton(infoIcon, infoIconClicked);
        baseValuesInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Base Cost and Weight:\n" +
                        "These values are multipliers which represents user defined offsets in the intrinsic " +
                        "worth assigned to a good. They cannot be negative.\n" +
                        "In practice, it is best to set all base values in a market to the " +
                        "same value. If they are different, the market has a very difficult time finding " +
                        "equilibrium, as agents will habitually prefer goods with higher base values without any" +
                        "kind of justification internal to the market.\n" +
                        "Base Cost is set with the entry box on the left, and Base Weight is set with the drop down " +
                        "box on the right, at present the only possible option is 1. Base cost is applied uniformly" +
                        "to offset the price of all goods, while the base weight sets the agents' buying priority" +
                        "multiplier.\n" +
                        "It is strongly recommended that base cost be set to 10 for all goods.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(baseValuesInfoButton).padRight(75);

        // good job name label
        Label jobName = new Label ("Job Name", firstSkin);
        labels.add(jobName).padRight(5);
        // info button
        ImageButton jobNameInfoButton = new ImageButton(infoIcon, infoIconClicked);
        jobNameInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Job Name:\n" +
                        "This is the name of the job or profession associated with producing this good, e.g. " +
                        "a fisherman produces fish and a farmer produces grain.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(jobNameInfoButton).padRight(75);

        // job chance label
        Label chance = new Label ("Job Chance", firstSkin);
        labels.add(chance).padRight(5);
        // info button
        ImageButton chanceInfoButton = new ImageButton(infoIcon, infoIconClicked);
        chanceInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Job Chance:\n" +
                        "This is the chance that an agent in the market will have the profession which " +
                        "produces the specified good at the start of the simulation. The system which makes this " +
                        "random choice works based upon the ratios between the given values and will therefore work " +
                        "for any number. However, to understand the choice it is making, it is best to make sure " +
                        "that the sum of all of these values is 1. In that case, the job chance can be read as the " +
                        "percent chance each agent will have the given profession.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        labels.add(chanceInfoButton).padRight(120);


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
        final Label elasticityDemandLabel = new Label("-5.00", firstSkin);
        // set fixed size so value change does not slightly resize the label
        // condensed layout:
        // good.add(elasticityDemandLabel).size(45, 20);
        // spacious layout:
        good.add(elasticityDemandLabel).size(45, 40);

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

        // get base good weight
        SelectBox<Integer> baseWeight = new SelectBox<>(firstSkin);
        Array<Integer> weightChoices = new Array<>();
        weightChoices.add(1);
        baseWeight.setItems(weightChoices);
        good.add(baseWeight).padRight(10);

        // get job name
        TextField jobName = new TextField("Job Name", firstSkin);
        good.add(jobName).padRight(10);

        // get job chance (between 0 and 1)
        final Label jobChanceLabel = new Label("0.00", firstSkin);
        // set fixed size so value change does not slightly resize the label
        // condensed layout:
        // good.add(jobChanceLabel).size(40, 20);
        // spacious layout:
        good.add(jobChanceLabel).size(40);


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
        Button remove = new TextButton("Remove", firstSkin);
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

}

