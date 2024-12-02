package com.example.fluxrssv21;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;
import java.util.List;

public class MyRSSsaxHandler extends DefaultHandler {
    private boolean inTitle = false;
    private boolean inPrice = false;
    private GamePrice gamePrice;
    private List<GamePrice> gamePrices = new ArrayList<>();

    public List<GamePrice> getGamePrices() {
        return gamePrices;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equalsIgnoreCase("title")) {
            inTitle = true;
            gamePrice = new GamePrice();
        } else if (qName.equalsIgnoreCase("price")) {
            inPrice = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String content = new String(ch, start, length).trim();
        if (inTitle) {
            gamePrice.setGameName(content);
        } else if (inPrice) {
            gamePrice.setPrice(content);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("title")) {
            inTitle = false;
        } else if (qName.equalsIgnoreCase("price")) {
            inPrice = false;

            // Add the game to the list if valid
            if (gamePrice.getGameName() != null && gamePrice.getPrice() != null) {
                gamePrices.add(gamePrice);
                System.out.println("Parsed Game: " + gamePrice.getGameName() + ", Price: " + gamePrice.getPrice());
                gamePrice = new GamePrice();
            }
        }
    }
}
