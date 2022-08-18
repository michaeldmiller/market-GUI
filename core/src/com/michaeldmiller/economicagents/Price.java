package com.michaeldmiller.economicagents;

public class Price {
    private String good;
    private double cost;
    private double equilibriumCost;
    private double originalCost;

    public Price(String good, double cost, double equilibriumCost, double originalCost) {
        this.good = good;
        this.cost = cost;
        this.equilibriumCost = equilibriumCost;
        this.originalCost = originalCost;
    }

    public String getGood() {
        return good;
    }

    public double getCost() {
        return cost;
    }

    public double getEquilibriumCost() {
        return equilibriumCost;
    }

    public double getOriginalCost() {
        return originalCost;
    }

    public void setGood(String newGood) {
        good = newGood;
    }

    public void setCost(double newCost) {
        cost = newCost;
    }

    public void setEquilibriumCost(double newEquilibriumCost) {
        equilibriumCost = newEquilibriumCost;
    }
    public void setOriginalCost(double newOriginalCost){
        originalCost = newOriginalCost;
    }

    public String toString() {
        return ("\n\n" + this.getGood() + ": " +
                String.format("Price: %.2f", this.getCost()) + ", " +
                String.format("Equilibrium Price: %.2f", this.getEquilibriumCost()) + ", " +
                String.format("Original Price: %.2f", this.getOriginalCost())
        );
    }
}
