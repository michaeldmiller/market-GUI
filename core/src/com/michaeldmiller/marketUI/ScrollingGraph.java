package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScrollingGraph extends Actor {
    private int x;
    private int y;
    private int width;
    private int height;
    private int worldWidth;
    private int worldHeight;
    private double scale;
    private String title;
    private HashMap<String, Integer> dataCoordinates;
    private HashMap<String, Color> colorLookup;
    private Skin skin;
    private int frame;
    private Stage stage;
    private ArrayList<GraphPoint> dots;
    private ArrayList<Label> labels;
    private Label graphTitle;

    public ScrollingGraph(int x, int y, int width, int height, int worldWidth, int worldHeight, double scale,
                          String title, HashMap<String, Integer> dataCoordinates, HashMap<String, Color> colorLookup,
                          Skin skin, int frame, Stage stage){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.scale = scale;
        this.title = title;
        this.dataCoordinates = dataCoordinates;
        this.colorLookup = colorLookup;
        this.skin = skin;
        this.frame = frame;
        this.stage = stage;
        this.dots = new ArrayList<GraphPoint>();
        this.labels = new ArrayList<Label>();
        this.graphTitle = new Label("", skin);
    }
    // accessors
    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }
    public float getWidth(){
        return width;
    }
    public float getHeight(){
        return height;
    }
    public int getWorldWidth(){
        return worldWidth;
    }
    public int getWorldHeight(){
        return worldHeight;
    }
    public double getScale(){
        return scale;
    }
    public String getTitle() {
        return title;
    }
    public HashMap<String, Integer> getDataCoordinates(){
        return dataCoordinates;
    }
    public HashMap<String, Color> getColorLookup(){
        return colorLookup;
    }
    public Skin getSkin(){
        return skin;
    }
    public int getFrame(){
        return frame;
    }
    public Stage getStage(){
        return stage;
    }
    public ArrayList<GraphPoint> getDots() {
        return dots;
    }
    public ArrayList<Label> getLabels() {
        return labels;
    }
    public Label getGraphTitle() {
        return graphTitle;
    }
    // mutators
    public void setX(int newX) {
        this.x = newX;
    }
    public void setY(int newY) {
        this.y = newY;
    }
    public void setWidth(int newWidth) {
        this.width = newWidth;
    }
    public void setHeight(int newHeight) {
        this.height = newHeight;
    }
    public void setWorldWidth(int newWorldWidth){
        this.worldWidth = newWorldWidth;
    }
    public void setWorldHeight(int newWorldHeight){
        this.worldHeight = newWorldHeight;
    }
    public void setScale(int newScale) {
        this.scale = newScale;
    }
    public void setTitle(String newTitle){
        this.title = newTitle;
    }
    public void setDataCoordinates(HashMap<String, Integer> newDataCoordinates) {
        this.dataCoordinates = newDataCoordinates;
    }
    public void setColorLookup(HashMap<String, Color> newColorLookup) {
        this.colorLookup = newColorLookup;
    }
    public void setSkin(Skin newSkin) {
        this.skin = newSkin;
    }
    public void setFrame(int newFrame) {
        this.frame = newFrame;
    }
    public void setStage(Stage newStage) {
        this.stage = newStage;
    }
    public void setDots(ArrayList<GraphPoint> newDots) {
        this.dots = newDots;
    }
    public void setLabels(ArrayList<Label> newLabels) {
        this.labels = newLabels;
    }
    public void setGraphTitle(Label newGraphTitle) {
        this.graphTitle = newGraphTitle;
    }

    public void graphData(){
        // access and store all current prices, adjusted to scale with the size of the graph

        // for each price coordinate pair, lookup the appropriate color, make a dot on the graph, then set it to scroll
        // off to the left of the screen
        for (Map.Entry<String, Integer> priceCoord : dataCoordinates.entrySet()){
            // find color
            Color dotColor = colorLookup.get(priceCoord.getKey());
            // make dot
            GraphPoint dot = new GraphPoint(x + width, priceCoord.getValue() + y, 2, 2, dotColor);

            // make actor leave screen
            MoveToAction leaveScreen = new MoveToAction();
            leaveScreen.setPosition(x - 10, y + priceCoord.getValue());
            leaveScreen.setDuration(50);
            dot.addAction(leaveScreen);
            // add actor to list of price dots
            dots.add(dot);

            stage.addActor(dot);

        }
    }

    public void makeGraph(){
        // using GraphPoints as they are general rectangles and are suited to the task
        // add x-axis
        GraphPoint xAxis = new GraphPoint(x, y, width, 3, new Color (0, 0, 0, 1));
        stage.addActor(xAxis);
        // add y-axis
        GraphPoint yAxis = new GraphPoint(x, y, 3, height, new Color (0, 0, 0, 1));
        stage.addActor(yAxis);
        // add x-ceiling
        GraphPoint xCeiling = new GraphPoint(x, y + height,
                width, 2, new Color (0, 0, 0, 1));
        stage.addActor(xCeiling);
        // add y-ceiling
        GraphPoint yCeiling = new GraphPoint(x + width, y,
                2, height, new Color (0, 0, 0, 1));
        stage.addActor(yCeiling);
        // add title
        graphTitle = new Label(title, new Label.LabelStyle(
                new BitmapFont(Gdx.files.internal("franklin-medium.fnt")),
                new Color (0.7f, 0.7f, 0.7f, 1)));
        graphTitle.setAlignment(Align.center);
        graphTitle.setPosition(((int) (x + (width / 2))) - (int) (graphTitle.getWidth() / 2),
                (int) (y + height - ((0.045 * height) * (worldHeight/height))));
        // graphTitle.setFontScale(2);
        stage.addActor(graphTitle);

    }

    public void graphLabels(){
        // calculate how often to send frame label
        // screen width can fit, comfortably, 20 labels. Calculate ratio of graph width to screen width, determine
        // how many labels will fit on a graph. Given that it takes ~2400 frames for the dots to traverse the graph,
        // finally determine how often to send a label.
        // this calculation is dependent on an actor move duration of 50.

        int dotTransitTime = 2400;
        int frameThreshold = (int) (dotTransitTime / (20 * ((double) (width) / worldWidth)));

        // round to the 50
        frameThreshold = (int) (Math.round((frameThreshold) / 50.0) * 50);

        if (frame % frameThreshold == 0 || frame == 1){
            Label timeLabel = new Label(String.valueOf(frame), skin);
            timeLabel.setPosition(x + width, y - 20);

            // add action for the labels to move to the left, following the dots
            // it is important that the duration for dot movement and label movement be the same
            MoveToAction leaveScreen = new MoveToAction();
            leaveScreen.setPosition(x - 10, y - 20);
            leaveScreen.setDuration(50);
            timeLabel.addAction(leaveScreen);
            // add labels to price label list for later removal
            labels.add(timeLabel);

            stage.addActor(timeLabel);
        }

        if (frame == 1){
            // set range of prices to be covered in the graph
            int priceMax = (int) (height * 1.0 /scale);
            int labelNum = 1;

            for (int i = x; i < (height + x); i++) {
                // split price height into 10 sections
                if (i % height / 10 == 0){
                    // set label value to tenth of priceMax * labelNum, set position at labelNum * 1/10th of the way
                    // up the graph
                    Label quantityLabel = new Label(String.valueOf(priceMax / 10 * labelNum), skin);
                    quantityLabel.setPosition(x + width - 20, y +  ((int) (height / 10) * labelNum) - 10);
                    // quantityLabel.setAlignment(Align.right);
                    // ^doesn't work

                    // add action for the labels to move to the left, following the dots. Unlike dots and tick number
                    // labels, the price labels will not be deleted upon reaching their resting place on the y-axis
                    // it is again important that the duration for dot movement and label movement be the same
                    MoveToAction leaveScreen = new MoveToAction();
                    leaveScreen.setPosition(x - 25, y + ((int) (height / 10) * labelNum) - 10);
                    leaveScreen.setDuration(50);
                    quantityLabel.addAction(leaveScreen);

                    stage.addActor(quantityLabel);

                    // add horizontal guides
                    GraphPoint xGuide = new GraphPoint(x, y + ((int) (height / 10) * labelNum),
                            width, 1, new Color (0, 0, 0, 1));
                    stage.addActor(xGuide);
                    labelNum += 1;



                }
            }
        }
    }
    public void updateGraphTitle(){
        graphTitle.remove();
        graphTitle = new Label("Agent: " + title, new Label.LabelStyle(
                new BitmapFont(Gdx.files.internal("franklin-medium.fnt")),
                new Color (0.7f, 0.7f, 0.7f, 1)));
        graphTitle.setAlignment(Align.center);
        graphTitle.setPosition(((int) (x + (width / 2))) - (int) (graphTitle.getWidth() / 2),
                (int) (y + height - ((0.045 * height) * (worldHeight/height))));
        stage.addActor(graphTitle);


    }

    // remove functions
    public void removeGraphDots(float xThresholdFloat, ArrayList<GraphPoint> dots){
        int xThreshold = (int) Math.ceil(xThresholdFloat);
        // given the list of graph points
        for (int i = 0; i < dots.size(); i++){
            // if the x coordinate of the dot is at or has surpassed the threshold plus one (moving right to left)
            // remove the dot from the stage and the list of dots
            if (dots.get(i).getX() <= xThreshold + 3){
                dots.get(i).remove();
                dots.remove(i);
            }
        }
    }

    public void removeGraphLabels(float xThresholdFloat, ArrayList<Label> labels){
        // essentially identical to removeGraphDots but for labels
        int xThreshold = (int) Math.ceil(xThresholdFloat);
        for (int i = 0; i < labels.size(); i++){
            if (labels.get(i).getX() <= xThreshold + 3){
                labels.get(i).remove();
                labels.remove(i);
            }
        }
    }
}
