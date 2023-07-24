package codingblackfemales.orderbook.order;

import codingblackfemales.orderbook.OrderBookLevel;
import codingblackfemales.orderbook.OrderBookSide;
import codingblackfemales.orderbook.visitor.OrderBookVisitor;

public class DefaultOrderFlyweight extends ParentOrderFlyweight<DefaultOrderFlyweight>{

    @Override
    public void accept(OrderBookVisitor visitor, OrderBookSide side, OrderBookLevel level, boolean isLast) {
        visitor.visit(this, side, level, isLast);
    }
}