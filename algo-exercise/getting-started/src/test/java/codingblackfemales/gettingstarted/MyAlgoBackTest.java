package codingblackfemales.gettingstarted;

import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.marketdata.api.MarketTick;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.OrderStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class MyAlgoBackTest extends AbstractAlgoBackTest {

    private MyAlgoLogic myAlgoLogic;

    @Override
    public AlgoLogic createAlgoLogic() {
        this.myAlgoLogic = new MyAlgoLogic();
        return myAlgoLogic;
    }

    private UnsafeBuffer encodeMarketTick(MarketTick tick) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putDouble(tick.getPrice());
        buffer.putInt(tick.getVolume());
        buffer.putDouble(tick.getSpread());
        buffer.flip();
        return new UnsafeBuffer(buffer);
    }

    private MarketTick createFavorableTick(double price, int volume, double spread) {
        return new MarketTick(price, volume, spread);
    }

    @Test
    public void testInitialStateWithNoMarketData() throws Exception {
        var state = container.getState();
        assertEquals(0, state.getChildOrders().size());
    }

    @Test
    public void testSimpleOrderPlacement() throws Exception {
        send(encodeMarketTick(createFavorableTick(95.0, 500, 1))); // Below BUY_THRESHOLD

        var state = container.getState();
        assertTrue("Expected at least one order to be placed.", state.getChildOrders().size() > 0);
        assertEquals(OrderStatus.FILLED, state.getChildOrders().get(0).getStatus());
    }

    @Test
    public void testNoActionOnUnfavorableCondition() throws Exception {
        send(encodeMarketTick(createFavorableTick(120.0, 500, 1))); // Above BUY_THRESHOLD

        var state = container.getState();
        assertEquals("Expected no orders to be placed.", 0, state.getChildOrders().size());
    }

    @Test
    public void testOrderCancellation() throws Exception {
        send(encodeMarketTick(createFavorableTick(95.0, 500, 1))); // Favorable tick
        var state = container.getState();

        assertTrue("Expected at least one order to exist.", !state.getChildOrders().isEmpty());

        ChildOrder order = state.getChildOrders().get(0);
        order.setCreationTime(System.currentTimeMillis() - (MyAlgoLogic.MAX_ORDER_LIFETIME + 1000));

        send(encodeMarketTick(createFavorableTick(120.0, 500, 1))); // Unfavorable tick

        state = container.getState();
        assertTrue("Expected order to be canceled.", state.getChildOrders().isEmpty());
    }
}
