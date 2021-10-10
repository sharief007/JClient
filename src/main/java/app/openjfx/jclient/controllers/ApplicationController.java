package app.openjfx.jclient.controllers;

import app.openjfx.jclient.utilities.TabUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Component
@Lazy
public class ApplicationController {
    @FXML
    private TabPane APPLICATION_TABS;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TabUtility tabUtility;

    public ApplicationController(TabUtility tabUtility) {
        this.tabUtility = tabUtility;
    }

    @PostConstruct
    public void log(){
        log.info("Application Controller created");
    }

    @FXML
    public void initialize() {
        Tab tab = tabUtility.createNewPulsarClientTab("PulsarClient");
        if (Objects.nonNull(tab)) {
            APPLICATION_TABS.getTabs().add(tab);
            APPLICATION_TABS.getSelectionModel().select(tab);
        }
    }

    @FXML
    void createAndOpenPulsarClient() {
        Tab tab = tabUtility.createNewPulsarClientTab("PulsarClient");
        if (Objects.nonNull(tab)) {
            APPLICATION_TABS.getTabs().add(tab);
            APPLICATION_TABS.getSelectionModel().select(tab);
        }
    }

    @FXML
    void createAndOpenWebSocketClient() {
        //do nothing for now
    }

    @FXML
    void closeTab(){
        if (APPLICATION_TABS.getTabs().size()!=0){
            int index = APPLICATION_TABS.getSelectionModel().getSelectedIndex();
            APPLICATION_TABS.getTabs().remove(index);
        }
    }

    @FXML
    void closeAllTabs(){
        APPLICATION_TABS.getTabs().removeAll(APPLICATION_TABS.getTabs());
    }

    @FXML
    void quit() {
        var window = this.APPLICATION_TABS.getScene().getWindow();
        window.fireEvent(new WindowEvent(window,WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
