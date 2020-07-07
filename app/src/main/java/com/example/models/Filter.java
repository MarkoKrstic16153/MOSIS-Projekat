package com.example.models;

public class Filter {
    private Intensity intensityFilter;
    private Type typeFilter;
    private double rangeFilter;
    private boolean showMine;

    public boolean isShowMine() {
        return showMine;
    }

    public void setShowMine(boolean showMine) {
        this.showMine = showMine;
    }



    public Filter(){
        this.intensityFilter = Intensity.ALL;
        this.typeFilter = Type.ALL;
        this.rangeFilter = 1000;
    }

    public Filter(Intensity intensityFilter, Type typeFilter, double rangeFilter) {
        this.intensityFilter = intensityFilter;
        this.typeFilter = typeFilter;
        this.rangeFilter = rangeFilter;
    }

    public Intensity getIntensityFilter() {
        return intensityFilter;
    }

    public void setIntensityFilter(Intensity intensityFilter) {
        this.intensityFilter = intensityFilter;
    }

    public Type getTypeFilter() {
        return typeFilter;
    }

    public void setTypeFilter(Type typeFilter) {
        this.typeFilter = typeFilter;
    }

    public double getRangeFilter() {
        return rangeFilter;
    }

    public void setRangeFilter(double rangeFilter) {
        this.rangeFilter = rangeFilter;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "intensityFilter=" + intensityFilter +
                ", typeFilter=" + typeFilter +
                ", rangeFilter=" + rangeFilter +
                '}';
    }
}
