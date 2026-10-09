package tapoo.metro.model;

import com.brunomnsilva.smartgraph.graph.Edge;

import java.util.List;

/**
 * An undirected network without loops; parallel connections require different line colours.
 * Names are trimmed and converted to upper case for storage and lookup.
 * Invalid names, unknown stations and domain violations cause InvalidMetroException.
 */
public interface MetroNetwork {
    /**
     * Registers a new station in the network.
     *
     * @param station station to register
     * @throws InvalidMetroException if the station is null, has a blank name or is already registered
     */
    void addStation(Station station) throws InvalidMetroException;

    /**
     * Creates a direct connection between two existing, distinct stations.
     *
     * @param origin origin station
     * @param destination destination station
     * @param lineColour required line colour
     * @param travelMinutes positive estimated travel time in minutes
     * @throws InvalidMetroException if a station is invalid or unknown, both stations identify the same station,
     *                               the travel time is not positive, the colour is null, or a connection for the same pair and colour already exists
     */
    void connectStations(Station origin, Station destination, int travelMinutes, LineColour lineColour)
            throws InvalidMetroException;

    /**
     * Checks whether two existing stations have a direct connection.
     *
     * @param origin origin station
     * @param destination destination station
     * @return true if the stations are directly connected; false otherwise, including when a station is queried
     *         against itself
     * @throws InvalidMetroException if a station is invalid or unknown
     */
    boolean hasDirectConnection(Station origin, Station destination)
            throws InvalidMetroException;

    /**
     * Returns the names of all stations directly connected to the given station.
     *
     * @param station station whose neighbours are requested
     * @return iterable with neighbouring station names in unspecified order, or an empty iterable for an isolated station
     * @throws InvalidMetroException if the station is invalid or unknown
     */
    Iterable<String> neighbouringStations(Station station) throws InvalidMetroException;

    int numberOfStations();

    /**
     * Return the minimum travel time among direct connections; reject missing direct connections.
     */
    int directTravelMinutes(Station origin, Station destination);

    /**
     * Remove a station and all its incident edges; reject invalid/unknown names.
     */
    void removeStation(Station station);

    /**
     * Remove exactly one existing edge; reject null, foreign or removed edges.
     */
    void removeConnection(Edge<Connection, Station> edge);

    int numberOfConnections();

    /**
     * Number of incident edges, including parallel edges after the extension.
     */
    int numberOfConnectionsByStation(Station station);

    /** Number of distinct neighbouring stations; reject invalid or unknown stations. */
    int numberOfNeighbours(Station station);

    /**
     * All stations tied for maximum degree, sorted; empty graph -> empty list.
     */
    List<String> stationsWithMostConnections();

    /**
     * Names sorted alphabetically; empty graph -> empty list.
     */
    List<String> isolatedStations();

    /**
     * True when there are no isolated stations, including an empty graph.
     */
    boolean hasNoIsolatedStations();

    /**
     * Returns each reachable station once in BFS processing order, starting with start.
     * @param start existing station, resolved by normalised name
     * @return traversal order, without changing the network
     * @throws InvalidMetroException if start is null, invalid or unknown
     */
    List<Station> breadthFirstTraversal(Station start);

    /**
     * Returns each reachable station once in iterative DFS processing order.
     * @param start existing station, resolved by normalised name
     * @return traversal order, starting with start, without changing the network
     * @throws InvalidMetroException if start is null, invalid or unknown
     */
    List<Station> depthFirstTraversal(Station start);

    /**
     * @param origin existing origin station
     * @param destination existing destination station
     * @return whether a path exists, including a zero-edge path to the same station
     * @throws InvalidMetroException if either station is invalid or unknown
     */
    boolean hasPath(Station origin, Station destination);

    /**
     * @param origin existing origin station
     * @param destination existing destination station
     * @return a minimum-edge path in origin-to-destination order; empty if unreachable or identical
     * @throws InvalidMetroException if either station is invalid or unknown
     */
    List<Edge<Connection, Station>> findPath(Station origin, Station destination);

    /**
     * @param path ordered valid edges belonging to this network
     * @return sum of connection times, or zero for an empty list
     * @throws InvalidMetroException if the list is null or contains null, foreign or removed edges
     */
    int totalTravelMinutes(List<Edge<Connection, Station>> path);
}
