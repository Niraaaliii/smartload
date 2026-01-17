package com.teleport.smartload.dto;

import com.teleport.smartload.model.Order;
import com.teleport.smartload.model.Truck;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class OptimizeRequest {

    @NotNull(message = "Truck is required")
    @Valid
    private Truck truck;

    @NotNull(message = "Orders list is required")
    @Size(max = 22, message = "Maximum 22 orders allowed")
    @Valid
    private List<Order> orders;

    public OptimizeRequest() {
    }

    public OptimizeRequest(Truck truck, List<Order> orders) {
        this.truck = truck;
        this.orders = orders;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
