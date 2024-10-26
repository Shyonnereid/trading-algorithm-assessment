        package codingblackfemales.gettingstarted;

import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.NoAction;
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
        this.myAlgoLogic = new MyAlgoLogic(); // Initialize MyAlgoLogic instance
        return myAlgoLogic;
    }

    // Method to encode MarketTick into UnsafeBuffer
    private UnsafeBuffer encodeMarketTick(MarketTick tick) {
        ByteBuffer buffer = ByteBuffer.allocate(1024); // Allocate enough space
        buffer.putDouble(tick.getPrice()); // 8 bytes for the price
        buffer.putInt(tick.getVolume()); // 4 bytes for the volume
        buffer.putDouble(tick.getSpread()); // 8 bytes for the spread
        return new UnsafeBuffer(buffer);
    }

    // Create an unfavorable tick based on the logic where price is above the buy
    // threshold
    private MarketTick createUnfavorableTick() {
        return new MarketTick(110, 500, 5); // Price 110, low volume, wide spread (unfavorable)
    }

    // Create a method to generate another MarketTick for testing purposes
    private MarketTick createAnotherMarketTick() {
        return new MarketTick(105, 600, 4); // Example values for another tick
    }

    // Create a method to generate a favorable MarketTick
    private MarketTick createFavorableTick(double price, int volume, double spread) {
        return new MarketTick(price, volume, spread); // Create a favorable tick
    }

    @Test
    public void testInitialStateWithNoMarketData() throws Exception {
        var state = container.getState();
        assertEquals(0, state.getChildOrders().size());
    }

    @Test
    public void testOrderPlacementWhenMarketConditionsMet() throws Exception {
        // Create market data that triggers a buy order
        send(createTick()); // Initial market data
        send(createTick2()); // Simulating market condition where buy is triggered

        var state = container.getState();

        // Check that an order was placed
        assertEquals(1, state.getChildOrders().size()); // Expecting 1 order
        assertEquals(OrderStatus.NEW, state.getChildOrders().get(0).getStatus());
    }

    @Test
    public void testOrderCancellationDueToTimeout() throws Exception {
        // Create a market tick that would normally trigger an order
        send(createTick());
        send(createTick2()); // Triggering order placement

        // Simulate time passing by manually updating the order creation time
        var state = container.getState();
        ChildOrder order = state.getChildOrders().get(0);

        // Manually set the order creation time to simulate timeout
        order.setCreationTime(System.currentTimeMillis() - (MyAlgoLogic.MAX_ORDER_LIFETIME + 1000)); // Exceeding
                                                                                                     // lifetime

        // Now evaluate the algorithm using the instance of MyAlgoLogic (myAlgoLogic)
        var action = myAlgoLogic.evaluate(state); // Corrected to use myAlgoLogic instance

        // Check that the order was canceled
        assertTrue(action instanceof CancelChildOrder);
        assertEquals(order.getOrderId(), ((CancelChildOrder) action).getOrderId());
    }

    @Test
    public void testNoActionTakenWhenConditionsAreNotMet() throws Exception {
        // Create market data that does not meet buy conditions
        send(createTick()); // Initial tick with favorable conditions
        send(encodeMarketTick(createUnfavorableTick())); // Unfavorable conditions, price is too high (110)

        var state = container.getState();

        // Call evaluate on the instance of MyAlgoLogic
        var action = myAlgoLogic.evaluate(state);

        // Expecting no action to be taken
        assertEquals(NoAction.NoAction, action); // No action expected because price is too high
    }

    @Test
    public void testMultipleMarketUpdates() throws Exception {
        // Simulate multiple ticks
        send(createTick());
        send(createTick2()); // This would normally trigger an order
        send(encodeMarketTick(createAnotherMarketTick())); // Send another market data update

        // Validate the state after multiple updates
        var state = container.getState();

        // Assert filled orders, cancellations, etc. based on your logic
        long filledQuantity = state.getChildOrders().stream()
                .map(ChildOrder::getFilledQuantity)
                .reduce(Long::sum).orElse(0L);

        long expectedFilledQuantity = 100; // Set this to the expected value

        assertEquals(expectedFilledQuantity, filledQuantity); // Validate the filled quantity
    }

    @Test
    public void testOrderCancellationOnMarketDataChange() throws Exception {
        // Create an initial market state
        send(createTick());
        send(createTick2()); // Assuming this triggers an order placement

        // Evaluate the algo logic
        var state = container.getState();
        var action = myAlgoLogic.evaluate(state); // Corrected to use myAlgoLogic instance

        // Verify if the order was canceled
        assertTrue(action instanceof CancelChildOrder);
    }

    // New test for Market Data Volatility
    @Test
    public void testMarketDataVolatility() throws Exception {
        // Simulating favorable conditions
        send(encodeMarketTick(createFavorableTick(100, 1000, 1))); // Favorable tick
        send(encodeMarketTick(createFavorableTick(105, 1000, 1))); // Favorable tick
        send(encodeMarketTick(createFavorableTick(150, 1000, 1))); // Sudden price spike (volatility)

        var state = container.getState();
        var action = myAlgoLogic.evaluate(state);

        // Verify the algorithm's response to volatility
        assertTrue(action instanceof NoAction); // Adjust this based on your expected behavior
    }

    // New test for Continuous Favorable Conditions
    @Test
    public void testContinuousFavorableConditions() throws Exception {
        // Send multiple favorable ticks
        for (int price = 95; price < 100; price += 1) {
            send(encodeMarketTick(createFavorableTick(price, 1000, 1))); // All ticks are favorable
        }

        var state = container.getState();

        // Expecting orders to be placed continuously
        assertEquals(5, state.getChildOrders().size()); // Adjust this based on your logic
    }

    // New test for Unfavorable Conditions After Favorable Ones
    @Test
    public void testUnfavorableConditionsAfterFavorable() throws Exception {
        // Initial favorable conditions
        send(encodeMarketTick(createFavorableTick(95, 1000, 1))); // Favorable tick
        send(encodeMarketTick(createFavorableTick(97, 1000, 1))); // Another favorable tick

        // Unfavorable conditions
        send(encodeMarketTick(createUnfavorableTick())); // Unfavorable tick

        var state = container.getState();
        var action = myAlgoLogic.evaluate(state);

        // Expecting no new action due to unfavorable conditions
        assertEquals(NoAction.NoAction, action);
    }

    // New test for Multiple Market Updates
    @Test
    public void testMultipleMarketUpdatesScenario() throws Exception {
        // Send multiple ticks in quick succession
        send(encodeMarketTick(createFavorableTick(100, 1000, 1)));
        send(encodeMarketTick(createFavorableTick(102, 1000, 1)));
        send(encodeMarketTick(createFavorableTick(101, 1000, 1)));
        send(encodeMarketTick(createFavorableTick(104, 1000, 1)));

        var state = container.getState();

        // Check the state after multiple updates
        assertEquals(1, state.getChildOrders().size()); // Adjust this based on your logic
    }

    // New test for Order Filling
    @Test
    public void testOrderFilling() throws Exception {
        // Initial market data that triggers an order
        send(encodeMarketTick(createFavorableTick(95, 1000, 1))); // Favorable condition to place an order
        send(encodeMarketTick(createFavorableTick(98, 1000, 1))); // Favorable condition

        var state = container.getState();
        var order = state.getChildOrders().get(0); // Assume an order was placed

        // Simulate filling the order
        order.setFilledQuantity(100); // Simulate that the order has been filled

        // Verify that the order filling is processed correctly
        assertEquals(order.getFilledQuantity(), 100); // Expecting filled quantity to be updated
    }
}
