package com.teleport.smartload.service;

import com.teleport.smartload.dto.OptimizeRequest;
import com.teleport.smartload.dto.OptimizeResponse;
import com.teleport.smartload.model.Order;
import com.teleport.smartload.model.Truck;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoadOptimizerServiceImpl implements LoadOptimizerService {

    // Track best solution found during backtracking
    private long bestPayout;
    private List<Order> bestCombination;

    @Override
    public OptimizeResponse optimize(OptimizeRequest request) {
        Truck truck = request.getTruck();
        List<Order> orders = request.getOrders();

        // Handle empty orders
        if (orders == null || orders.isEmpty()) {
            return buildResponse(truck, new ArrayList<>());
        }

        // Filter valid orders (pickup <= delivery, fits in truck individually)
        List<Order> validOrders = filterValidOrders(orders, truck);

        if (validOrders.isEmpty()) {
            return buildResponse(truck, new ArrayList<>());
        }

        // Reset tracking variables
        bestPayout = 0;
        bestCombination = new ArrayList<>();

        // Run recursive backtracking
        backtrack(validOrders, truck, 0, new ArrayList<>(), 0, 0, 0);

        return buildResponse(truck, bestCombination);
    }

    /**
     * Filter orders that are valid:
     * - pickup date <= delivery date
     * - weight fits in truck
     * - volume fits in truck
     */
    private List<Order> filterValidOrders(List<Order> orders, Truck truck) {
        List<Order> valid = new ArrayList<>();
        for (Order order : orders) {
            // Check date validity
            if (order.getPickupDate() != null && order.getDeliveryDate() != null
                    && order.getPickupDate().isAfter(order.getDeliveryDate())) {
                continue; // Invalid: pickup after delivery
            }
            // Check if order can fit in truck individually
            if (order.getWeightLbs() <= truck.getMaxWeightLbs()
                    && order.getVolumeCuft() <= truck.getMaxVolumeCuft()) {
                valid.add(order);
            }
        }
        return valid;
    }

    /**
     * Recursive backtracking to find optimal combination
     */
    private void backtrack(List<Order> orders, Truck truck, int index,
            List<Order> current, long currentPayout,
            int currentWeight, int currentVolume) {

        // Update best if current is better
        if (currentPayout > bestPayout) {
            bestPayout = currentPayout;
            bestCombination = new ArrayList<>(current);
        }

        // Base case: processed all orders
        if (index >= orders.size()) {
            return;
        }

        // Try each remaining order
        for (int i = index; i < orders.size(); i++) {
            Order order = orders.get(i);

            int newWeight = currentWeight + order.getWeightLbs();
            int newVolume = currentVolume + order.getVolumeCuft();

            // Pruning: skip if exceeds capacity
            if (newWeight > truck.getMaxWeightLbs() || newVolume > truck.getMaxVolumeCuft()) {
                continue;
            }

            // Check compatibility with current selection
            if (!isCompatible(order, current)) {
                continue;
            }

            // Add order and recurse
            current.add(order);
            backtrack(orders, truck, i + 1, current,
                    currentPayout + order.getPayoutCents(), newWeight, newVolume);
            current.remove(current.size() - 1); // Backtrack
        }
    }

    /**
     * Check if order is compatible with current selection:
     * - Same origin and destination (route compatibility)
     * - Hazmat isolation: cannot mix hazmat with non-hazmat
     */
    private boolean isCompatible(Order newOrder, List<Order> current) {
        if (current.isEmpty()) {
            return true;
        }

        Order first = current.get(0);

        // Route compatibility: same origin and destination
        if (!newOrder.getOrigin().equalsIgnoreCase(first.getOrigin()) ||
                !newOrder.getDestination().equalsIgnoreCase(first.getDestination())) {
            return false;
        }

        // Hazmat isolation: all must be hazmat or all must be non-hazmat
        boolean currentHasHazmat = current.stream().anyMatch(Order::isHazmat);
        boolean currentHasNonHazmat = current.stream().anyMatch(o -> !o.isHazmat());

        if (newOrder.isHazmat() && currentHasNonHazmat) {
            return false; // Cannot add hazmat to non-hazmat load
        }
        if (!newOrder.isHazmat() && currentHasHazmat) {
            return false; // Cannot add non-hazmat to hazmat load
        }

        return true;
    }

    /**
     * Build response from selected orders
     */
    private OptimizeResponse buildResponse(Truck truck, List<Order> selected) {
        List<String> orderIds = new ArrayList<>();
        long totalPayout = 0;
        int totalWeight = 0;
        int totalVolume = 0;

        for (Order order : selected) {
            orderIds.add(order.getId());
            totalPayout += order.getPayoutCents();
            totalWeight += order.getWeightLbs();
            totalVolume += order.getVolumeCuft();
        }

        double weightPercent = truck.getMaxWeightLbs() > 0
                ? (totalWeight * 100.0) / truck.getMaxWeightLbs()
                : 0;
        double volumePercent = truck.getMaxVolumeCuft() > 0
                ? (totalVolume * 100.0) / truck.getMaxVolumeCuft()
                : 0;

        // Round to 2 decimal places
        weightPercent = Math.round(weightPercent * 100.0) / 100.0;
        volumePercent = Math.round(volumePercent * 100.0) / 100.0;

        return new OptimizeResponse(
                truck.getId(),
                orderIds,
                totalPayout,
                totalWeight,
                totalVolume,
                weightPercent,
                volumePercent);
    }
}
