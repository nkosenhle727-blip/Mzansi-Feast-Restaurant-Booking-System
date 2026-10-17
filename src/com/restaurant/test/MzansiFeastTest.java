package com.restaurant.test;

import com.restaurant.dao.BillDAO;
import com.restaurant.dao.ReservationDAO;
import com.restaurant.model.MenuItem;
import com.restaurant.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MzansiFeastTest {

    private ReservationDAO reservationDAO;
    private BillDAO billDAO;

    @BeforeEach
    void setUp() {
        reservationDAO = new ReservationDAO();
        billDAO = new BillDAO();
    }

    @Test
    @DisplayName("UT-01: Calculate Order Item Subtotal")
    void testOrderItemSubtotal() {
        MenuItem item = new MenuItem(1, "Mogodu & Dombolo", "Traditional dish", 110.0, "Main");
        OrderItem orderItem = new OrderItem(item, 3);

        double expectedSubtotal = 330.0;
        assertEquals(expectedSubtotal, orderItem.getSubtotal(), 0.001, "Subtotal calculation must equal unit price multiplied by quantity.");
    }

    @Test
    @DisplayName("UT-02: Slot Recommendation Engine (Algorithm 4.5.2)")
    void testAlternativeSlotRecommendations() {
        LocalDate testDate = LocalDate.of(2026, 10, 15);
        LocalTime requestedTime = LocalTime.of(13, 0);
        int tableNumber = 1;

        List<LocalTime> alternatives = reservationDAO.getAlternativeSlots(tableNumber, testDate, requestedTime);

        assertNotNull(alternatives, "Recommendation list should not be null.");
        assertFalse(alternatives.contains(requestedTime), "Recommended alternative slots must not contain the requested occupied slot.");
        assertTrue(alternatives.size() <= 3, "Recommendation engine should return at most 3 alternative slots.");
    }

    @Test
    @DisplayName("UT-03: Daily Sales Revenue Aggregation (Algorithm 4.5.5)")
    void testDailySalesAggregation() {
        LocalDate auditDate = LocalDate.of(2026, 9, 24);
        double totalSales = billDAO.getDailySalesTotal(auditDate);

        assertTrue(totalSales >= 0.0, "Daily gross revenue must return a non-negative value.");
    }
}