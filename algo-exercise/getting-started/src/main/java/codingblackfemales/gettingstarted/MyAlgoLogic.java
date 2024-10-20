package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.NoAction;
import codingblackfemales.action.CancelChildOrder; // Import CancelOrderAction
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.util.Util;
import messages.order.Side;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    // Define a constant for the maximum order lifetime (in milliseconds)
    public static final long MAX_ORDER_LIFETIME = 60000; // Example: 60 seconds

    // Define a constant for the buy threshold (price below which to buy)
    private static final double BUY_THRESHOLD = 100.0; // Example threshold price

    @Override
    public Action evaluate(SimpleAlgoState state) {

        // Log the current state of the order book
        var orderBookAsString = Util.orderBookToString(state);
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        // Handle order cancellation due to timeout
        List<ChildOrder> activeOrders = state.getActiveChildOrders();
        long currentTime = System.currentTimeMillis();

        for (ChildOrder order : activeOrders) {
            long orderLifetime = currentTime - order.getCreationTime();
            if (orderLifetime > MAX_ORDER_LIFETIME) {
                logger.info("[MYALGO] Canceling order due to timeout: " + order);
                return new CancelChildOrder(order); // Cancel the order
            }
        }

        // Check the best ask price and place a buy order if necessary
        if (state.getAskLevels() > 0) {
            // Ensure there are sell levels
            var bestAsk = state.getAskAt(0); // Get the lowest selling price
            double bestAskPrice = bestAsk.getPrice();

            // Place a buy order if the best ask price is below the threshold
            if (bestAskPrice < BUY_THRESHOLD) {
                logger.info("[MYALGO] Best ask is below threshold, placing buy order.");
                return new CreateChildOrder(Side.BUY, 10L, Math.round(bestAskPrice)); // Buy 10 units at the best askprice
            }
        }

        // No action to take if none of the conditions are met
        logger.info("[MYALGO] No action taken.");
        return NoAction.NoAction;
    }
}
