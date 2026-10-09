package tapoo.metro.model;

/** A distinct connection with an estimated travel time and line colour. */
public class Connection {
    private final int travelMinutes;
    private final LineColour lineColour;

    /**
     * Creates a connection for a specific metro line.
     * @param travelMinutes positive estimated travel time in minutes
     * @param lineColour required line identifier
     * @throws InvalidMetroException if minutes are not positive or the colour is null
     */
    public Connection(int travelMinutes, LineColour lineColour) {
        if (travelMinutes <= 0 || lineColour == null) {
            throw new InvalidMetroException("Positive minutes and a line colour are required.");
        }
        this.travelMinutes = travelMinutes;
        this.lineColour = lineColour;
    }

    public int getTravelMinutes() { return travelMinutes; }

    public LineColour getLineColour() { return lineColour; }

    @Override
    public String toString() {
        return travelMinutes + " min [" + lineColour + "]";
    }
}
