package app.openjfx.jclient.listeners;

import app.openjfx.jclient.events.StageReadyEvent;
import app.openjfx.jclient.utilities.StageUtility;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Autowired
    private StageUtility stageUtility;

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = (Stage) event.getSource();
        stageUtility.loadMainFxml(stage);
        stageUtility.setTitleAndIcon(stage);
        stageUtility.setCloseRequestHandler(stage);
        stage.show();
    }

}
