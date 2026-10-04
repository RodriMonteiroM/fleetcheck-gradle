package pt.upt.fleetcheck;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetServiceTest {

    private final FleetService service = new FleetService();

    @Test
    void vehicleBelowIntervalDoesNotNeedService() {
        Vehicle vehicle = new Vehicle("V2", "Hybrid", 22000, 15000, 10000);
        assertFalse(service.needsService(vehicle));
    }

    @Test
    void vehicleExactlyAtIntervalNeedsService() {
        Vehicle vehicle = new Vehicle("V3", "Diesel", 65000, 55000, 10000);
        assertTrue(service.needsService(vehicle));
    }

    @Test
    void vehicleAboveIntervalNeedsService() {
        Vehicle vehicle = new Vehicle("V1", "EV", 52000, 40000, 10000);
        assertTrue(service.needsService(vehicle));
    }

    @Test
    void countsVehiclesNeedingService() {
        List<Vehicle> vehicles = List.of(
                new Vehicle("V1", "EV", 52000, 40000, 10000),
                new Vehicle("V2", "Hybrid", 22000, 15000, 10000),
                new Vehicle("V3", "Diesel", 65000, 55000, 10000),
                new Vehicle("V4", "Petrol", 9000, 0, 10000));
        assertEquals(2, service.countVehiclesNeedingService(vehicles));
    }

    @Test
    void averageMileageOfFleet() {
        List<Vehicle> vehicles = List.of(
                new Vehicle("V1", "EV", 52000, 40000, 10000),
                new Vehicle("V4", "Petrol", 22000, 0, 10000));
        assertEquals(37000.0, service.averageMileage(vehicles));
    }

    @Test
    void averageMileageOfEmptyFleetIsZero() {
        assertEquals(0.0, service.averageMileage(List.of()));
    }
}
