package codingblackfemales.gettingstarted;

import codingblackfemales.action.Action;
import codingblackfemales.action.CancelChildOrder;
import codingblackfemales.action.CreateChildOrder;
import codingblackfemales.action.NoAction;
import codingblackfemales.algo.AlgoLogic;
import codingblackfemales.sotw.ChildOrder;
import codingblackfemales.sotw.SimpleAlgoState;
import codingblackfemales.util.Util;
import messages.order.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MyAlgoLogic implements AlgoLogic {

    private static final Logger logger = LoggerFactory.getLogger(MyAlgoLogic.class);

    // Set the maximum order lifetime to 3 minutes (180000 milliseconds)
    public static final long MAX_ORDER_LIFETIME = 180000; // 3 minutes

    // Define a constant for the buy threshold (price below which to buy)
    private static final double BUY_THRESHOLD = 115.0; // Example threshold price

    @Override
    public Action evaluate(SimpleAlgoState state) {
        // Log the current state of the order book
        var orderBookAsString = Util.orderBookToString(state);
        logger.info("[MYALGO] The state of the order book is:\n" + orderBookAsString);

        List<ChildOrder> activeOrders = state.getActiveChildOrders();
        long currentTime = System.currentTimeMillis();

        // Cancel any active order if it has timed out
        for (ChildOrder order : activeOrders) {
            long orderLifetime = currentTime - order.getCreationTime();
            if (orderLifetime > MAX_ORDER_LIFETIME) {
                logger.info("[MYALGO] Canceling order due to timeout: " + order);
                return new CancelChildOrder(order);
            }
        }

        // Check the best ask price and place a buy order if below threshold
        if (state.getAskLevels() > 0) {
            var bestAsk = state.getAskAt(0);
            double bestAskPrice = bestAsk.getPrice();

            // Check if conditions are favorable and no active orders are present
            if (bestAskPrice < BUY_THRESHOLD && activeOrders.isEmpty()) {
                logger.info("[MYALGO] Best ask is below threshold, placing buy order.");
                return new CreateChildOrder(Side.BUY, 10L, Math.round(bestAskPrice));
            }
        }

        // No action taken if none of the conditions are met
        logger.info("[MYALGO] No action taken."); 
        return NoAction.NoAction;
    }
}