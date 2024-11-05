package codingblackfemales.sotw;

public enum OrderStatus {
    PENDING(1), // order is pending
    ACKED(2), // order processing
    CANCELED(3), // order has been cancelled
    FILLED(4); // order has been filled

    private final int value;

    OrderStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // Convert integer to OrderStatus
    public static OrderStatus fromInt(int value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid OrderStatus value: " + value);
    }
}
