package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.michaeldmiller.economicagents.MarketInfo;

import java.util.ArrayList;
import java.util.HashSet;

public class MarketCreationScreen implements Screen {
    final MarketUI marketUI;
    Stage stage;
    Table masterTable;
    VerticalGroup marketGoods;
    Skin firstSkin;
    Label infoLabel;
    ArrayList<MarketInfo> currentMarketProfile;

    public MarketCreationScreen(final MarketUI marketUI) {
        // initialize the marketUI property, stage, and button skin
        this.marketUI = marketUI;
        stage = new Stage(new FitViewport(marketUI.worldWidth, marketUI.worldHeight));
        firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
        currentMarketProfile = new ArrayList<MarketInfo>();

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
        Label title = new Label("Market Creator", firstSkin);

        // information label, wrap is true for multiple information lines
        infoLabel = new Label(" -------------Information-------------", firstSkin);
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

        // create scroll pane for goods
        ScrollPane goodsScrollPane = new ScrollPane(marketGoods);
        // set to fill whole rest of the screen (might be changed later), align to the top
        masterTable.add(goodsScrollPane).expandY().top();

        // info box, add information label
        // create scroll pane
        final ScrollPane informationScrollPane = new ScrollPane(infoLabel);
        masterTable.add(informationScrollPane).width(250).center().top();

        // add bottom row
        masterTable.row();
        // left align
        // masterTable.add(createBottomRow()).align(Align.left).top().padLeft(10);
        // center align
        masterTable.add(createBottomRow()).top();

        // add create button
        Button createButton = new TextButton("Create", firstSkin);
        createButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // loop through market good table
                int i = 0;
                StringBuilder errorText = new StringBuilder();
                errorText.append("Error(s):\n");
                ArrayList<MarketInfo> marketInfoAccumulation = new ArrayList<>();
                for (Actor actor : marketGoods.getChildren()){
                    // ignore the first two
                    if (!(i <= 1)){
                        // cast to table, as it is known everything after the first two is a data table for a good
                        Table t = (Table) actor;
                        // create error text, which will display the location of any errors

                        int currentGoodNum = i - 1;

                        // get the children, i.e. the fields about the good information

                        // store text from the good field as the good's name
                        TextField goodField = (TextField) t.getChild(0);
                        String goodName = goodField.getText();

                        // get the base consumption, reject if it is not a number or less than zero.
                        TextField consumptionField = (TextField) t.getChild(1);
                        double baseConsumption = 0;
                        try{
                            baseConsumption = Double.parseDouble(consumptionField.getText());
                            if (baseConsumption < 0){
                                throw new IllegalArgumentException();
                            }
                        } catch(NumberFormatException e){
                            errorText.append(String.format("The base consumption of good %d (%s) must be a number.\n",
                                    currentGoodNum, goodName));
                        } catch(IllegalArgumentException e){
                            errorText.append(String.format("The base consumption of good %d (%s) must be positive.\n",
                                    currentGoodNum, goodName));
                        }

                        // get the base production, reject if it is not a number or less than zero.
                        TextField productionField = (TextField) t.getChild(2);
                        double baseProduction = 0;
                        try{
                            baseProduction = Double.parseDouble(productionField.getText());
                            if (baseProduction < 0) {
                                throw new IllegalArgumentException();
                            }
                        } catch(NumberFormatException e){
                            errorText.append(String.format("The base production of good %d (%s) must be a number.\n",
                                    currentGoodNum, goodName));
                        } catch(IllegalArgumentException e){
                            errorText.append(String.format("The base production of good %d (%s) must be positive.\n",
                                    currentGoodNum, goodName));
                        }


                        // get the elasticities, no checks required here as the slider and selector limit
                        // the possible user inputs
                        // index 3 is the label for the slider, skip to 4
                        Slider demandElasticitySlider = (Slider) t.getChild(4);
                        double demandElasticity = demandElasticitySlider.getValue();


                        // despite complaints, the value at index 5 is in fact a select box of integers
                        @SuppressWarnings("unchecked") SelectBox<Integer> supplyElasticityBox =
                                (SelectBox<Integer>) t.getChild(5);
                        double supplyElasticity = supplyElasticityBox.getSelected();

                        // get the base cost, reject if it is not a number or less than zero.
                        TextField baseCostField = (TextField) t.getChild(6);
                        double baseCost = 0;
                        try{
                            baseCost = Double.parseDouble(baseCostField.getText());
                            if (baseConsumption < 0){
                                throw new IllegalArgumentException();
                            }
                        } catch(NumberFormatException e){
                            errorText.append(String.format("The base cost of good %d (%s) must be a number.\n",
                                    currentGoodNum, goodName));
                        } catch(IllegalArgumentException e){
                            errorText.append(String.format("The base cost of good %d (%s) must be positive.\n",
                                    currentGoodNum, goodName));
                        }


                        // get the base weight, no check required due to the select box limit on the possible
                        // user inputs
                        // despite complaints, the value at index 7 is in fact a select box of integers
                        @SuppressWarnings("unchecked") SelectBox<Integer> baseWeightBox =
                                (SelectBox<Integer>) t.getChild(7);
                        double baseWeight = supplyElasticityBox.getSelected();

                        // get the job name
                        TextField jobNameField = (TextField) t.getChild(8);
                        String jobName = jobNameField.getText();

                        // get the job chance, slider ensures all values are valid
                        // skip index 9, as it is occupied by the label for the job chance
                        Slider jobChanceSlider = (Slider) t.getChild(10);
                        double jobChance = jobChanceSlider.getValue();

                        // with reasonable values, create a market info object for the good
                        MarketInfo marketInfo = new MarketInfo(goodName, baseConsumption, baseProduction,
                                demandElasticity, supplyElasticity, baseCost, baseWeight,
                                jobName, jobChance);
                        marketInfoAccumulation.add(marketInfo);


                    }
                    // increment counter
                    i++;
                }
                // prevent repeats by checking for duplicates
                // create list of good and job names
                ArrayList<String> goodNames = new ArrayList<>();
                ArrayList<String> jobNames = new ArrayList<>();
                for (MarketInfo currentMarketProperty : marketInfoAccumulation){
                    goodNames.add(currentMarketProperty.getGood());
                    jobNames.add(currentMarketProperty.getJobName());
                }
                // check to see if there are any goods at all, create an error message if there are none
                if (goodNames.size() == 0){
                    errorText.append("Please add at least one good.\n");
                }
                // create a hash set for names, then loop through the good names. If it cannot be added to the set,
                // i.e. if there is a duplicate, report the failure and the current good number as an error to the
                // error text.
                HashSet<String> goodNameSet = new HashSet<>();
                int placeCountNames = 1;
                for (String name : goodNames){
                    boolean goodNotThere = goodNameSet.add(name);
                    if (!goodNotThere){
                        errorText.append(String.format("Good %d (%s) cannot be named this, as the name has already " +
                                        "been used.\n", placeCountNames, name));
                    }
                    placeCountNames++;
                }
                // do the same operation as above for the job/profession names
                HashSet<String> jobNameSet = new HashSet<>();
                int placeCountJobs = 1;
                for (String job : jobNames){
                    boolean jobNotThere = jobNameSet.add(job);
                    if (!jobNotThere){
                        errorText.append(String.format("Job %d (%s) cannot be named this, as the name has already " +
                                "been used.\n", placeCountJobs, job));
                    }
                    placeCountJobs++;
                }

                // check to make sure the sum of the job chances is not 0, create an error text if the sum is 0.
                double jobChanceSum = 0;
                for (MarketInfo currentMarketInfo : marketInfoAccumulation){
                    jobChanceSum += currentMarketInfo.getJobChance();
                }
                // if there was no good added, do not print the error message
                if (jobChanceSum == 0 && jobNames.size() != 0){
                    errorText.append("At least one good must have a job chance greater than 0.\n");
                }

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

                // finally, if there were no errors (i.e. the error text is still its initial value), create the
                // market, otherwise, set the information box to show the encountered errors
                if(errorText.toString().equals("Error(s):\n")){
                    // set the market information
                    currentMarketProfile = marketInfoAccumulation;
                    // create a new main interface screen using the new market profile
                    marketUI.mainInterface = new MainInterface(marketUI, numberOfAgents, currentMarketProfile);
                    // if a market did not exist before, refresh the main menu to include an enabled resume button
                    if (!marketUI.marketExists){
                        marketUI.marketExists = true;
                        marketUI.mainMenu = new MainMenu(marketUI);
                    }
                    // set the current screen to the main interface
                    marketUI.setScreen(marketUI.mainInterface);
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
        masterTable.add(createButton);
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
                infoLabel.setText("Instructions:\n" + "Welcome to the MarketUI. MarketUI dynamically simulates " +
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
                        "them. When you are done, click the create button in the bottom right to create the market" +
                        "and begin simulating it. Thank you for using MarketUI.\n");
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
        Label numberOfAgents = new Label ("# of Agents", firstSkin);
        bottomRowLabels.add(numberOfAgents).padLeft(20);
        // info button
        TextButton numberOfAgentsButton = new TextButton("I", firstSkin);
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
        bottomRowLabels.add(numberOfAgentsButton).expandX();

        TextField numberOfAgentsField = new TextField("2000", firstSkin);
        bottomRowInteractive.add(numberOfAgentsField).expandX();

        return bottomRow;
    }

    public Table createCategoryLabels(){
        // create labels and information toggle buttons for the market info categories
        Table labels = new Table();

        // good name label
        Label name = new Label ("Good Name", firstSkin);
        labels.add(name).padLeft(20);
        // info button
        TextButton nameButton = new TextButton("I", firstSkin);
        nameButton.addListener(new InputListener(){
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
        labels.add(nameButton).padRight(20);


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
        labels.add(productionButton).padRight(40);

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
        labels.add(priceElasticityButton).padRight(65);

        // good base cost label
        Label baseValues = new Label ("Base Values", firstSkin);
        labels.add(baseValues);
        // info button
        TextButton baseValuesButton = new TextButton("I", firstSkin);
        baseValuesButton.addListener(new InputListener(){
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
        labels.add(baseValuesButton).padRight(70);

        // good job name label
        Label jobName = new Label ("Job Name", firstSkin);
        labels.add(jobName);
        // info button
        TextButton jobNameButton = new TextButton("I", firstSkin);
        jobNameButton.addListener(new InputListener(){
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
        labels.add(jobNameButton).padRight(55);

        // job chance label
        Label chance = new Label ("Job Chance", firstSkin);
        labels.add(chance);
        // info button
        TextButton chanceButton = new TextButton("I", firstSkin);
        chanceButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Job Chance:\n" +
                        "This is the chance that an agent in the market will have the profession which " +
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
        labels.add(chanceButton).padRight(120);


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

        // get base good weight
        SelectBox<Integer> baseWeight = new SelectBox<>(firstSkin);
        Array<Integer> weightChoices = new Array<>();
        weightChoices.add(1);
        baseWeight.setItems(weightChoices);
        good.add(baseWeight).padRight(10);

        // get job name
        TextField jobName = new TextField("Job Name", firstSkin);
        good.add(jobName).padRight(25);

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
