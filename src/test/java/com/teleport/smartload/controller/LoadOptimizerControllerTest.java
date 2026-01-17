package com.teleport.smartload.controller;

import com.teleport.smartload.dto.OptimizeRequest;
import com.teleport.smartload.dto.OptimizeResponse;
import com.teleport.smartload.model.Order;
import com.teleport.smartload.model.Truck;
import com.teleport.smartload.service.LoadOptimizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoadOptimizerControllerTest {

    private LoadOptimizerController controller;
    private LoadOptimizerService mockService;

    @BeforeEach
    void setUp() {
        mockService = mock(LoadOptimizerService.class);
        controller = new LoadOptimizerController(mockService);
    }

    private Order createOrder(String id, long payoutCents, int weightLbs, int volumeCuft) {
        return new Order(id, payoutCents, weightLbs, volumeCuft, "LA", "Dallas",
                LocalDate.now(), LocalDate.now().plusDays(3), false);
    }

    @Test
    @DisplayName("optimize should return 200 OK with response from service")
    void optimizeReturnsOk() {
        Truck truck = new Truck("truck-1", 44000, 3000);
        Order order = createOrder("ord-1", 100000, 20000, 1500);
        OptimizeRequest request = new OptimizeRequest(truck, List.of(order));

        OptimizeResponse expectedResponse = new OptimizeResponse(
                "truck-1", List.of("ord-1"), 100000, 20000, 1500, 45.45, 50.0);
        when(mockService.optimize(any(OptimizeRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<OptimizeResponse> response = controller.optimize(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("truck-1", response.getBody().getTruckId());
        assertEquals(List.of("ord-1"), response.getBody().getSelectedOrderIds());
        assertEquals(100000, response.getBody().getTotalPayoutCents());
    }

    @Test
    @DisplayName("optimize should delegate to service")
    void optimizeDelegatesToService() {
        Truck truck = new Truck("truck-1", 44000, 3000);
        OptimizeRequest request = new OptimizeRequest(truck, List.of());

        OptimizeResponse expectedResponse = new OptimizeResponse(
                "truck-1", List.of(), 0, 0, 0, 0.0, 0.0);
        when(mockService.optimize(any(OptimizeRequest.class))).thenReturn(expectedResponse);

        controller.optimize(request);

        verify(mockService, times(1)).optimize(request);
    }

    @Test
    @DisplayName("optimize should return response with correct structure")
    void optimizeReturnsCorrectStructure() {
        Truck truck = new Truck("truck-1", 44000, 3000);
        Order order = createOrder("ord-1", 100000, 22000, 1500);
        OptimizeRequest request = new OptimizeRequest(truck, List.of(order));

        OptimizeResponse expectedResponse = new OptimizeResponse(
                "truck-1", List.of("ord-1"), 100000, 22000, 1500, 50.0, 50.0);
        when(mockService.optimize(any(OptimizeRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<OptimizeResponse> response = controller.optimize(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50.0, response.getBody().getUtilizationWeightPercent());
        assertEquals(50.0, response.getBody().getUtilizationVolumePercent());
    }
}
