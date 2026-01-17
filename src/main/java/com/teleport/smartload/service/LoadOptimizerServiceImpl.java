package com.teleport.smartload.service;

import com.teleport.smartload.dto.OptimizeRequest;
import com.teleport.smartload.dto.OptimizeResponse;
import com.teleport.smartload.model.Order;
import com.teleport.smartload.model.Truck;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LoadOptimizerServiceImpl implements LoadOptimizerService {

    /**
     * Thread-safe holder for tracking best result during backtracking
     */
    private static class Result {
        long bestPayout = 0;
        List<Order> bestCombination = new ArrayList<>();
    }

    @Override
    public OptimizeResponse optimize(OptimizeRequest request) {
        Truck truck = request.getTruck();
        List<Order> orders = request.getOrders();

        if (orders == null || orders.isEmpty()) {
            return buildResponse(truck, new ArrayList<>());
        }

        List<Order> validOrders = filterValidOrders(orders, truck);

        if (validOrders.isEmpty()) {
            return buildResponse(truck, new ArrayList<>());
        }

        // Sort by payout density (payout per unit of capacity used) for better pruning
        validOrders.sort(Comparator.comparingDouble(
                o -> -((double) o.getPayoutCents() / (o.getWeightLbs() + o.getVolumeCuft()))));

        // Precompute suffix sums for pruning - max possible payout from index i onwards
        long[] suffixPayouts = new long[validOrders.size() + 1];
        for (int i = validOrders.size() - 1; i >= 0; i--) {
            suffixPayouts[i] = suffixPayouts[i + 1] + validOrders.get(i).getPayoutCents();
        }

        Result result = new Result();
        backtrack(validOrders, truck, 0, new ArrayList<>(), 0, 0, 0, result, suffixPayouts);

        return buildResponse(truck, result.bestCombination);
    }

    /**
     * Filters out orders that don't make sense - like if pickup is after delivery
     * or if the order is too big for the truck
     */
    private List<Order> filterValidOrders(List<Order> orders, Truck truck) {
        List<Order> valid = new ArrayList<>();
        for (Order order : orders) {
            // skip if pickup date is after delivery - that's not valid
            if (order.getPickupDate() != null && order.getDeliveryDate() != null
                    && order.getPickupDate().isAfter(order.getDeliveryDate())) {
                continue;
            }
            // only add if it could actually fit on the truck
            if (order.getWeightLbs() <= truck.getMaxWeightLbs()
                    && order.getVolumeCuft() <= truck.getMaxVolumeCuft()) {
                valid.add(order);
            }
        }
        return valid;
    }

    /**
     * Uses backtracking with pruning to find the combination with max payout
     */
    private void backtrack(List<Order> orders, Truck truck, int index,
            List<Order> current, long currentPayout,
            int currentWeight, int currentVolume,
            Result result, long[] suffixPayouts) {

        // found a better combo? save it
        if (currentPayout > result.bestPayout) {
            result.bestPayout = currentPayout;
            result.bestCombination = new ArrayList<>(current);
        }

        if (index >= orders.size()) {
            return;
        }

        // Pruning: if current + all remaining can't beat best, skip this branch
        if (currentPayout + suffixPayouts[index] <= result.bestPayout) {
            return;
        }

        for (int i = index; i < orders.size(); i++) {
            Order order = orders.get(i);

            int newWeight = currentWeight + order.getWeightLbs();
            int newVolume = currentVolume + order.getVolumeCuft();

            // would exceed truck capacity - skip this one
            if (newWeight > truck.getMaxWeightLbs() || newVolume > truck.getMaxVolumeCuft()) {
                continue;
            }

            // can't mix incompatible orders
            if (!isCompatible(order, current)) {
                continue;
            }

            current.add(order);
            backtrack(orders, truck, i + 1, current,
                    currentPayout + order.getPayoutCents(), newWeight, newVolume,
                    result, suffixPayouts);
            current.remove(current.size() - 1);
        }
    }

    /**
     * Checks if we can add this order to the current load
     * - must be going to the same place
     * - can't mix hazmat with regular cargo
     */
    private boolean isCompatible(Order newOrder, List<Order> current) {
        if (current.isEmpty()) {
            return true;
        }

        Order first = current.get(0);

        // different route? can't combine
        if (!newOrder.getOrigin().equalsIgnoreCase(first.getOrigin()) ||
                !newOrder.getDestination().equalsIgnoreCase(first.getDestination())) {
            return false;
        }

        // hazmat rules - can't mix hazmat with non-hazmat
        boolean hasHazmat = current.stream().anyMatch(Order::isHazmat);
        boolean hasRegular = current.stream().anyMatch(o -> !o.isHazmat());

        if (newOrder.isHazmat() && hasRegular) {
            return false;
        }
        if (!newOrder.isHazmat() && hasHazmat) {
            return false;
        }

        return true;
    }

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
