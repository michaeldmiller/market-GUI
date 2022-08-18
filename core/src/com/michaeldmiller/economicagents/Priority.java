package com.michaeldmiller.economicagents;

// A 'Priorities' is an ArrayList of com.michaeldmiller.economicagents.Priority
public class Priority {
    private String good;
    private double baseWeight;
    private double relativeNeed;
    private double modifier;
    private double priceElasticity;
    private double originalPriceElasticity;
    private double weight;

    public Priority(String good, double baseWeight, double relativeNeed,
                    double modifier, double priceElasticity, double originalPriceElasticity,
                    double weight) {
        this.good = good;
        this.baseWeight = baseWeight;
        this.relativeNeed = relativeNeed;
        this.modifier = modifier;
        this.priceElasticity = priceElasticity;
        this.originalPriceElasticity = originalPriceElasticity;
        this.weight = weight;
    }

    public String getGood() {
        return good;
    }

    public double getBaseWeight() {
        return baseWeight;
    }

    public double getRelativeNeed() {
        return relativeNeed;
    }

    public double getModifier() {
        return modifier;
    }

    public double getPriceElasticity() {
        return priceElasticity;
    }

    public double getOriginalPriceElasticity() {
        return originalPriceElasticity;
    }

    public double getWeight() {
        return weight;
    }

    public void setGood(String newGood) {
        good = newGood;
    }

    public void setBaseWeight(double newBaseWeight) {
        baseWeight = newBaseWeight;
    }

    public void setRelativeNeed(double newRelativeNeed) {
        relativeNeed = newRelativeNeed;
    }

    public void setModifier(double newModifier) {
        modifier = newModifier;
    }

    public void setPriceElasticity(double newPriceElasticity) {
        priceElasticity = newPriceElasticity;
    }

    public void setOriginalPriceElasticity(double newOriginalPriceElasticity) {
        originalPriceElasticity = newOriginalPriceElasticity;
    }

    public void setWeight(double newWeight) {
        weight = newWeight;
    }

    public String toString() {
        return ("\n\n" + this.getGood() + ": " +
                String.format("base weight: %.2f", this.getBaseWeight()) + ", " +
                String.format("relative need: %.2f", this.getRelativeNeed()) + ", " +
                String.format("modifier: %.2f", this.getModifier()) + ", " +
                String.format("price elasticity: %.2f", this.getPriceElasticity()) + ", " +
                String.format("original price elasticity: %.2f", this.getOriginalPriceElasticity()) + ", " +
                String.format("final weight: %.2f", this.getWeight()) + ".");
    }
}
