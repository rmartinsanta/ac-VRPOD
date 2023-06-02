package es.urjc.etsii.grafo.VRPOD.model.instance;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;

public class VRPODInstanceImporter extends InstanceImporter<VRPODInstance> {

    @Override
    public VRPODInstance importInstance(BufferedReader reader, String filename) throws IOException {

        boolean vertexData = false;
        boolean oddata = false;
        int currentDriver = 0;
        double currentX = 0, currentY = 0;
        boolean nextLineCustomers = false;

        boolean parsedDimension = false;
        boolean parsedCapacity = false;
        boolean parsedNumOcassionalDrivers = false;

        int customersToParse = -1;

        VRPODInstance instance = null;
        int numberOfDestinations = -1;
        int capacity = -1;
        int numOccasionalDrivers = -1;
        Point2D[] clientsCoordinates = null;

        String line;
        while ((line = reader.readLine()) != null) {
            // Number of clientsCoordinates
            if (line.startsWith("DIMENSION:")) {
                if (parsedDimension)
                    throw new IllegalStateException("Duplicated DIMENSION inside file: " + filename);
                numberOfDestinations = Integer.parseInt(line.split(":")[1].trim());
                parsedDimension = true;
            }
            // Capacity
            if (line.startsWith("VEHICLE CAPACITY:")) {
                if (parsedCapacity)
                    throw new IllegalStateException("Duplicated CAPACITY inside file: " + filename);
                capacity = Integer.parseInt(line.split(":")[1].trim());
                parsedCapacity = true;
            }
            // Number of ODs
            if (line.startsWith("NUMBER OF OCCASIONAL DRIVERS:")) {
                if (parsedNumOcassionalDrivers)
                    throw new IllegalStateException("Duplicated NUMOCASSIONALDRIVERS inside file: " + filename);
                numOccasionalDrivers = Integer.parseInt(line.split(":")[1].trim());
                parsedNumOcassionalDrivers = true;


            }

            // Data of vertex
            if (vertexData) {
                String[] parts = line.split("\t");
                int i = Integer.parseInt(parts[0].trim());
                int x = Integer.parseInt(parts[1].trim());
                int y = Integer.parseInt(parts[2].trim());
                clientsCoordinates[i] = new Point2D(x, y);
                instance.packetSizes[i] = Integer.parseInt(parts[3].trim());
                if (parts.length == 5) {
                    instance.setProfit(-1, i, Double.parseDouble(parts[4].trim()));
                }

                if (i == (instance.numberOfDestinations - 1)) {
                    vertexData = false;
                }
            } else {
                if (line.startsWith("VERTEX_ID")) {
                    assert numOccasionalDrivers != -1;
                    assert capacity != -1;
                    assert numberOfDestinations != -1;
                    instance = line.trim().endsWith("VERTEX_PROFIT") ?
                            new VRPODInstanceProfitODIndependant(filename, numOccasionalDrivers, capacity, numberOfDestinations) :
                            new VRPODInstanceProfitODDependant(filename, numOccasionalDrivers, capacity, numberOfDestinations);

                    vertexData = true;
                    clientsCoordinates = new Point2D[instance.numberOfDestinations];
                    instance.packetSizes = new int[instance.numberOfDestinations];

                }
            }

            // Occasional drivers data
            if (oddata) {
                if (line.startsWith("OCCASIONAL DRIVER ")) {
                    currentDriver = Integer.parseInt(line.trim().split(" ")[2]);
                }
                if (line.startsWith("x coordinate:")) {
                    currentX = Double.parseDouble(line.split(":")[1].trim());
                }
                if (line.startsWith("y coordinate:")) {
                    currentY = Double.parseDouble(line.split(":")[1].trim());
                }
                if (line.startsWith("Number of customers")) {
                    instance.odsCoordinates[currentDriver] = new Point2D(currentX, currentY);
                    customersToParse = Integer.parseInt(line.split(":")[1].trim());
                    instance.ods2Clients[currentDriver] = new HashSet<>(customersToParse);
                }

                if (nextLineCustomers) {
                    String[] parts = line.split(" ");

                    // Process data of clientsCoordinates:
                    if (line.startsWith("Customer")) {
                        int c = Integer.parseInt(parts[1].trim());
                        instance.ods2Clients[currentDriver].add(c);
                        instance.setProfit(currentDriver, c, Double.parseDouble(parts[3].trim()));
                        customersToParse--;
                        if (customersToParse == 0) {
                            nextLineCustomers = false;
                        }
                    } else {
                        // List of clientsCoordinates
                        for (String s : parts) {
                            instance.ods2Clients[currentDriver].add(Integer.parseInt(s.trim()));
                        }

                        //assert instance.ods2Clients[currentDriver].size() == customersToParse: String.format("Uhh uh: Promised number of customers does not match loaded data from array: expected %s, loaded %s", customersToParse,  instance.ods2Clients[currentDriver].size());
                        nextLineCustomers = false;
                    }
                }
                if (line.startsWith("Subset of customers") && (customersToParse > 0)) {
                    nextLineCustomers = true;
                }

            } else {
                if (line.startsWith("OCCASIONAL DRIVERS DATA")) {
                    oddata = true;
                }
            }
        }


        instance.inverseOds2Customer();

        assert parsedCapacity : "Missing property CAPACITY in file " + instance.name;
        assert parsedDimension : "Missing property DIMENSION in file " + instance.name;
        assert parsedNumOcassionalDrivers : "Missing property NUMOCASSIONALDRIVERS in file " + instance.name;

        instance.initializeClients(clientsCoordinates);
        instance.computeDistances();

        return instance;
    }
}
