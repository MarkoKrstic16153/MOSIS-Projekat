package com.example.mfit;


import com.example.models.Filter;

public class FilterData {

    public Filter filter;

    public FilterData(){
        filter=new Filter();
    }

    private  static class SingletonHolder{
        private static final FilterData instance = new FilterData();
    }
    public static FilterData getInstance(){
        return  FilterData.SingletonHolder.instance;
    }

}
