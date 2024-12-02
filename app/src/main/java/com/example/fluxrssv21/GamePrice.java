package com.example.fluxrssv21;

public class GamePrice {
    private String gameName;
    private String platform;
    private String price;

    public GamePrice() {}

    public GamePrice(String gameName, String platform, String price) {
        this.gameName = gameName;
        this.platform = platform;
        this.price = price;
    }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
}
