package tapoo.metro.view;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import tapoo.metro.model.Connection;
import tapoo.metro.model.InvalidMetroException;
import tapoo.metro.model.LineColour;
import tapoo.metro.model.MetroNetworkImpl;
import tapoo.metro.model.Station;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Prepared view. Domain validation and student algorithms belong in MetroNetworkImpl.
 */
public final class MetroView extends BorderPane {
    private final MetroNetworkImpl network;
    private final SmartGraphPanel<Station, Connection> graphView;
    private final ComboBox<Station> stationToRemove = new ComboBox<>();
    private final ComboBox<Station> origin = new ComboBox<>();
    private final ComboBox<Station> destination = new ComboBox<>();
    private final ComboBox<Edge<Connection, Station>> connectionToRemove = new ComboBox<>();
    private final ComboBox<LineColour> lineColour = new ComboBox<>();
    private final Label labelNumberStations = new Label("##");
    private final Label labelNumberConnections = new Label("##");
    private final Label labelBusiestStations = new Label("##");
    private final Label labelMaximumDegree = new Label("##");
    private final Label labelIsolatedStations = new Label("##");
    private final Label labelHasNoIsolatedStations = new Label("##");

    public MetroView(MetroNetworkImpl network) {
        this.network = network;
        graphView = new SmartGraphPanel<>(network.graphForView(), new SmartCircularSortedPlacementStrategy());
        setStyle("-fx-background-color: #FFF;");

        setTop(buildConfigurationPanel());
        setCenter(buildGraphPanel());
        setBottom(buildStatisticsPanel());
    }

    private VBox buildConfigurationPanel() {
        TextField name = new TextField();
        name.setPromptText("Station name");
        TextField minutes = new TextField();
        minutes.setPromptText("Minutes");
        lineColour.getItems().setAll(LineColour.values());
        lineColour.setPromptText("Line colour");
        Button addStation = new Button("Add station");
        Button removeStation = new Button("Remove station");
        Button connect = new Button("Connect");
        Button removeConnection = new Button("Remove connection");
        connectionToRemove.setConverter(new StringConverter<>() {
            @Override
            public String toString(Edge<Connection, Station> edge) {
                if (edge == null) return "";
                Vertex<Station>[] endpoints = edge.vertices();
                return endpoints[0].element() + " — " + endpoints[1].element() + " : " + edge.element();
            }

            @Override
            public Edge<Connection, Station> fromString(String value) {
                return null;
            }
        });
        // Top area - user interaction
        VBox top = new VBox(30);
        top.setPadding(new Insets(30));

        HBox firstRow = new HBox(10);
        HBox secondRow = new HBox(10);
        HBox thirdRow = new HBox(10);

        firstRow.getChildren().addAll(new Label("Add Station: "), name, addStation,
                new Separator(Orientation.VERTICAL),
                new Label("Remove Station: "), stationToRemove, removeStation);

        secondRow.getChildren().addAll(new Label("Connect From: "),
                origin, new Label("To:"), destination, minutes, new Label("Line:"), lineColour, connect);

        thirdRow.getChildren().addAll(new Label("Remove Connection: "),
                connectionToRemove, removeConnection);

        top.getChildren().addAll(firstRow, secondRow, thirdRow);
        setEvents(name, minutes, addStation, removeStation, connect, removeConnection);
        return top;
    }

    private SmartGraphPanel<Station, Connection> buildGraphPanel() {
        return graphView;
    }

    private FlowPane buildStatisticsPanel() {
        // Bottom area - statistics
        FlowPane bottom = new FlowPane(10, 10);
        bottom.setPadding(new Insets(30));

        bottom.getChildren().addAll(new Label("Station count: "), labelNumberStations,
                new Label("Connection count: "), labelNumberConnections,
                new Label("Busiest Stations: "), labelBusiestStations,
                new Label("Maximum Number of connections: "), labelMaximumDegree,
                new Label("Isolated Stations: "), labelIsolatedStations,
                new Label("No Isolated Stations: "), labelHasNoIsolatedStations);

        return bottom;
    }

    private void setEvents(TextField name, TextField minutes, Button addStation, Button removeStation,
                           Button connect, Button removeConnection) {
        // Events delegate domain validation and mutations to the model.
        addStation.setOnAction(event -> perform(() -> network.addStation(new Station(name.getText()))));
        removeStation.setOnAction(event -> perform(() -> network.removeStation(stationToRemove.getValue())));
        connect.setOnAction(event -> perform(() -> {
            int time;
            try {
                time = Integer.parseInt(minutes.getText().trim());
            } catch (NumberFormatException exception) {
                throw new InvalidMetroException("Minutes must be an integer.");
            }
            network.connectStations(origin.getValue(), destination.getValue(), time, lineColour.getValue());
        }));
        removeConnection.setOnAction(event -> perform(() -> network.removeConnection(connectionToRemove.getValue())));
    }

    public void init() {
        graphView.init();
        applyLineColours();
        updateControls();
        updateStatistics();
    }

    /** Execute the operation and refresh the graph, selections and statistics. */
    private void perform(Runnable operation) {
        try {
            // Execute the requested operation.
            operation.run();
        } catch (InvalidMetroException | UnsupportedOperationException exception) {
            // Report domain validation errors or pending student exercises.
            Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
            alert.setHeaderText("Operation could not be completed");
            alert.showAndWait();
        }
        // Refresh the view after both successful and rejected operations.
        graphView.updateAndWait();
        applyLineColours();
        updateControls();
        updateStatistics();
    }

    private void updateControls() {
        List<Station> stations = new ArrayList<>();
        for (Vertex<Station> vertex : network.graphForView().vertices())
            stations.add(vertex.element());
        Collections.sort(stations, (first, second) -> first.getName().compareTo(second.getName()));
        stationToRemove.getItems().setAll(stations);
        origin.getItems().setAll(stations);
        destination.getItems().setAll(stations);
        connectionToRemove.getItems().setAll(network.graphForView().edges());
    }

    /** Refresh the statistics computed by the Part 2 domain methods. */
    private void updateStatistics() {
        labelNumberStations.setText(String.valueOf(network.numberOfStations()));
        labelNumberConnections.setText(String.valueOf(network.numberOfConnections()));
        List<String> busiest = network.stationsWithMostConnections();
        List<String> descriptions = new ArrayList<>();
        for (String name : busiest) {
            descriptions.add(name + " (" + network.numberOfNeighbours(new Station(name)) + " neighbours)");
        }
        labelBusiestStations.setText(descriptions.toString());
        labelMaximumDegree.setText(busiest.isEmpty() ? "N/A"
                : String.valueOf(network.numberOfConnectionsByStation(new Station(busiest.get(0)))));
        labelIsolatedStations.setText(network.isolatedStations().toString());
        labelHasNoIsolatedStations.setText(String.valueOf(network.hasNoIsolatedStations()));
    }

    /** Keep both line text and stroke colour available to identify connections. */
    private void applyLineColours() {
        for (Edge<Connection, Station> edge : network.graphForView().edges()) {
            String colour = switch (edge.element().getLineColour()) {
                case BLUE -> "#1565C0";
                case YELLOW -> "#F9A825";
                case GREEN -> "#2E7D32";
                case RED -> "#C62828";
            };
            graphView.getStylableEdge(edge).setStyleInline(
                    "-fx-stroke: " + colour + "; -fx-stroke-width: 3;");
        }
    }

}
