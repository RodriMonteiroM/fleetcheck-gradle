package pt.upt.fleetcheck;

public record Vehicle(
        String id,
        String model,
        int mileageKm,
        int lastServiceKm,
        int serviceIntervalKm) {
}
