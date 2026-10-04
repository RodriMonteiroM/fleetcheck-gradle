package pt.upt.fleetcheck;

import java.util.List;

public class FleetService {

    public boolean needsService(Vehicle vehicle) {
        int kilometresSinceService = vehicle.mileageKm() - vehicle.lastServiceKm();
        // Service is due once the interval is reached (boundary inclusive).
        return kilometresSinceService >= vehicle.serviceIntervalKm();
    }

    public long countVehiclesNeedingService(List<Vehicle> vehicles) {
        long count = 0;
        for (Vehicle vehicle : vehicles) {
            if (needsService(vehicle)) {
                count++;
            }
        }
        return count;
    }

    public double averageMileage(List<Vehicle> vehicles) {
        if (vehicles.isEmpty()) {
            return 0.0;
        }
        long total = 0;
        for (Vehicle vehicle : vehicles) {
            total += vehicle.mileageKm();
        }
        return (double) total / vehicles.size();
    }
}
