package org.GameBot.GameBot.Shop;

public enum ShopItem {
    FirstAidKit(100, 60, "firstAidKit"),
    Energetic(110, 65, "energetics"),
    PowerBooster(150, 75, "powerBooster");

    private final int price;
    private final int cost;
    private final String column;

    ShopItem(int price, int cost, String column){
        this.price = price;
        this.cost = cost;
        this.column = column;
    }

    public int getPrice() { return price; }
    public int getCost() { return cost; }
    public String getColumn() { return column; }
}
