package tapoo.metro;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tapoo.metro.model.MetroSample;
import tapoo.metro.view.MetroView;

/** Prepared JavaFX entry point. */
public final class MetroApplication extends Application {
    @Override public void start(Stage stage) {
        MetroView view = new MetroView(MetroSample.create());
        stage.setTitle("TAPOO — Metro network");
        stage.setScene(new Scene(view, 1150, 780));
        stage.show();
        view.init();
    }
    public static void main(String[] args) { launch(args); }
}
