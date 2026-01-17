package com.teleport.smartload.service;

import com.teleport.smartload.dto.OptimizeRequest;
import com.teleport.smartload.dto.OptimizeResponse;
import com.teleport.smartload.model.Order;
import com.teleport.smartload.model.Truck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoadOptimizerServiceTest {

    private LoadOptimizerService service;
    private Truck defaultTruck;

    @BeforeEach
    void setUp() {
        service = new LoadOptimizerServiceImpl();
        defaultTruck = new Truck("truck-1", 44000, 3000);
    }

    private Order createOrder(String id, long payoutCents, int weightLbs, int volumeCuft,
            String origin, String destination, boolean isHazmat) {
        return new Order(id, payoutCents, weightLbs, volumeCuft, origin, destination,
                LocalDate.now(), LocalDate.now().plusDays(3), isHazmat);
    }

    @Nested
    @DisplayName("Basic Optimization Tests")
    class BasicOptimizationTests {

        @Test
        @DisplayName("Should select single order when only one fits")
        void selectsSingleOrder() {
            Order order = createOrder("ord-1", 100000, 20000, 1500, "LA", "Dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(order));

            OptimizeResponse response = service.optimize(request);

            assertEquals(1, response.getSelectedOrderIds().size());
            assertEquals("ord-1", response.getSelectedOrderIds().get(0));
            assertEquals(100000, response.getTotalPayoutCents());
        }

        @Test
        @DisplayName("Should select multiple orders when they fit together")
        void selectsMultipleOrders() {
            Order order1 = createOrder("ord-1", 100000, 15000, 1000, "LA", "Dallas", false);
            Order order2 = createOrder("ord-2", 80000, 12000, 800, "LA", "Dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(order1, order2));

            OptimizeResponse response = service.optimize(request);

            assertEquals(2, response.getSelectedOrderIds().size());
            assertTrue(response.getSelectedOrderIds().contains("ord-1"));
            assertTrue(response.getSelectedOrderIds().contains("ord-2"));
            assertEquals(180000, response.getTotalPayoutCents());
        }

        @Test
        @DisplayName("Should maximize payout, not number of orders")
        void maximizesPayout() {
            // One high-value order vs two low-value orders
            Order highValue = createOrder("high", 200000, 40000, 2800, "LA", "Dallas", false);
            Order low1 = createOrder("low1", 50000, 20000, 1000, "LA", "Dallas", false);
            Order low2 = createOrder("low2", 50000, 20000, 1000, "LA", "Dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(highValue, low1, low2));

            OptimizeResponse response = service.optimize(request);

            // Should pick high value (200k) over both low (100k combined)
            assertEquals(200000, response.getTotalPayoutCents());
            assertEquals(1, response.getSelectedOrderIds().size());
            assertEquals("high", response.getSelectedOrderIds().get(0));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should return empty response for null orders")
        void handlesNullOrders() {
            OptimizeRequest request = new OptimizeRequest(defaultTruck, null);

            OptimizeResponse response = service.optimize(request);

            assertTrue(response.getSelectedOrderIds().isEmpty());
            assertEquals(0, response.getTotalPayoutCents());
        }

        @Test
        @DisplayName("Should return empty response for empty orders list")
        void handlesEmptyOrders() {
            OptimizeRequest request = new OptimizeRequest(defaultTruck, new ArrayList<>());

            OptimizeResponse response = service.optimize(request);

            assertTrue(response.getSelectedOrderIds().isEmpty());
            assertEquals(0, response.getTotalPayoutCents());
        }

        @Test
        @DisplayName("Should return empty when all orders exceed capacity")
        void handlesAllOrdersTooLarge() {
            Order tooHeavy = createOrder("heavy", 100000, 50000, 1000, "LA", "Dallas", false);
            Order tooBig = createOrder("big", 100000, 10000, 5000, "LA", "Dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(tooHeavy, tooBig));

            OptimizeResponse response = service.optimize(request);

            assertTrue(response.getSelectedOrderIds().isEmpty());
        }
    }

    @Nested
    @DisplayName("Weight and Volume Constraints")
    class CapacityConstraints {

        @Test
        @DisplayName("Should not exceed max weight")
        void respectsWeightLimit() {
            Order order1 = createOrder("ord-1", 100000, 30000, 1000, "LA", "Dallas", false);
            Order order2 = createOrder("ord-2", 80000, 20000, 500, "LA", "Dallas", false);
            // Together: 50000 lbs > 44000 max
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(order1, order2));

            OptimizeResponse response = service.optimize(request);

            assertTrue(response.getTotalWeightLbs() <= 44000);
            // Should pick order1 (higher payout)
            assertEquals(100000, response.getTotalPayoutCents());
        }

        @Test
        @DisplayName("Should not exceed max volume")
        void respectsVolumeLimit() {
            Order order1 = createOrder("ord-1", 100000, 10000, 2000, "LA", "Dallas", false);
            Order order2 = createOrder("ord-2", 80000, 10000, 1500, "LA", "Dallas", false);
            // Together: 3500 cuft > 3000 max
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(order1, order2));

            OptimizeResponse response = service.optimize(request);

            assertTrue(response.getTotalVolumeCuft() <= 3000);
            assertEquals(100000, response.getTotalPayoutCents());
        }

        @Test
        @DisplayName("Should calculate utilization percentages correctly")
        void calculatesUtilizationCorrectly() {
            Order order = createOrder("ord-1", 100000, 22000, 1500, "LA", "Dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(order));

            OptimizeResponse response = service.optimize(request);

            // 22000/44000 = 50%
            assertEquals(50.0, response.getUtilizationWeightPercent(), 0.01);
            // 1500/3000 = 50%
            assertEquals(50.0, response.getUtilizationVolumePercent(), 0.01);
        }
    }

    @Nested
    @DisplayName("Hazmat Isolation Rules")
    class HazmatRules {

        @Test
        @DisplayName("Should not mix hazmat with regular cargo")
        void noMixingHazmatWithRegular() {
            Order hazmat = createOrder("hazmat", 150000, 15000, 1000, "LA", "Dallas", true);
            Order regular = createOrder("regular", 100000, 15000, 1000, "LA", "Dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(hazmat, regular));

            OptimizeResponse response = service.optimize(request);

            // Should only pick one (hazmat has higher payout)
            assertEquals(1, response.getSelectedOrderIds().size());
            assertEquals("hazmat", response.getSelectedOrderIds().get(0));
            assertEquals(150000, response.getTotalPayoutCents());
        }

        @Test
        @DisplayName("Should allow multiple hazmat orders together")
        void allowsMultipleHazmatTogether() {
            Order hazmat1 = createOrder("hazmat1", 100000, 10000, 500, "LA", "Dallas", true);
            Order hazmat2 = createOrder("hazmat2", 80000, 10000, 500, "LA", "Dallas", true);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(hazmat1, hazmat2));

            OptimizeResponse response = service.optimize(request);

            assertEquals(2, response.getSelectedOrderIds().size());
            assertEquals(180000, response.getTotalPayoutCents());
        }

        @Test
        @DisplayName("Should allow multiple regular orders together")
        void allowsMultipleRegularTogether() {
            Order reg1 = createOrder("reg1", 100000, 10000, 500, "LA", "Dallas", false);
            Order reg2 = createOrder("reg2", 80000, 10000, 500, "LA", "Dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(reg1, reg2));

            OptimizeResponse response = service.optimize(request);

            assertEquals(2, response.getSelectedOrderIds().size());
            assertEquals(180000, response.getTotalPayoutCents());
        }
    }

    @Nested
    @DisplayName("Route Compatibility")
    class RouteCompatibility {

        @Test
        @DisplayName("Should only combine orders with same origin and destination")
        void sameRoutesOnly() {
            Order la_dallas = createOrder("la-dal", 100000, 15000, 1000, "LA", "Dallas", false);
            Order la_houston = createOrder("la-hou", 80000, 10000, 800, "LA", "Houston", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(la_dallas, la_houston));

            OptimizeResponse response = service.optimize(request);

            // Should pick higher payout single route
            assertEquals(1, response.getSelectedOrderIds().size());
            assertEquals("la-dal", response.getSelectedOrderIds().get(0));
        }

        @Test
        @DisplayName("Should handle case-insensitive route matching")
        void caseInsensitiveRoutes() {
            Order order1 = createOrder("ord1", 100000, 15000, 1000, "LA", "Dallas", false);
            Order order2 = createOrder("ord2", 80000, 10000, 800, "la", "dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(order1, order2));

            OptimizeResponse response = service.optimize(request);

            assertEquals(2, response.getSelectedOrderIds().size());
            assertEquals(180000, response.getTotalPayoutCents());
        }
    }

    @Nested
    @DisplayName("Date Validation")
    class DateValidation {

        @Test
        @DisplayName("Should filter out orders where pickup is after delivery")
        void filterInvalidDates() {
            Order valid = createOrder("valid", 100000, 15000, 1000, "LA", "Dallas", false);
            Order invalid = new Order("invalid", 200000, 10000, 500, "LA", "Dallas",
                    LocalDate.now().plusDays(5), LocalDate.now(), false); // pickup after delivery
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(valid, invalid));

            OptimizeResponse response = service.optimize(request);

            assertEquals(1, response.getSelectedOrderIds().size());
            assertEquals("valid", response.getSelectedOrderIds().get(0));
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle maximum 22 orders within reasonable time")
        void handles22OrdersQuickly() {
            List<Order> orders = new ArrayList<>();
            for (int i = 0; i < 22; i++) {
                orders.add(createOrder("ord-" + i, 10000 + i * 1000, 2000, 100, "LA", "Dallas", false));
            }
            OptimizeRequest request = new OptimizeRequest(defaultTruck, orders);

            long startTime = System.currentTimeMillis();
            OptimizeResponse response = service.optimize(request);
            long duration = System.currentTimeMillis() - startTime;

            // Should complete within 2 seconds as per requirement
            assertTrue(duration < 2000, "Should complete within 2 seconds, took: " + duration + "ms");
            assertNotNull(response);
            assertFalse(response.getSelectedOrderIds().isEmpty());
        }

        @Test
        @DisplayName("Should handle mixed hazmat and regular orders efficiently")
        void handles22MixedOrdersQuickly() {
            List<Order> orders = new ArrayList<>();
            for (int i = 0; i < 22; i++) {
                orders.add(createOrder("ord-" + i, 10000 + i * 1000, 2000, 100, "LA", "Dallas", i % 3 == 0));
            }
            OptimizeRequest request = new OptimizeRequest(defaultTruck, orders);

            long startTime = System.currentTimeMillis();
            OptimizeResponse response = service.optimize(request);
            long duration = System.currentTimeMillis() - startTime;

            assertTrue(duration < 2000, "Should complete within 2 seconds, took: " + duration + "ms");
            assertNotNull(response);
        }
    }

    @Nested
    @DisplayName("Response Format Tests")
    class ResponseFormatTests {

        @Test
        @DisplayName("Should return correct truck ID")
        void returnsTruckId() {
            Order order = createOrder("ord-1", 100000, 10000, 500, "LA", "Dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(order));

            OptimizeResponse response = service.optimize(request);

            assertEquals("truck-1", response.getTruckId());
        }

        @Test
        @DisplayName("Should return all selected order IDs")
        void returnsAllOrderIds() {
            Order ord1 = createOrder("ord-1", 100000, 10000, 500, "LA", "Dallas", false);
            Order ord2 = createOrder("ord-2", 80000, 10000, 500, "LA", "Dallas", false);
            Order ord3 = createOrder("ord-3", 60000, 10000, 500, "LA", "Dallas", false);
            OptimizeRequest request = new OptimizeRequest(defaultTruck, List.of(ord1, ord2, ord3));

            OptimizeResponse response = service.optimize(request);

            assertEquals(3, response.getSelectedOrderIds().size());
            assertTrue(response.getSelectedOrderIds().containsAll(List.of("ord-1", "ord-2", "ord-3")));
        }
    }
}
