package com.teleport.smartload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OptimizeResponse {

    @JsonProperty("truck_id")
    private String truckId;

    @JsonProperty("selected_order_ids")
    private List<String> selectedOrderIds;

    @JsonProperty("total_payout_cents")
    private long totalPayoutCents;

    @JsonProperty("total_weight_lbs")
    private int totalWeightLbs;

    @JsonProperty("total_volume_cuft")
    private int totalVolumeCuft;

    @JsonProperty("utilization_weight_percent")
    private double utilizationWeightPercent;

    @JsonProperty("utilization_volume_percent")
    private double utilizationVolumePercent;

    public OptimizeResponse() {
    }

    public OptimizeResponse(String truckId, List<String> selectedOrderIds,
            long totalPayoutCents, int totalWeightLbs, int totalVolumeCuft,
            double utilizationWeightPercent, double utilizationVolumePercent) {
        this.truckId = truckId;
        this.selectedOrderIds = selectedOrderIds;
        this.totalPayoutCents = totalPayoutCents;
        this.totalWeightLbs = totalWeightLbs;
        this.totalVolumeCuft = totalVolumeCuft;
        this.utilizationWeightPercent = utilizationWeightPercent;
        this.utilizationVolumePercent = utilizationVolumePercent;
    }

    public String getTruckId() {
        return truckId;
    }

    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }

    public List<String> getSelectedOrderIds() {
        return selectedOrderIds;
    }

    public void setSelectedOrderIds(List<String> selectedOrderIds) {
        this.selectedOrderIds = selectedOrderIds;
    }

    public long getTotalPayoutCents() {
        return totalPayoutCents;
    }

    public void setTotalPayoutCents(long totalPayoutCents) {
        this.totalPayoutCents = totalPayoutCents;
    }

    public int getTotalWeightLbs() {
        return totalWeightLbs;
    }

    public void setTotalWeightLbs(int totalWeightLbs) {
        this.totalWeightLbs = totalWeightLbs;
    }

    public int getTotalVolumeCuft() {
        return totalVolumeCuft;
    }

    public void setTotalVolumeCuft(int totalVolumeCuft) {
        this.totalVolumeCuft = totalVolumeCuft;
    }

    public double getUtilizationWeightPercent() {
        return utilizationWeightPercent;
    }

    public void setUtilizationWeightPercent(double utilizationWeightPercent) {
        this.utilizationWeightPercent = utilizationWeightPercent;
    }

    public double getUtilizationVolumePercent() {
        return utilizationVolumePercent;
    }

    public void setUtilizationVolumePercent(double utilizationVolumePercent) {
        this.utilizationVolumePercent = utilizationVolumePercent;
    }
}
