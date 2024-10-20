package codingblackfemales.marketdata.api;

public class MarketTick {

    private double price;
    private int volume;
    private double spread;

    public MarketTick(double price, int volume, double spread) {
        this.price = price;
        this.volume = volume;
        this.spread = spread;
    }

    public double getPrice() {
        return price;
    }

    public int getVolume() {
        return volume;
    }

    public double getSpread() {
        return spread;
    }
}
