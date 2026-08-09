package com.marketmaker.interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.marketmaker.entities.Order;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderInputBoundary;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderRequestModel;
import com.marketmaker.use_case.place_order.PlaceOrderInputBoundary;
import com.marketmaker.use_case.place_order.PlaceOrderRequestModel;

/**
 * The controller's job is to read a free-text form and dispatch exactly one use case.
 * The tests that matter most are the negative ones: a ticket that cannot be read must
 * leave both interactors untouched.
 */
class OrderTicketControllerTest {

    private RecordingMarketInteractor market;
    private RecordingRestingInteractor resting;
    private OrderTicketController controller;

    @BeforeEach
    void setUp() {
        market = new RecordingMarketInteractor();
        resting = new RecordingRestingInteractor();
        controller = new OrderTicketController(market, resting, "demo");
    }

    @Test
    void marketOrderDispatchesToTheMarketUseCase() {
        assertNull(controller.place("aapl", Order.Side.BUY, Order.Type.MARKET, "10", ""));

        assertTrue(market.called);
        assertFalse(resting.called);
        assertEquals("AAPL", market.request.getTicker());
        assertEquals(10, market.request.getQuantity());
    }

    @Test
    void limitOrderDispatchesToTheRestingUseCaseWithItsTrigger() {
        assertNull(controller.place("msft", Order.Side.BUY, Order.Type.LIMIT, "5", "400.50"));

        assertTrue(resting.called);
        assertFalse(market.called);
        assertEquals(400.50, resting.request.getTriggerPrice());
        assertEquals(Order.Type.LIMIT, resting.request.getType());
    }

    @Test
    void anEmptyTickerRunsNothing() {
        assertEquals("Enter a ticker.", controller.place("   ", Order.Side.BUY, Order.Type.MARKET, "10", ""));

        assertFalse(market.called);
        assertFalse(resting.called);
    }

    @Test
    void anUnreadableQuantityRunsNothing() {
        assertEquals("Quantity must be a whole number.",
                controller.place("AAPL", Order.Side.BUY, Order.Type.MARKET, "ten", ""));

        assertFalse(market.called);
        assertFalse(resting.called);
    }

    /** The case from the review: a bad trigger must not leave a half-placed order behind. */
    @Test
    void anUnreadableLimitPriceRunsNothingEvenThoughQuantityWasFine() {
        assertEquals("Enter a limit price.",
                controller.place("AAPL", Order.Side.BUY, Order.Type.LIMIT, "10", ""));

        assertFalse(market.called);
        assertFalse(resting.called);
    }

    @Test
    void anUnreadableStopPriceSaysStopRatherThanLimit() {
        assertEquals("Enter a stop price.",
                controller.place("AAPL", Order.Side.SELL, Order.Type.STOP_LOSS, "10", "abc"));

        assertFalse(market.called);
        assertFalse(resting.called);
    }

    /**
     * A market order ignores the trigger box on purpose. Text left over from a limit order
     * must not block a valid market ticket.
     */
    @Test
    void aMarketOrderIgnoresLeftoverTextInTheTriggerBox() {
        assertNull(controller.place("AAPL", Order.Side.BUY, Order.Type.MARKET, "10", "not a price"));

        assertTrue(market.called);
        assertFalse(resting.called);
    }

    private static final class RecordingMarketInteractor implements PlaceOrderInputBoundary {
        private boolean called;
        private PlaceOrderRequestModel request;

        @Override
        public void execute(PlaceOrderRequestModel requestModel) {
            called = true;
            request = requestModel;
        }
    }

    private static final class RecordingRestingInteractor implements PlaceLimitStopOrderInputBoundary {
        private boolean called;
        private PlaceLimitStopOrderRequestModel request;

        @Override
        public void execute(PlaceLimitStopOrderRequestModel requestModel) {
            called = true;
            request = requestModel;
        }
    }
}
