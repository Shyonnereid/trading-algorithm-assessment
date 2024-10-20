package codingblackfemales.sotw;

import messages.order.Side; // Assuming Side is defined in another package
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ChildOrder {
    private Side side;
    private long orderId;
    private long quantity;
    private long price;
    private long filledQuantity; // Store total filled quantity as long

    private OrderStatus orderStatus; // Field to hold the order status
    private long creationTime; // Add creationTime field
    private List<ChildFill> fills; // Declare the fills list

    public ChildOrder(Side side, long orderId, long quantity, long price, OrderStatus orderStatus) {
        this.side = side;
        this.orderId = orderId;
        this.quantity = quantity;
        this.price = price;
        this.orderStatus = orderStatus; // Set the order status in the constructor

        this.creationTime = System.currentTimeMillis(); // Initialize creation time when the order is placed
        this.fills = new LinkedList<>(); // Initialize the fills list
        this.filledQuantity = 0; // Initialize filled quantity
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime; // Store the creation time
    }

    public OrderStatus getStatus() {
        return orderStatus;
    }

    public Side getSide() {
        return side;
    }

    public long getOrderId() {
        return orderId;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getPrice() {
        return price;
    }

    public long getFilledQuantity() {
        return filledQuantity; // Directly return filled quantity
    }

    public void setFilledQuantity(long filledQuantity) {
        this.filledQuantity = filledQuantity; // Set filled quantity
    }

    public long getTotalFilledQuantity() {
        return fills.stream().map(ChildFill::getQuantity).collect(Collectors.summingLong(Long::longValue));
    }

    // Add a method to update order status if needed
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void addFill(long filledQuantity, long filledPrice) {
        this.fills.add(new ChildFill(filledQuantity, filledPrice)); // Add a new fill to the list
        this.filledQuantity += filledQuantity; // Update filled quantity when a new fill is added
    }
}
