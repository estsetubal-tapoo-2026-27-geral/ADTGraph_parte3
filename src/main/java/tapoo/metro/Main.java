package tapoo.metro;

import tapoo.metro.model.*;

/** Fictitious network; travel times are illustrative. */
public final class Main {
    public static void main(String[] args) {
        MetroNetwork network = MetroSample.create();
        // Aeroporto is isolated. Distinct edges have repeated travel times.
        System.out.println("Stations: " + network.numberOfStations());
        System.out.println("Alameda neighbours: " + network.neighbouringStations(new Station(" alameda ")));
        System.out.println("Aeroporto neighbours: " + network.neighbouringStations(new Station("AEROPORTO")));

        // Completed part 1: adjacency queries.
        System.out.println("Alameda - Arroios: " + network.hasDirectConnection(new Station("alameda"), new Station("ARROIOS")));
        System.out.println("Arroios - Alameda: " + network.hasDirectConnection(new Station("Arroios"), new Station("Alameda")));
        System.out.println("Alameda - Campo Grande: " + network.hasDirectConnection(new Station("Alameda"), new Station("Campo Grande")));

        try {
            network.addStation(new Station(" alameda "));
        } catch (InvalidMetroException exception) {
            System.out.println("Cannot add station: " + exception.getMessage());
        }
        try {
            network.connectStations(new Station("Alameda"), new Station("Unknown"), 2, LineColour.GREEN);
        } catch (InvalidMetroException exception) {
            System.out.println("Cannot create connection: " + exception.getMessage());
        }

        // Completed part 1: direct travel time.
        System.out.println("Alameda - Arroios (min): " + network.directTravelMinutes(new Station("Alameda"), new Station("Arroios")));
        System.out.println("Arroios - Alameda (min): " + network.directTravelMinutes(new Station("Arroios"), new Station("Alameda")));
        System.out.println("Connections: " + network.numberOfConnections());
        System.out.println("Busiest stations: " + network.stationsWithMostConnections());
        System.out.println("Isolated stations: " + network.isolatedStations());
        network.connectStations(new Station("Alameda"), new Station("Saldanha"), 4, LineColour.BLUE);
        System.out.println("Alameda degree with parallel lines: "
                + network.numberOfConnectionsByStation(new Station("Alameda")));
        System.out.println("Alameda distinct neighbours: "
                + network.numberOfNeighbours(new Station("Alameda")));
        System.out.println("Minimum direct minutes: "
                + network.directTravelMinutes(new Station("Alameda"), new Station("Saldanha")));

        // TODO A3.3: display BFS and DFS from the same station after implementing them.
        // TODO A3.3 complementary: display path existence, ordered edges and total minutes.

    }
}
