package com.michaeldmiller.economicagents;

// given a good in a market, establish the following attributes
public class MarketInfo {
    private final String good;
    private double baseConsumption;
    private double baseProduction;
    private double priceElasticityDemand;
    private double priceElasticitySupply;
    private double goodCost;
    private final double priorityBaseWeight;
    private final String jobName;
    private final double jobChance;

    public MarketInfo(String good, double baseConsumption, double baseProduction, double priceElasticityDemand,
                      double priceElasticitySupply, double goodCost, double priorityBaseWeight,
                      String jobName, double jobChance) {
        this.good = good;
        this.baseConsumption = baseConsumption;
        this.baseProduction = baseProduction;
        this.priceElasticityDemand = priceElasticityDemand;
        this.priceElasticitySupply = priceElasticitySupply;
        this.goodCost = goodCost;
        this.priorityBaseWeight = priorityBaseWeight;
        this.jobName = jobName;
        this.jobChance = jobChance;
    }

    public String getGood() {
        return good;
    }

    public double getBaseConsumption() {
        return baseConsumption;
    }
    public double getBaseProduction(){
        return baseProduction;
    }

    public double getPriceElasticityDemand() {
        return priceElasticityDemand;
    }

    public double getPriceElasticitySupply() {
        return priceElasticitySupply;
    }

    public double getGoodCost() {
        return goodCost;
    }

    public double getPriorityBaseWeight() {
        return priorityBaseWeight;
    }

    public String getJobName() {
        return jobName;
    }

    public double getJobChance() {
        return jobChance;
    }

    public void setBaseConsumption(double newBaseConsumption) {
        this.baseConsumption = newBaseConsumption;
    }

    public void setBaseProduction(double newBaseProduction) {
        this.baseProduction = newBaseProduction;
    }

    public void setPriceElasticityDemand(double newPriceElasticityDemand) {
        this.priceElasticityDemand = newPriceElasticityDemand;
    }

    public void setPriceElasticitySupply(double newPriceElasticitySupply) {
        this.priceElasticitySupply = newPriceElasticitySupply;
    }

    public void setGoodCost(double newGoodCost) {
        this.goodCost = newGoodCost;
    }

    public String toString() {
        return ("Good: " + this.getGood() + ", " +
                "Base Consumption: " + this.getBaseConsumption() + ", " +
                "Base Production: " + this.getBaseProduction() + ", " +
                "Demand Elasticity: " + this.getPriceElasticityDemand() + ", " +
                "Supply Elasticity: " + this.getPriceElasticitySupply() + ", " +
                "Good Cost: " + this.getGoodCost() + ", " +
                "Base Weight: " + this.getPriorityBaseWeight() + ", " +
                "Job Name: " + this.getJobName() + ", " +
                "Job Chance: " + this.getJobChance() + ", ");
    }


}
