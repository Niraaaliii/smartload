package com.teleport.smartload.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class Truck {

    @NotBlank(message = "Truck ID is required")
    private String id;

    @Positive(message = "Max weight must be positive")
    private int maxWeightLbs;

    @Positive(message = "Max volume must be positive")
    private int maxVolumeCuft;

    public Truck() {
    }

    public Truck(String id, int maxWeightLbs, int maxVolumeCuft) {
        this.id = id;
        this.maxWeightLbs = maxWeightLbs;
        this.maxVolumeCuft = maxVolumeCuft;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxWeightLbs() {
        return maxWeightLbs;
    }

    public void setMaxWeightLbs(int maxWeightLbs) {
        this.maxWeightLbs = maxWeightLbs;
    }

    public int getMaxVolumeCuft() {
        return maxVolumeCuft;
    }

    public void setMaxVolumeCuft(int maxVolumeCuft) {
        this.maxVolumeCuft = maxVolumeCuft;
    }
}
