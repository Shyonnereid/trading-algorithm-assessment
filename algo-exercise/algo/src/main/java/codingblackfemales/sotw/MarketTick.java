package codingblackfemales.sotw;

public class MarketTick {
    private final int price;
    private final int volume;
    private final int depth;

    public MarketTick(int price, int volume, int depth) {
        this.price = price;
        this.volume = volume;
        this.depth = depth;
    }

    public int getPrice() {
        return price;
    }

    public int getVolume() {
        return volume;
    }

    public int getDepth() {
        return depth;
    }
}