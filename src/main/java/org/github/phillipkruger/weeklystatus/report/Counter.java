package org.github.phillipkruger.weeklystatus.report;

public class Counter {
    private static int c = 0;
    
    public int next(){
        return ++c;
    } 
    
    public String reset(){
        c = 0;
        return "";
    }
}
