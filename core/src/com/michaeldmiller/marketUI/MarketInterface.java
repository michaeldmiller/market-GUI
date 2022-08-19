package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.michaeldmiller.economicagents.*;

import java.util.*;

import static com.michaeldmiller.economicagents.MarketMain.*;

public class MarketInterface implements Screen {
    final MarketUI marketUI;
    public Stage stage;
    Skin firstSkin;
    Market market;
    ArrayList<MarketInfo> currentMarketProfile;
    HashMap<String, Color> colorLookup;
    Label moreInfo;
    double scale;
    int frame;
    double secondFraction;
    int numberOfAgents;
    ScrollingGraph moneyGraph;
    ScrollingGraph marketInventoryGraph;
    String agentID;
    Drawable infoIcon;
    Drawable infoIconClicked;
    Table masterTable;
    VerticalGroup marketGoods;
    Label infoLabel;
    HashMap<Integer, ScrollingGraph> graphs;
    int numberOfGraphsSelectedByUser;
    boolean isPaused;
    SelectBox<Integer> numberOfGraphsSelector;
    VerticalGroup typeSelectors;
    SelectBox<String> modificationSelector;
    TextField goodField;
    TextField modificationField;
    TextField agentField;



    public MarketInterface (final MarketUI marketUI, int specifiedNumberOfAgents,
                          ArrayList<MarketInfo> specifiedMarketProfile) {
        this.marketUI = marketUI;
        this.isPaused = true;
        this.graphs = new HashMap<>();
        numberOfGraphsSelectedByUser = 0;
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
        // masterTable.setDebug(true);

        // create labels
        Label title = new Label("Market Interface", firstSkin);

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
        titleInstructions.add(title).padLeft(70);
        titleInstructions.add().expand();
        titleInstructions.add(createInstructions()).width(125);
        masterTable.add(titleInstructions).align(Align.left).fill();

        // create table for pause and menu buttons
        Table pauseMenu = new Table();
        pauseMenu.add(createPauseResumeButton()).width(125);
        pauseMenu.add(menuButton).top().right().width(125);
        masterTable.add(pauseMenu);
        masterTable.row();

        // create vertical group, with one or two rows, each containing a horizontal group, for the graphs
        marketGoods = new VerticalGroup();
        marketGoods.align(Align.left);

        HorizontalGroup graphRow1 = new HorizontalGroup();

        marketGoods.setSize(2000, 400);

        marketGoods.addActor(graphRow1);
        marketGoods.expand();

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

        // information panel: graph selectors and information label
        Table informationPanel = createInformationPanel();
        masterTable.add(informationPanel).fill().center().top();

        // add bottom row
        masterTable.row();
        // create display panel
        // left align
        // masterTable.add(createDisplayPanel()).align(Align.left).top().padLeft(10);
        // center align
        masterTable.add(createDisplayPanel()).top();

        // create change agent area
        Table changeAgentTable = createChangeAgentTable();
        masterTable.add(changeAgentTable);

        // instantiate the market
        instantiateMarket();

    }


    private void createGraph(int index, String graphType){
        // determine if graph already exists with that index
        if (graphs.containsKey(index)){
            // if it does, get it and delete it.
            ScrollingGraph oldGraph = graphs.get(index);
            // delete the graph components, and then remove the graph from the graphs list
            oldGraph.deleteAll(oldGraph.getDots(), oldGraph.getLabels(), oldGraph.getOtherComponents());
            graphs.remove(index);
        }

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

        // determine x and y coordinates
        int xCoordinate;
        if (index == 0 || index == 1){
            xCoordinate = 70;
        } else{
            xCoordinate = 710;
        }
        int yCoordinate;
        if(index == 0 || index == 2){
            yCoordinate = 480;
        } else{
            yCoordinate = 100;
        }
        // determine width and height
        int width;
        int height;
        // use number of possible graphs selected, not the current graph number
        if (numberOfGraphsSelectedByUser == 1){
            width = 1210;
            height = 715;
            yCoordinate = 100;
        } else if (numberOfGraphsSelectedByUser == 2){
            width = 1210;
            height = 335;
        } else if (numberOfGraphsSelectedByUser == 3){
            // if it is graph 2, take up whole screen length wise, otherwise take up half the screen
            if (index == 1){
                width = 1210;
            }
            else {
                width = 570;
            }
            height = 335;
        } else {
            width = 570;
            height = 335;
        }

        // create the appropriate graph
        // list of graph types
        if (graphType.equals("Prices")){
            PriceGraph priceGraph = new PriceGraph(xCoordinate, yCoordinate, width, height,
                    marketUI.worldWidth, marketUI.worldHeight, scale, "Prices",
                    new HashMap<String, Integer>(), colorLookup, firstSkin, frame, stage, true);
            priceGraph.makeGraph();
            graphs.put(index, priceGraph);

        } else if (graphType.equals("Producers")){
            ProfessionGraph professionGraph = new ProfessionGraph(xCoordinate, yCoordinate, width, height,
            marketUI.worldWidth, marketUI.worldHeight, 500.0 / numberOfAgents, "# of Producers",
                    new HashMap<String, Integer>(), colorLookup, firstSkin, frame, stage, true);
            professionGraph.makeGraph();
            graphs.put(index, professionGraph);

        } else if (graphType.equals("Agent")){
            AgentPropertyGraph agentGraph = new AgentPropertyGraph(xCoordinate, yCoordinate, width, height,
                    marketUI.worldWidth, marketUI.worldHeight, scale * 2, "Agent: " + agentID,
                    new HashMap<String, Integer>(), colorLookup, firstSkin, frame, stage, true);
            agentGraph.makeGraph();
            graphs.put(index, agentGraph);

        } else if (graphType.equals("Unmet Needs")){
            TotalUnmetNeedsGraph totalUnmetNeedsGraph = new TotalUnmetNeedsGraph(xCoordinate, yCoordinate, width, height,
                    marketUI.worldWidth, marketUI.worldHeight, 0.01, "Unmet Needs",
                    new HashMap<String, Integer>(), colorLookup, firstSkin, frame, stage, true);
            totalUnmetNeedsGraph.makeGraph();
            graphs.put(index, totalUnmetNeedsGraph);

        } else if (graphType.equals("Priorities")){
            PriorityGraph totalGoodPurchasePriorityGraph = new PriorityGraph(xCoordinate, yCoordinate, width, height,
                    marketUI.worldWidth, marketUI.worldHeight, 0.001, "Total Purchase Priorities",
                    new HashMap<String, Integer>(), colorLookup, firstSkin, frame, stage, true);
            totalGoodPurchasePriorityGraph.makeGraph();
            graphs.put(index, totalGoodPurchasePriorityGraph);

        }

    }

    private void redrawGraphs(){
        // delete all the graphs
        for (Iterator<Map.Entry<Integer, ScrollingGraph>> iterator = graphs.entrySet().iterator(); iterator.hasNext();){
            Map.Entry<Integer, ScrollingGraph> graphForDeletion = iterator.next();
            ScrollingGraph oldGraph = graphForDeletion.getValue();
            // delete the graph components, and then remove the graph from the graphs list
            oldGraph.deleteAll(oldGraph.getDots(), oldGraph.getLabels(), oldGraph.getOtherComponents());
            iterator.remove();
        }

        // redraw all graphs using the graph creation logic
        // go to table containing the graph selectors
        // located at the fourth cell of the master table, third child of the infobox. Each table contained within
        // has a label and a select box. Run create graph on the index and the value of the select box
        Table InfoPanelCell = (Table) masterTable.getChild(3);
        VerticalGroup graphTypeSelectors = (VerticalGroup) InfoPanelCell.getChild(2);
        int index = 0;
        for (Actor tableActor : graphTypeSelectors.getChildren()){
            // cast to table
            Table table = (Table) tableActor;
            // get select box at index 1. Despite complaints, this is a select box of strings.
            SelectBox<String> selectBox = (SelectBox<String>) table.getChild(1);
            // create graph and increment index
            createGraph(index, selectBox.getSelected());
            index++;
        }

    }
    private Table createInformationPanel(){
        Table informationPanel = new Table();

        Table numberOfGraphsSelectionTable = new Table();
        // create options
        Label numberOfGraphsPrompt = new Label("Number of Graphs:", firstSkin);
        numberOfGraphsSelectionTable.add(numberOfGraphsPrompt).padRight(5);

        // create number of graphs selector
        numberOfGraphsSelector = createNumberOfGraphsSelector();
        numberOfGraphsSelectionTable.add(numberOfGraphsSelector).padRight(5);

        // create number of graphs info button
        ImageButton numberOfGraphsInfoButton = new ImageButton(infoIcon, infoIconClicked);
        numberOfGraphsInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Number of Graphs:\n\n" + "Use this selector to determine how many graphs " +
                        "you would like to have in the main display area of the interface. Changing the number " +
                        "of graphs will alter the layout of all graphs and redraw them. There are four possible " +
                        "slots. Slot 1 is in the top left corner, Slot 2 is the in the bottom left corner, " +
                        "Slot 3 is in the top right corner, and Slot 4 is in the bottom right corner. ");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        numberOfGraphsSelectionTable.add(numberOfGraphsInfoButton);
        informationPanel.add(numberOfGraphsSelectionTable).padTop(5).top();
        informationPanel.row();

        // type label
        Table typePromptLabelTable = new Table();
        Label typePromptLabel = new Label("Graph Type", firstSkin);
        typePromptLabelTable.add(typePromptLabel).padLeft(50).padRight(37);
        // create graph type info button
        ImageButton graphTypeInfoButton = new ImageButton(infoIcon, infoIconClicked);
        graphTypeInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Graph Type:\n\n" + "Use these selectors to determine the type of graph you " +
                        "would like to have in each graph slot in the main display area. There are five graph " +
                        "types. There are five graph types, as follows:\n\nThe first graph type is of the prices of " +
                        "each good in the market, which will be stable when the market is in equilibrium.\n\nThe second " +
                        "graph type is of the total number of agents producing each type of good. These values will " +
                        "be generally stable but experience small fluctuations when the market is in equilibrium.\n\n" +
                        "The third graph type is of the current agent, specifically the current weights it experienced " +
                        "in favor of each good when making its purchasing decision that tick. It also displays a " +
                        "constant straight line in the middle of the graph. The color of this line corresponds to " +
                        "the agent's current profession and the good it produces.\n\nThe fourth graph type is of the " +
                        "total unmet needs for each good accumulated by each agent in the market. This is essentially " +
                        "how much of each good they remember not being able to consume when they needed to. Agents " +
                        "will purchase variable quantities of their selected good when they choose to buy something " +
                        "to attempt to draw down these unmet needs. These cumulative values will be low and stable " +
                        "when the market is in equilibrium.\n\nThe last graph type is of total good purchase priorities. " +
                        "This shows the sum of all weights felt by agents in favor of purchasing a particular good " +
                        "that tick. It is an aggregation of the values displayed at the individual level in the agent " +
                        "graph, and shows trends in the relative importance of a good in the market.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        typePromptLabelTable.add(graphTypeInfoButton);
        informationPanel.add(typePromptLabelTable);
        informationPanel.row();

        // add group for type selectors
        typeSelectors = new VerticalGroup();
        informationPanel.add(typeSelectors);
        informationPanel.row();

        // info box, add information label
        // create scroll pane
        infoLabel.setAlignment(Align.top, Align.left);
        final ScrollPane informationScrollPane = new ScrollPane(infoLabel);
        informationPanel.add(informationScrollPane).width(250).expandY().top();
        informationPanel.row();

        // add modifications
        VerticalGroup modificationsGroup = createModificationsGroup();
        informationPanel.add(modificationsGroup).padTop(5).padBottom(42);
        informationPanel.row();

        // return the information panel table
        return informationPanel;
    }

    private Table createChangeAgentTable(){
        Table changeAgentTable = new Table();
        Table changeAgentLabelTable = new Table();
        Label changeAgentLabel = new Label("Modify Agent ID", firstSkin);
        changeAgentLabelTable.add(changeAgentLabel);
        ImageButton changeAgentInfoButton = new ImageButton(infoIcon, infoIconClicked);
        changeAgentInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Modify Agent ID:\n\n" + "Enter the ID of an agent to change the focus of " +
                        "the simulation graph(s) and current agent display button to that agent. When a new " +
                        "agent is selected, any agent graphs will be redrawn.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        changeAgentLabelTable.add(changeAgentInfoButton).padLeft(5);
        changeAgentTable.add(changeAgentLabelTable).padBottom(1);
        changeAgentTable.row();
        // modification field and button
        Table changeAgentModificationTable = new Table();
        agentField = new TextField("Agent ID", firstSkin);
        Button changeAgentButton = createChangeAgentButton();
        changeAgentModificationTable.add(agentField).padRight(10);
        changeAgentModificationTable.add(changeAgentButton);
        changeAgentTable.add(changeAgentModificationTable).padBottom(4);
        // return change agent table
        return changeAgentTable;

    }

    private VerticalGroup createModificationsGroup(){
        VerticalGroup modificationsGroup = new VerticalGroup();
        Table modificationsLabelTable = new Table();
        Label marketModificationsLabel = new Label("Market Modifications", firstSkin);
        modificationsLabelTable.add(marketModificationsLabel).padRight(5);
        ImageButton modificationsInfoButton = new ImageButton(infoIcon, infoIconClicked);
        modificationsInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Market Modifications:\n\n" + "The items below this info button, a selector and " +
                        "two text entry fields, allow for live modification of the market. To make a modification, " +
                        "select the modification type in the selector box, enter the the good to which you would " +
                        "like to apply the modification in the top box, then enter the new value in the lower box. " +
                        "There are five modifications available, all of which affect a property of a good in the " +
                        "market.\n\nThe first modification is to the base consumption. Modifying this value for a " +
                        "particular good will change the per tick consumption of the good for each agent to the new " +
                        "value. It is useful for quickly and simply bringing the market in and out of equilibrium. " +
                        "The second modification is to base production. Modifying this value will change the " +
                        "base production rate for agents producing this good, i.e. changing the base rate from " +
                        "one to two would double the per tick output of the good for each agent. The third " +
                        "modification is to the price elasticity of demand. Changing this value for a good " +
                        "alters each agents base sensitivity to price changes when deciding whether to buy " +
                        "that good.\n\nThe last two modifications are more likely to have unintended consequences. " +
                        "The first of those, the fourth modification overall, is price elasticity of supply. " +
                        "Changing this alters the sensitivity of agents to price when deciding the amount of " +
                        "a good they want to produce. As all agents currently can only produce the maximum amount " +
                        "possible every tick (fractional production capacity tended to interfere with finding long run " +
                        "production equilibrium), changing this value does not make sense in the context of the " +
                        "market. However, it can be used to change the value of price equilibrium without affecting " +
                        "consumption. The second of these modifications likely to have unintended consequences, the " +
                        "fifth modification overall, is base cost. This modification essentially applies an outside " +
                        "weight to the importance of a good in a market. It is usually set to ten, to improve scaling " +
                        "and make differences more visible in graphing and is best used to change the graphing scale " +
                        "on the fly, if detail is being lost because of values being to large or too small to see. " +
                        "If the values are set unequally for the goods, however, agents will habitually prefer " +
                        "the good or goods that have the higher base costs, without logical explanation within the " +
                        "market. This greatly inhibits or outright prevents the simulation from finding a stable " +
                        "price and production equilibrium that is suitable for all agents. ");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        modificationsLabelTable.add(modificationsInfoButton);
        modificationsGroup.addActor(modificationsLabelTable);

        // add modification selector
        modificationSelector = new SelectBox<>(firstSkin);
        Array<String> modificationChoices = new Array<>();
        modificationChoices.add("Base Consumption");
        modificationChoices.add("Base Production");
        modificationChoices.add("Price Elasticity of Demand");
        modificationChoices.add("Price Elasticity of Supply");
        modificationChoices.add("Base Cost");
        modificationSelector.setItems(modificationChoices);
        modificationsGroup.addActor(modificationSelector);
        // add good and new value entry fields
        goodField = new TextField("Good", firstSkin);
        modificationsGroup.addActor(goodField);
        // add modification value entry box
        modificationField = new TextField("New Value", firstSkin);
        modificationsGroup.addActor(modificationField);
        // create modification button
        modificationsGroup.addActor(createModifyButton());

        // return modifications group
        return modificationsGroup;
    }

    private Button createPauseResumeButton(){
        // given the current state, create a button with the opposite text, i.e. given a resume button, create
        // a pause button, and create a resume button given a pause button
        Button returnButton = new TextButton("Resume", firstSkin);
        // set the button to flip the boolean paused value when pressed
        returnButton.addListener(new InputListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                isPaused = !isPaused;
                // reset text, if text is pause, set to resume, and vice versa
                Actor buttonActor = event.getListenerActor();
                TextButton textButton = (TextButton) buttonActor;
                String buttonText = String.valueOf(textButton.getText());
                if (buttonText.equals("Resume")){
                    textButton.setText("Pause");
                } else {
                    textButton.setText("Resume");
                }
            }
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        return returnButton;
    }

    private Button createChangeAgentButton(){
        Button changeAgent = new TextButton("Set", firstSkin);
        changeAgent.addListener(new InputListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                // attempt to modify the agent
                String agentID = agentField.getText();
                changeAgent(agentID);

            }
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        return changeAgent;

    }

    private SelectBox<Integer> createNumberOfGraphsSelector(){
        SelectBox<Integer> numberOfGraphsSelectBox = new SelectBox<>(firstSkin);
        Array<Integer> graphChoices = new Array<>();
        graphChoices.add(0);
        graphChoices.add(1);
        graphChoices.add(2);
        graphChoices.add(3);
        graphChoices.add(4);
        numberOfGraphsSelectBox.setItems(graphChoices);

        numberOfGraphsSelectBox.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                // change selected number of graphs
                numberOfGraphsSelectedByUser = numberOfGraphsSelector.getSelected();
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
                        final SelectBox<String> graphTypeSelector = new SelectBox<>(firstSkin);
                        Array<String> graphTypes = new Array<>();
                        graphTypes.add("");
                        graphTypes.add("Prices");
                        graphTypes.add("Producers");
                        graphTypes.add("Agent");
                        graphTypes.add("Unmet Needs");
                        graphTypes.add("Priorities");
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
                                // createGraph(index,
                                createGraph(location, graphTypeSelector.getSelected());

                            }
                        });
                        graphTypeSelectorTable.add(graphTypeSelector);

                        typeSelectors.addActor(graphTypeSelectorTable);
                    }

                }
                // if the number has been changed, redraw all the graphs
                redrawGraphs();
            }
        });

        return numberOfGraphsSelectBox;
    }

    private Button createModifyButton(){
        // create the modification button, which takes the modification category from the selection box, and the
        // new values from the appropriate user entry points (agent text entry field and/or main text entry field)
        // then determines which modification function to call and calls it

        Button modifyButton = new TextButton("Modify", firstSkin);
        modifyButton.addListener(new InputListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                // get modification type
                String modificationType = modificationSelector.getSelected();
                String goodType = goodField.getText();
                String value = modificationField.getText();

                // type requirement lists
                // all retroactive market setup modifications have to be doubles, all must be positive except for price
                // elasticity of demand, which must be negative. None of them can be 0.
                ArrayList<String> needGood = new ArrayList<>(Arrays.asList("Base Consumption", "Base Production",
                        "Price Elasticity of Demand", "Price Elasticity of Supply", "Base Cost"));
                ArrayList<String> mustBeDouble = new ArrayList<>(Arrays.asList("Base Consumption", "Base Production",
                        "Price Elasticity of Demand", "Price Elasticity of Supply", "Base Cost"));

                ArrayList<String> mustBePositive = new ArrayList<>(Arrays.asList("Base Consumption", "Base Production",
                        "Price Elasticity of Supply", "Base Cost"));

                ArrayList<String> mustBeNegative = new ArrayList<>(Collections.singletonList("Price Elasticity of Demand"));

                // use pooled error checking based on modification type's presence in the above lists
                boolean valueOK = true;
                double valueAsDouble = 0;

                // if a good is needed, make sure a valid good has been entered
                if (needGood.contains(modificationType)){
                    boolean goodFound = false;
                    for (MarketInfo marketInfo : market.getMarketProfile()){
                        if (marketInfo.getGood().equals(goodType)){
                            goodFound = true;
                            break;
                        }
                    }
                    if (!goodFound){
                        // if the good is invalid, set valueOK flag to false, print error message
                        infoLabel.setText("Error:\nThe good name must match the name of a good currently in the market.");
                        valueOK = false;
                    }
                }
                // for all subsequent checks, do not perform them if the value has already failed another test.
                // check double type cast validity
                if (mustBeDouble.contains(modificationType) && valueOK){
                    // if the type must be processed as a double, attempt to parse it
                    try{
                        valueAsDouble = Double.parseDouble(value);
                    } catch (NumberFormatException e){
                        infoLabel.setText("Error:\nFor this modification, the new value must be a number.");
                        valueOK = false;
                    }
                }
                // check if number must be positive
                if (mustBePositive.contains(modificationType) && valueOK){
                    if (valueAsDouble <= 0) {
                        infoLabel.setText("Error:\nFor this modification, the new value must be positive.");
                        valueOK = false;
                    }

                }
                // check if value must be negative
                if (mustBeNegative.contains(modificationType) && valueOK) {
                    if (valueAsDouble >= 0) {
                        infoLabel.setText("Error:\nFor this modification, the new value must be negative.");
                        valueOK = false;
                    }
                }

                // before handing off to modification functions, check that the value is ok
                if (valueOK){
                    // determine the modification type, call the corresponding function
                    if (modificationType.equals("Base Consumption")){
                        changeConsumption(goodType, valueAsDouble);
                    }
                    else if (modificationType.equals("Base Production")){
                        changeProduction(goodType, valueAsDouble);
                    }
                    else if (modificationType.equals("Price Elasticity of Demand")){
                        changePriceElasticityOfDemand(goodType, valueAsDouble);
                    }
                    else if (modificationType.equals("Price Elasticity of Supply")){
                        changePriceElasticityOfSupply(goodType, valueAsDouble);
                    }
                    else if (modificationType.equals("Base Cost")){
                        changeBaseCost(goodType, valueAsDouble);
                    }
                    // show confirmation message
                    infoLabel.setText(modificationType + " successfully set to " + value + "." + "\n" +
                            market.getMarketProfile());
                }

            }
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        return modifyButton;
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
    private void changeConsumption(String good, double consumptionValue){
        // change the value in the market profile
        for (MarketInfo marketInfo : market.getMarketProfile()){
            if (marketInfo.getGood().equals(good)){
                marketInfo.setBaseConsumption(consumptionValue);
                break;
            }
        }
        // loop through each agent in the market, change the consumption of the specified good to its new value
        for (Agent a : market.getAgents()){
            a.getConsumption().get(good).setTickConsumption(consumptionValue);
        }
    }

    private void changeProduction(String good, double productionValue){
        // find the profession name associated with the good
        String professionName = "";
        for (MarketInfo marketInfo : market.getMarketProfile()){
            if (marketInfo.getGood().equals(good)){
                // get the profession name
                professionName = marketInfo.getJobName();
                // change the value in the market profile, so that agents that switch into this profession in the
                // future will use the new value
                marketInfo.setBaseProduction(productionValue);
                break;
            }
        }

        // loop through each agent in the market, change the base production of the specified good for all agents which
        // produce this good in their profession
        for (Agent a : market.getAgents()){
            if (a.getProfession().getJob().equals(professionName)){
                a.getProfession().setBaseProduction(productionValue);
            }
        }

    }

    private void changePriceElasticityOfDemand(String good, double elasticityOfDemandValue){
        // change the value in the market profile
        for (MarketInfo marketInfo : market.getMarketProfile()){
            if (marketInfo.getGood().equals(good)){
                marketInfo.setPriceElasticityDemand(elasticityOfDemandValue);
                break;
            }
        }
        // loop through each agent in the market, change the price elasticity of demand of the specified good
        // to its new value
        for (Agent a : market.getAgents()){
            // find matching priority
            for (Priority p : a.getPriorities()){
                if (p.getGood().equals(good)){
                    p.setPriceElasticity(elasticityOfDemandValue);
                }
            }
        }
    }

    private void changePriceElasticityOfSupply(String good, double elasticityOfSupplyValue){
        // find the profession name associated with the good
        String professionName = "";
        for (MarketInfo marketInfo : market.getMarketProfile()){
            if (marketInfo.getGood().equals(good)){
                // get the profession name
                professionName = marketInfo.getJobName();
                // change the value in the market profile, so that agents that switch into this profession in the
                // future will use the new value
                marketInfo.setPriceElasticitySupply(elasticityOfSupplyValue);
                break;
            }
        }

        // loop through each agent in the market, change the base production of the specified good for all agents which
        // produce this good in their profession
        for (Agent a : market.getAgents()){
            if (a.getProfession().getJob().equals(professionName)){
                a.getProfession().setPriceElasticityOfSupply(elasticityOfSupplyValue);
            }
        }

    }

    private void changeBaseCost(String good, double baseCostValue){
        for (MarketInfo marketInfo : market.getMarketProfile()){
            if (marketInfo.getGood().equals(good)){
                // change the value in the market profile
                marketInfo.setGoodCost(baseCostValue);
                break;
            }
        }
        // change corresponding base cost of the matching good entry in the market prices
        for (Price p : market.getPrices()){
            if (p.getGood().equals(good)){
                p.setOriginalCost(baseCostValue);
            }
        }

    }

    public void changeAgent(String proposedAgentID){
        // check to see if the new value is different from the old one
        if (!proposedAgentID.equals(agentID)){
            // given a proposed agentID, check the market for a match, if one exists, reassign the agentID field
            // for the market (i.e. the agent selected for display). If no match exists, display an error text in the
            // info label, otherwise, display a confirmation message.

            boolean matchFound = false;

            for (Agent a : market.getAgents()){
                if (a.getId().equals(proposedAgentID)){
                    agentID = proposedAgentID;
                    matchFound = true;
                    // tricky part: find any graphs which mention the agent and recreate them
                    // separating into two loops to avoid error
                    ArrayList<Integer> graphsNeedingUpdate = new ArrayList<>();
                    for (Map.Entry<Integer, ScrollingGraph> graphEntry : graphs.entrySet()){
                        if (graphEntry.getValue() instanceof AgentPropertyGraph){
                            graphsNeedingUpdate.add(graphEntry.getKey());
                        }
                    }
                    for (Integer i : graphsNeedingUpdate){
                        createGraph(i, "Agent");
                    }
                    break;
                }
            }
            if (!matchFound){
                infoLabel.setText("That is not a valid agent ID. Please try again.");
            } else{
                infoLabel.setText("Agent ID modified to: " + agentID + ".");
            }

        } else{
            infoLabel.setText("That is already the current agent ID.");
        }


    }

    // graph update functions
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

    public Button createInstructions(){
        // create instruction button
        Button instructionsButton = new TextButton(" Instructions ", firstSkin);
        instructionsButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Instructions:\n\n" +
                        "This is the market interface screen for the virtual market to be simulated. Having created " +
                        "a graph on the previous screen, or selected a preset, all the information is now available " +
                        "to begin your simulation!\n\nThe simulation begins paused, this is controlled by the pause/resume " +
                        "button at the top of your screen next to the instructions and menu buttons. Before beginning, " +
                        "the simulation by clicking resume, go to the number of graphs selector just below the menu " +
                        "button and select how many graphs you would like to display, and the type of those graphs. " +
                        "Further details about this can be found by clicking their information buttons. After you are " +
                        "satisfied, click resume to start the simulation. You can click this button again at any time " +
                        "to pause the simulation.\n\nDuring the simulation, if you would like to know more about a " +
                        "particular aspect or value in the market, navigate to the display panel at the bottom " +
                        "of your screen. A variety of different information and attributes about the market " +
                        "can be displayed with these buttons. More details can be found in the information buttons " +
                        "in the display panel.\n\nIf you would like to modify something in the market during the" +
                        "simulation, navigate to the modifications and change agent areas on the bottom right of " +
                        "your screen. The former allows you to modify several market attributes; instructions on how " +
                        "to do so can be found in the information button there. Finally, you can change the agent of " +
                        "focus using the change agent area, instructions to do so are likewise found in that area's " +
                        "information button. Enjoy your simulation! Thank you for using MarketUI.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });

        return instructionsButton;
    }

    public Table createDisplayPanel(){
        // create output table
        Table displayPanel = new Table();
        // two rows, many columns. Top row for Label and info button, bottom for button, or selector and button

        Label displayOptions = new Label("Display", firstSkin);
        displayPanel.add(displayOptions).bottom();

        // display prices
        Table priceDisplayTable = new Table();
        Label displayCurrentPrices = new Label("Price Information", firstSkin);
        priceDisplayTable.add(displayCurrentPrices).padLeft(20).padRight(5);
        // info button
        ImageButton pricesInfoButton = new ImageButton(infoIcon, infoIconClicked);
        pricesInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Price Information:\n\n" + "This displays the full details of the prices " +
                        "currently experienced in the market, including the equilibrium and original " +
                        "cost values used in calculations.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        priceDisplayTable.add(pricesInfoButton).padRight(15);
        displayPanel.add(priceDisplayTable);

        // display number of producers
        Table totalProducersDisplayTable = new Table();
        Label displayTotalProducers = new Label("Total Producers", firstSkin);
        totalProducersDisplayTable.add(displayTotalProducers);
        // info button
        ImageButton totalProducersInfoButton = new ImageButton(infoIcon, infoIconClicked);
        totalProducersInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Total Producers:\n\n" + "This displays the total numbers of producers for each " +
                        "type of good.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        totalProducersDisplayTable.add(totalProducersInfoButton).padLeft(5).padRight(15);
        displayPanel.add(totalProducersDisplayTable);


        // display current agent
        Table currentAgentDisplayTable = new Table();
        Label displayCurrentAgent = new Label("Current Agent", firstSkin);
        currentAgentDisplayTable.add(displayCurrentAgent);
        // info button
        ImageButton currentAgentInfoButton = new ImageButton(infoIcon, infoIconClicked);
        currentAgentInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Current Agent Information:\n\n" + "This displays the properties of the current " +
                        "agent. To view a different agent, change the current active agent using the change agent " +
                        "button in the lower right.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        currentAgentDisplayTable.add(currentAgentInfoButton).padLeft(5).padRight(15);
        displayPanel.add(currentAgentDisplayTable);

        // display total unmet needs
        Table totalUnmetNeedsDisplayTable = new Table();
        Label displayTotalUnmetNeeds = new Label("Unmet Needs", firstSkin);
        totalUnmetNeedsDisplayTable.add(displayTotalUnmetNeeds);
        // info button
        ImageButton unmetNeedsInfoButton = new ImageButton(infoIcon, infoIconClicked);
        unmetNeedsInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Total Unmet Needs:\n\n" + "This displays the total amount of unmet needs for " +
                        "each good held by the agents in the market. Through its effects on individual price " +
                        "elasticities and the market demand curve, this is a key component in determining the " +
                        "price of each good.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        totalUnmetNeedsDisplayTable.add(unmetNeedsInfoButton).padLeft(5).padRight(15);
        displayPanel.add(totalUnmetNeedsDisplayTable);

        // display total priority weights
        Table totalPriorityWeightsTable = new Table();
        Label displayTotalPriorityWeights = new Label("Priority Weights", firstSkin);
        totalPriorityWeightsTable.add(displayTotalPriorityWeights);
        // info button
        ImageButton priorityWeightsInfoButton = new ImageButton(infoIcon, infoIconClicked);
        priorityWeightsInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Total Priority Weights:\n\n" + "This displays the sum of all of the weight values " +
                        "assigned to each good by each agent in this tick, when the agents were determining which " +
                        "good they would try to buy. These weights informed a weighted choice system that picked" +
                        "the chosen good (i.e. if a the weight was 80 for Good A and 20 for Good B, and Goods A " +
                        "and B were the only choices, the agent would have an 80% chance of buying Good A and a 20%" +
                        "chance of buying Good B.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        totalPriorityWeightsTable.add(priorityWeightsInfoButton).padLeft(5).padRight(15);
        displayPanel.add(totalPriorityWeightsTable);


        // display market profile information
        Table marketProfileDisplayTable = new Table();
        Label displayMarketProfileInfo = new Label("Market Profile", firstSkin);
        marketProfileDisplayTable.add(displayMarketProfileInfo);
        // info button
        ImageButton marketProfileInfoButton = new ImageButton(infoIcon, infoIconClicked);
        marketProfileInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Market Profile:\n\n" + "This displays a list of the basic information about " +
                        "the goods used currently in the market, particularly things like their base costs, " +
                        "elasticities, job and profession names, as well as a list of Agent IDs. It does not display, " +
                        "the full details of all the agents in the market, as this is a huge volume of text which " +
                        "causes a great deal of lag in the simulation.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        marketProfileDisplayTable.add(marketProfileInfoButton).padLeft(5).padRight(15);
        displayPanel.add(marketProfileDisplayTable);

        // display market goods information
        Table marketPropertiesDisplayTable = new Table();
        Label displayMarketPropertyInfo = new Label("Market Properties", firstSkin);
        marketPropertiesDisplayTable.add(displayMarketPropertyInfo);
        // info button
        ImageButton marketPropertiesInfoButton = new ImageButton(infoIcon, infoIconClicked);
        marketPropertiesInfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Market Properties:\n\n" + "This displays the current inventory, job output combinations," +
                        "and prices currently in the market. It does not display the entire market, as this is a " +
                        "huge volume of text which causes a great deal of lag in the simulation.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        marketPropertiesDisplayTable.add(marketPropertiesInfoButton).padLeft(5).padRight(15);
        displayPanel.add(marketPropertiesDisplayTable);
        // consumptions sum, priorities sum
        // order: prices, producers sum, current agent, unmet need sum, consumptions sum, priorities sum, market goods, market properties.

        // advance to button row
        displayPanel.row();
        // add lower part of panel label
        Label displayPanelLowerLabel = new Label("Panel", firstSkin);
        displayPanel.add(displayPanelLowerLabel).top();

        // prices display button
        TextButton pricesDisplayButton = new TextButton("Display", firstSkin);
        pricesDisplayButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // find prices and change information box text to description
                infoLabel.setText("Market Prices: " + market.getPrices());

            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        displayPanel.add(pricesDisplayButton);

        // total producers display button
        TextButton totalProducersDisplayButton = new TextButton("Display", firstSkin);
        totalProducersDisplayButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // find total number of producers and change information box text to description
                // make hash map of total producers
                HashMap<String, Integer> jobsTotal = new HashMap<String, Integer>();
                for (Agent a : market.getAgents()){
                    if (!jobsTotal.containsKey(a.getProfession().getJob())){
                        jobsTotal.put(a.getProfession().getJob(), 1);
                    }
                    else {
                        String key = a.getProfession().getJob();
                        jobsTotal.put(key, jobsTotal.get(key) + 1);
                    }
                }
                StringBuilder outputString = new StringBuilder();
                // pair the producers with goods and assemble into output string
                for (Map.Entry<String, Integer> jobQuantity : jobsTotal.entrySet()){
                    for (JobOutput jobLookup : market.getJobOutputs()){
                        if (jobLookup.getJob().equals(jobQuantity.getKey())){
                            // add leading new line
                            outputString.append("\n\n");
                            // use correct plural
                            if (jobQuantity.getValue() > 1){
                                outputString.append(String.format("%s: %d %ss", jobLookup.getGood(), jobQuantity.getValue(), jobQuantity.getKey()));
                            } else{
                                outputString.append(String.format("%s: %d %s", jobLookup.getGood(), jobQuantity.getValue(), jobQuantity.getKey()));
                            }

                        }
                    }
                }
                infoLabel.setText("Total Producers: " + outputString);

            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        displayPanel.add(totalProducersDisplayButton).padRight(10);

        // current agent display button
        TextButton currentAgentDisplayButton = new TextButton("Display", firstSkin);
        currentAgentDisplayButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // find agent and change information box text to description
                for (Agent agent : market.getAgents()){
                    if (agent.getId().equals(agentID)){
                        infoLabel.setText("Agent " + agentID + ": " + agent);
                    }
                }

            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        displayPanel.add(currentAgentDisplayButton).padRight(10);

        // unmet needs display button
        TextButton unmetNeedsDisplayButton = new TextButton("Display", firstSkin);
        unmetNeedsDisplayButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // find total number of unmet needs and change information box text to description
                // make hash map of total unmet needs
                HashMap<String, Double> unmetNeedsTotals = new HashMap<>();
                for (Agent a : market.getAgents()){
                    for (Map.Entry<String, Consumption> consumptionProfile : a.getConsumption().entrySet()){
                        if (!unmetNeedsTotals.containsKey(consumptionProfile.getKey())){
                            unmetNeedsTotals.put(consumptionProfile.getKey(), consumptionProfile.getValue().getTotalUnmetNeed());
                        }
                        else {
                            unmetNeedsTotals.put(consumptionProfile.getKey(),
                                    unmetNeedsTotals.get(consumptionProfile.getKey()) + consumptionProfile.getValue().getTotalUnmetNeed());
                        }
                    }
                }
                // assemble into output string
                StringBuilder outputString = new StringBuilder();
                for (Map.Entry<String, Double> unmetNeedsTotal : unmetNeedsTotals.entrySet()){
                    // add leading new line
                    outputString.append("\n\n");
                    // append good information
                    outputString.append(String.format("%s: %.2f", unmetNeedsTotal.getKey(), unmetNeedsTotal.getValue()));

                }
                infoLabel.setText("Total Unmet Needs: " + outputString);

            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        displayPanel.add(unmetNeedsDisplayButton).padRight(15);

        // priority weight sum display button
        TextButton priorityWeightDisplayButton = new TextButton("Display", firstSkin);
        priorityWeightDisplayButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // find total amount of priority weights and change information box text to description
                // make hash map of total priority weights
                HashMap<String, Double> priorityWeightTotals = new HashMap<>();

                for (Agent a : market.getAgents()){
                    for (Priority priority : a.getPriorities()){
                        if (!priorityWeightTotals.containsKey(priority.getGood())){
                            priorityWeightTotals.put(priority.getGood(), priority.getWeight());
                        }
                        else {
                            priorityWeightTotals.put(priority.getGood(),
                                    priorityWeightTotals.get(priority.getGood()) + priority.getWeight());
                        }
                    }
                }
                // assemble into output string
                StringBuilder outputString = new StringBuilder();
                for (Map.Entry<String, Double> priorityWeight : priorityWeightTotals.entrySet()){
                    // add leading new line
                    outputString.append("\n\n");
                    // append good information
                    outputString.append(String.format("%s: %.2f", priorityWeight.getKey(), priorityWeight.getValue()));

                }
                infoLabel.setText("Total Priority Weights: " + outputString);

            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        displayPanel.add(priorityWeightDisplayButton).padRight(20);

        // market profile display button
        TextButton marketProfileDisplayButton = new TextButton("Display", firstSkin);
        marketProfileDisplayButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // find market profile and change information box text

                infoLabel.setText("Market Profile: " + market.getMarketProfile());

            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        displayPanel.add(marketProfileDisplayButton).padRight(20);

        // market profile display button
        TextButton marketPropertiesDisplayButton = new TextButton("Display", firstSkin);
        marketPropertiesDisplayButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // get list of agent IDs
                StringBuilder agentIDs = new StringBuilder();
                // use standard for loop to have access to index to prevent last comma from being added
                for (int i = 0; i < market.getAgents().size(); i++){
                    agentIDs.append(market.getAgents().get(i).getId());
                    if (i < market.getAgents().size() - 1){
                        agentIDs.append(", ");
                    } else{
                        agentIDs.append(".");
                    }
                }

                // find market properties and change information box text
                infoLabel.setText("Market Properties: " +
                        "\nThis market's inventory is: " + market.getInventory() + "\n" +
                        "\nIt permits the following job->output combinations: " + market.getJobOutputs() + "\n" +
                        "\nThe market has these prices: " + market.getPrices() +
                        "\nThe market has agents referred to by the following IDs: \n" + agentIDs);

            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        displayPanel.add(marketPropertiesDisplayButton).padRight(10);

        return displayPanel;


    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.9f, 0.9f, 0.9f, 1);
        Gdx.input.setInputProcessor(stage);
        // only run market if simulation is not paused
        if (!isPaused){
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

}

