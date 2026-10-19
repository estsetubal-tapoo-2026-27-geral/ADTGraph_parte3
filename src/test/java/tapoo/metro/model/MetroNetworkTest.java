package tapoo.metro.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the A3.3 operations of MetroNetworkImpl. */
class MetroNetworkTest {
    private MetroNetwork network;

    @BeforeEach
    void setUp() {
        network = new MetroNetworkImpl();
        // TODO A3.3: build the small network from README section 5 (stations A-E and their connections).
    }

    // TODO A3.3: add tests for breadthFirstTraversal and depthFirstTraversal (section 5).
    // TODO A3.3 complementary: add tests for hasPath, findPath and totalTravelMinutes (section 7.4).
}
