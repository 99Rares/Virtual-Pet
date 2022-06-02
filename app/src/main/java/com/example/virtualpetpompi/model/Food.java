package com.example.virtualpetpompi.model;

public class Food {
    private String fotoName;
    private int price;
    private  int fullness;

    public Food(String fotoName, int fullness,int price) {
        this.fotoName = fotoName;
        this.price = price;
        this.fullness = fullness;
    }

    @Override
    public String toString() {
        return fotoName + "|" + fullness +"|"+price +"|";
    }

    public String getFotoName() {
        return fotoName;
    }

    public void setFotoName(String fotoName) {
        this.fotoName = fotoName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getFullness() {
        return fullness;
    }

    public void setFullness(int fullness) {
        this.fullness = fullness;
    }
}
