package com.teleport.smartload.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public class Order {

    @NotBlank(message = "Order ID is required")
    private String id;

    @PositiveOrZero(message = "Payout must be non-negative")
    @JsonProperty("payout_cents")
    private long payoutCents;

    @Positive(message = "Weight must be positive")
    @JsonProperty("weight_lbs")
    private int weightLbs;

    @Positive(message = "Volume must be positive")
    @JsonProperty("volume_cuft")
    private int volumeCuft;

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Pickup date is required")
    @JsonProperty("pickup_date")
    private LocalDate pickupDate;

    @NotNull(message = "Delivery date is required")
    @JsonProperty("delivery_date")
    private LocalDate deliveryDate;

    @JsonProperty("is_hazmat")
    private boolean isHazmat;

    public Order() {
    }

    public Order(String id, long payoutCents, int weightLbs, int volumeCuft,
            String origin, String destination, LocalDate pickupDate,
            LocalDate deliveryDate, boolean isHazmat) {
        this.id = id;
        this.payoutCents = payoutCents;
        this.weightLbs = weightLbs;
        this.volumeCuft = volumeCuft;
        this.origin = origin;
        this.destination = destination;
        this.pickupDate = pickupDate;
        this.deliveryDate = deliveryDate;
        this.isHazmat = isHazmat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPayoutCents() {
        return payoutCents;
    }

    public void setPayoutCents(long payoutCents) {
        this.payoutCents = payoutCents;
    }

    public int getWeightLbs() {
        return weightLbs;
    }

    public void setWeightLbs(int weightLbs) {
        this.weightLbs = weightLbs;
    }

    public int getVolumeCuft() {
        return volumeCuft;
    }

    public void setVolumeCuft(int volumeCuft) {
        this.volumeCuft = volumeCuft;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public boolean isHazmat() {
        return isHazmat;
    }

    public void setHazmat(boolean hazmat) {
        isHazmat = hazmat;
    }
}
