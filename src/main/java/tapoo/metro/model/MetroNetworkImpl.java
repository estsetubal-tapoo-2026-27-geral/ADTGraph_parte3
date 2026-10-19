package tapoo.metro.model;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Domain operations implemented using the Graph ADT. */
public final class MetroNetworkImpl implements MetroNetwork {
    private final Graph<Station, Connection> graph = new GraphEdgeList<>();

    /** Apply the same validation and normalisation in every operation. */
    private String normaliseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidMetroException("Invalid station name.");
        }
        return name.trim().toUpperCase(Locale.ROOT);
    }

    /** Receives an already validated and normalised name. */
    private Vertex<Station> findStation(String name) {
        for (Vertex<Station> vertex : graph.vertices()) {
            if (vertex.element().getName().equals(name)) {
                return vertex;
            }
        }
        return null;
    }

    /** Resolve a station to a vertex or report a domain error. */
    private Vertex<Station> checkStation(Station station) {
        String normalisedName = normaliseName(station == null ? null : station.getName());
        Vertex<Station> vertex = findStation(normalisedName);
        if (vertex == null) {
            throw new InvalidMetroException("Unknown station: " + normalisedName);
        }
        return vertex;
    }

    @Override
    public void addStation(Station station) throws InvalidMetroException {
        String normalisedName = normaliseName(station == null ? null : station.getName());
        if (findStation(normalisedName) != null) {
            throw new InvalidMetroException("Station already exists.");
        }
        graph.insertVertex(new Station(normalisedName));
    }

    @Override
    public void connectStations(Station origin, Station destination, int travelMinutes, LineColour lineColour)
            throws InvalidMetroException {
        Vertex<Station> first = checkStation(origin);
        Vertex<Station> second = checkStation(destination);
        if (first == second) {
            throw new InvalidMetroException("Stations must be different.");
        }
        if (travelMinutes <= 0) {
            throw new InvalidMetroException("Travel time must be positive.");
        }
        if (lineColour == null) {
            throw new InvalidMetroException("Line colour is required.");
        }
        for (Edge<Connection, Station> edge : graph.incidentEdges(first)) {
            if (graph.opposite(first, edge) == second
                    && edge.element().getLineColour() == lineColour) {
                throw new InvalidMetroException("Connection already exists for this line.");
            }
        }
        graph.insertEdge(first, second, new Connection(travelMinutes, lineColour));
    }

    @Override
    public boolean hasDirectConnection(Station origin, Station destination)
            throws InvalidMetroException {
        Vertex<Station> first = checkStation(origin);
        Vertex<Station> second = checkStation(destination);
        return first != second && graph.areAdjacent(first, second);
    }

    @Override
    public Iterable<String> neighbouringStations(Station station) throws InvalidMetroException {
        Vertex<Station> vertex = checkStation(station);
        Set<String> result = new HashSet<>();
        for (Edge<Connection, Station> edge : graph.incidentEdges(vertex)) {
            Vertex<Station> neighbour = graph.opposite(vertex, edge);
            result.add(neighbour.element().getName());
        }
        return new ArrayList<>(result);
    }

    @Override
    public int directTravelMinutes(Station origin, Station destination) {
        Vertex<Station> first = checkStation(origin);
        Vertex<Station> second = checkStation(destination);
        int minimum = Integer.MAX_VALUE;
        boolean found = false;
        for (Edge<Connection, Station> edge : graph.incidentEdges(first)) {
            if (graph.opposite(first, edge) == second) {
                minimum = Math.min(minimum, edge.element().getTravelMinutes());
                found = true;
            }
        }
        if (!found) throw new InvalidMetroException("No direct connection.");
        return minimum;
    }

    /** Read-only use by the prepared view; mutations belong in this class. */
    public Graph<Station, Connection> graphForView() { return graph; }

    @Override
    public void removeStation(Station station) {
        graph.removeVertex(checkStation(station));
    }

    /** Reject null, foreign and removed edges by reference identity. */
    private void checkConnection(Edge<Connection, Station> edge) {
        if (edge != null) {
            for (Edge<Connection, Station> existing : graph.edges()) {
                if (existing == edge) return;
            }
        }
        throw new InvalidMetroException("Invalid or unknown connection.");
    }

    @Override
    public void removeConnection(Edge<Connection, Station> edge) {
        checkConnection(edge);
        graph.removeEdge(edge);
    }

    @Override
    public int numberOfConnections() { return graph.numEdges(); }

    @Override
    public int numberOfConnectionsByStation(Station station) {
        int degree = 0;
        for (Edge<Connection, Station> edge : graph.incidentEdges(checkStation(station))) degree++;
        return degree;
    }

    @Override
    public int numberOfNeighbours(Station station) {
        Set<Vertex<Station>> neighbours = new HashSet<>();
        Vertex<Station> vertex = checkStation(station);
        for (Edge<Connection, Station> edge : graph.incidentEdges(vertex)) {
            neighbours.add(graph.opposite(vertex, edge));
        }
        return neighbours.size();
    }

    @Override
    public List<String> stationsWithMostConnections() {
        List<String> result = new ArrayList<>();
        int maximum = -1;
        for (Vertex<Station> vertex : graph.vertices()) {
            int degree = numberOfConnectionsByStation(vertex.element());
            if (degree > maximum) {
                maximum = degree;
                result.clear();
            }
            if (degree == maximum) result.add(vertex.element().getName());
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public List<String> isolatedStations() {
        List<String> result = new ArrayList<>();
        for (Vertex<Station> vertex : graph.vertices()) {
            if (numberOfConnectionsByStation(vertex.element()) == 0) {
                result.add(vertex.element().getName());
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public boolean hasNoIsolatedStations() { return isolatedStations().isEmpty(); }

    @Override
    public int numberOfStations() { return graph.numVertices(); }

    @Override
    public List<Station> breadthFirstTraversal(Station start) {
        // TODO A3.3: resolve start with checkStation and implement BFS using Queue.
        throw new UnsupportedOperationException("TODO A3.3: breadthFirstTraversal");
    }

    @Override
    public List<Station> depthFirstTraversal(Station start) {
        // TODO A3.3: resolve start with checkStation and implement iterative DFS using Stack.
        throw new UnsupportedOperationException("TODO A3.3: depthFirstTraversal");
    }

    @Override
    public boolean hasPath(Station origin, Station destination) {
        // TODO A3.3 complementary: validate both stations and reuse a traversal.
        throw new UnsupportedOperationException("TODO A3.3: hasPath");
    }

    @Override
    public List<Edge<Connection, Station>> findPath(Station origin, Station destination) {
        // TODO A3.3 complementary: BFS with a local discovery-edge map, then reconstruct.
        throw new UnsupportedOperationException("TODO A3.3: findPath");
    }

    @Override
    public int totalTravelMinutes(List<Edge<Connection, Station>> path) {
        // TODO A3.3 complementary: validate the list, use checkConnection, and sum minutes.
        throw new UnsupportedOperationException("TODO A3.3: totalTravelMinutes");
    }
}
