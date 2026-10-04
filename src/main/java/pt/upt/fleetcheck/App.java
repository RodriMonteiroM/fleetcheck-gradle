package pt.upt.fleetcheck;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream input = App.class.getResourceAsStream("/vehicles.json")) {
            if (input == null) {
                throw new IllegalStateException("vehicles.json was not found");
            }

            List<Vehicle> vehicles = mapper.readValue(
                    input,
                    new TypeReference<List<Vehicle>>() {}
            );

            FleetService service = new FleetService();
            System.out.println("FleetCheck 1.0");
            System.out.println("Vehicles loaded: " + vehicles.size());
            System.out.println("Vehicles requiring service: "
                    + service.countVehiclesNeedingService(vehicles));
            System.out.printf("Average mileage: %.0f km%n",
                    service.averageMileage(vehicles));
        }
    }
}
