package tapoo.metro.model;

/** Fictitious teaching network; not an actual metro timetable or topology. */
public final class MetroSample {
    private MetroSample() { }
    public static MetroNetworkImpl create() {
        MetroNetworkImpl network = new MetroNetworkImpl();
        for (String name : new String[]{"Alameda", "Arroios", "Saldanha", "Campo Grande", "Aeroporto"}) {
            network.addStation(new Station(name));
        }
        network.connectStations(new Station("Alameda"), new Station("Arroios"), 2, LineColour.GREEN);
        network.connectStations(new Station("Alameda"), new Station("Saldanha"), 3, LineColour.RED);
        network.connectStations(new Station("Arroios"), new Station("Campo Grande"), 3, LineColour.GREEN);
        network.connectStations(new Station("Saldanha"), new Station("Campo Grande"), 2, LineColour.YELLOW);
        return network;
    }
}
