package app.openjfx.jclient;

import app.openjfx.jclient.events.StageReadyEvent;
import app.openjfx.jclient.listeners.StageInitializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JfxApplication extends Application {

    private ConfigurableApplicationContext context;
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    StageInitializer stageInitializer;

    @Override
    public void init() {
        this.context = new SpringApplicationBuilder(JclientApplication.class).run();
        log.info("Application Context Initialized");
    }

    @Override
    public void start(Stage stage) {
        this.context.publishEvent(new StageReadyEvent(stage));
        log.info("Starting application stage");
    }

    @Override
    public void stop() throws Exception {
        log.info("Closing application context");
        this.context.close();
        log.info("Context closed");
        Platform.exit();
        System.exit(0);
    }
}
