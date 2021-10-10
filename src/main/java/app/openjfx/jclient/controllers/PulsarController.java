package app.openjfx.jclient.controllers;

import app.openjfx.jclient.listeners.PulsarMessageHandler;
import app.openjfx.jclient.model.PulsarBatchFactory;
import app.openjfx.jclient.model.PulsarOAuth;
import app.openjfx.jclient.model.PulsarProperty;
import app.openjfx.jclient.services.PulsarApi;
import app.openjfx.jclient.utilities.ErrorUtility;
import app.openjfx.jclient.utilities.PulsarUtility;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Lazy
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PulsarController {

    private PulsarClient client;
    private Producer<byte[]> producer;
    private Consumer<byte[]> consumer;
    private final String[] auths = new String[]{"No Auth","JWT","OAuth 2.0"};
    private final String[] settings = new String[]{"Default","Batch"};
    private final PulsarApi pulsarApi;
    private final PulsarMessageHandler messageHandler;
    private final PulsarUtility pulsarUtility;
    private final Logger log = LoggerFactory.getLogger(getClass());
    public PulsarController(PulsarApi pulsarApi, PulsarMessageHandler messageHandler, PulsarUtility pulsarUtility) {
        this.pulsarApi = pulsarApi;
        this.messageHandler = messageHandler;
        this.pulsarUtility = pulsarUtility;
    }

    @FXML public TextField SERVICE_URL;
    @FXML public TextField TOPIC_NAME;
    @FXML public TextField TOPIC_NAME_2;
    @FXML public TextField SUBSCRIPTION;
    @FXML public TextArea MESSAGE_BODY;
    @FXML public VBox MESSAGEBOX;
    @FXML public ScrollPane SCROLLPANE;
    @FXML public CheckBox ACK;
    @FXML public Button CONNECT;
    @FXML public HBox HEADER;
    @FXML public CheckBox CLEAR_MSG;
    @FXML public HBox PRODUCER;
    @FXML public HBox SUBSCRIBER;
    @FXML public Button PRODUCERBTN;
    @FXML public Button SUBSCRIBERBTN;
    @FXML public ComboBox<String> AUTH;
    @FXML public TableView<PulsarProperty> MESSAGE_PROPERTIES;
    @FXML public TableColumn<PulsarProperty,String> KEY_COLUMN;
    @FXML public TableColumn<PulsarProperty,String> VALUE_COLUMN;
    @FXML public TextField KEY;
    @FXML public TextField VALUE;
    @FXML public Button ADD_HEADER;
    @FXML public ComboBox<String> PSETTINGS;
//    @FXML public ComboBox<String> SSETTINGS;


    private ObservableList<PulsarProperty> properties = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        log.info("PulsarController created");
        this.MESSAGEBOX.heightProperty().addListener((observable)-> this.SCROLLPANE.setVvalue(1d));
        this.SERVICE_URL.textProperty().addListener(listener);
        this.AUTH.setItems(FXCollections.observableList(Arrays.asList(auths)));
        this.PSETTINGS.setItems(FXCollections.observableList(Arrays.asList(settings)));
//        this.SSETTINGS.setItems(FXCollections.observableList(Arrays.asList(settings)));
        this.AUTH.getSelectionModel().selectFirst();
        this.PSETTINGS.getSelectionModel().selectFirst();
//        this.SSETTINGS.getSelectionModel().selectFirst();
        initTable();
    }

    @FXML
    public void connect() {
        try {
            if (Objects.nonNull(this.client) && !this.client.isClosed()) {
                this.client.closeAsync().thenRun(()-> Platform.runLater(()-> {
                    this.CONNECT.setText("Connect");
                    this.PRODUCERBTN.setText("Producer");
                    this.SUBSCRIBERBTN.setText("Subscribe");
                    this.HEADER.setDisable(false);
                    this.PRODUCER.setDisable(false);
                    this.SUBSCRIBER.setDisable(false);
                })).exceptionally(ex ->{
                   ErrorUtility.showDetailedError(ex.getLocalizedMessage(), (Exception) ex);
                   return null;
                });
            } else {
                String servieUrl = this.SERVICE_URL.getText();
                if (!(servieUrl.isEmpty() || servieUrl.isBlank())) {
                    if (AUTH.getValue().equals(auths[1])) {
                        Optional<String> jwt = pulsarUtility.showJWTdialog();
                        jwt.ifPresent(token ->{
                            try {
                                this.client = this.pulsarApi.newJWTPulsarClient(servieUrl,token);
                            } catch (PulsarClientException e) {
                                ErrorUtility.showDetailedError(e.getLocalizedMessage(),e);
                            }
                        });
                    } else if(AUTH.getValue().equals(auths[2])) {
                        Optional<PulsarOAuth> details = pulsarUtility.showOAuthDailog();
                        details.ifPresent(outh->{
                            try {
                                this.client = this.pulsarApi.newOAuthPulsarClient(servieUrl, outh.getIssuerURL(), outh.getCredentials(), outh.getAudience());
                            } catch (PulsarClientException e) {
                                ErrorUtility.showDetailedError(e.getLocalizedMessage(),e);
                            }
                        });
                    } else {
                        this.client = this.pulsarApi.newPulsarClient(SERVICE_URL.getText());
                    }
                    if (Objects.nonNull(client) && !client.isClosed()) {
                        Platform.runLater(()->{
                            this.CONNECT.setText("Disconnect");
                            this.HEADER.setDisable(true);
                        });
                    }
                }
            }
        } catch (PulsarClientException e) {
            ErrorUtility.showDetailedError(e.getLocalizedMessage(),e);
        }
    }

    @FXML
    public void createProducer()  {
        if (Objects.nonNull(this.client)) {
            if (Objects.nonNull(producer) && producer.isConnected()) {
                producer.closeAsync().thenRun(()-> Platform.runLater(()->{
                    PRODUCER.setDisable(false);
                    PRODUCERBTN.setText("Create Producer");
                })).exceptionally(e -> {
                    ErrorUtility.showDetailedError(e.getLocalizedMessage(),(Exception) e);
                    return null;
                });
            } else {
                if (!TOPIC_NAME.getText().isEmpty() && !TOPIC_NAME.getText().isBlank()){
                    if (PSETTINGS.getValue().equals(settings[1])) {
                        Optional<PulsarBatchFactory> factory = pulsarUtility.showProducerBatchOptions();
                        factory.ifPresent(options -> {
                                client.newProducer().topic(TOPIC_NAME.getText())
                                        .batchingMaxPublishDelay(options.getDelayMS(), TimeUnit.MILLISECONDS)
                                        .sendTimeout(options.getMessageTimeOut(), TimeUnit.MILLISECONDS)
                                        .maxPendingMessages(options.getQueueSize())
                                        .blockIfQueueFull(options.isBlockQueueIfFull())
                                        .createAsync()
                                    .thenApply(producer->{
                                        this.producer = producer;
                                        Platform.runLater(()->{
                                            PRODUCER.setDisable(true);
                                            PRODUCERBTN.setText("Close Producer");
                                        });
                                        return null;
                                    }).exceptionally(e -> {
                                ErrorUtility.showDetailedError(e.getLocalizedMessage(),(Exception) e);
                                return null;
                            });
                        });
                    } else {
                        this.client.newProducer()
                                .topic(TOPIC_NAME.getText()).createAsync()
                                .thenApply(producer->{
                                    this.producer = producer;
                                    Platform.runLater(()->{
                                        PRODUCER.setDisable(true);
                                        PRODUCERBTN.setText("Close Producer");
                                    });
                                    return null;
                                }).exceptionally(e -> {
                            ErrorUtility.showDetailedError(e.getLocalizedMessage(),(Exception) e);
                            return null;
                        });
                    }
                }
            }
        }
    }

    @FXML
    public void send(){
        var message = MESSAGE_BODY.getText();
        if (Objects.nonNull(this.producer) && !(message.isEmpty() || message.isBlank())) {
            if (this.producer.isConnected()) {
                    Map<String,String> props = new HashMap<>();
                    if (!properties.isEmpty()) {
                        properties.forEach(pulsarProperty -> {
                            props.put(pulsarProperty.getKey(),pulsarProperty.getValue());
                        });
                    }
                    this.producer.newMessage().value(message.getBytes(StandardCharsets.UTF_8))
                            .properties(props)
                        .sendAsync().thenRun(()->{
                            if (this.CLEAR_MSG.isSelected()){
                                Platform.runLater(()-> this.MESSAGE_BODY.setText(""));
                            }
                        })
                        .exceptionally(e -> {
                            ErrorUtility.showDetailedError(e.getLocalizedMessage(),(Exception) e);
                            return null;
                        });
            }
        }
    }

    @FXML
    public void subscribe(){
        if (Objects.nonNull(this.client)) {
            if (Objects.nonNull(consumer) && consumer.isConnected()){
                consumer.closeAsync()
                        .thenRun(()-> Platform.runLater(()->{
                            SUBSCRIBERBTN.setText("Subscribe");
                            SUBSCRIBER.setDisable(false);
                        })).exceptionally(e -> {
                            ErrorUtility.showDetailedError(e.getLocalizedMessage(),(Exception) e);
                            return null;
                        });
            } else {
                var topicName = TOPIC_NAME_2.getText();
                var subscriptionName = SUBSCRIPTION.getText();
                if (!(topicName.isEmpty() || topicName.isBlank()) && !(subscriptionName.isBlank() || subscriptionName.isEmpty())) {
                    this.messageHandler.setMessageBox(this.MESSAGEBOX);
                    this.messageHandler.setAutoAck(this.ACK.isSelected());
                    this.client.newConsumer()
                            .topic(topicName)
                            .subscriptionName(subscriptionName)
                            .messageListener(messageHandler)
                            .subscribeAsync().thenApply(consumer -> {
                                this.consumer = consumer;
                                Platform.runLater(()->{
                                    SUBSCRIBER.setDisable(true);
                                    SUBSCRIBERBTN.setText("Unsubscribe");
                                });
                                return null;
                            }).exceptionally(e -> {
                                ErrorUtility.showDetailedError(e.getLocalizedMessage(),(Exception) e);
                                return null;
                            });
                }
            }
        }
    }

    @FXML
    public void clearMessages(){
        Platform.runLater(()-> this.MESSAGEBOX.getChildren().clear());
    }

    private ChangeListener<String> listener = (observableValue, oldv, newv) -> this.CONNECT.setDisable(newv.isEmpty() || newv.isBlank());

    public void initTable() {
        KEY.textProperty().addListener((observable,old,newv)->{
            String value = VALUE.getText();
            ADD_HEADER.setDisable(!(!(newv.isEmpty() || newv.isBlank()) && !(value.isBlank() || value.isEmpty())));
        });
        VALUE.textProperty().addListener((observable,old,newv)->{
            String value = KEY.getText();
            ADD_HEADER.setDisable(!(!(newv.isEmpty() || newv.isBlank()) && !(value.isBlank() || value.isEmpty())));
        });
        KEY_COLUMN.setCellValueFactory(new PropertyValueFactory<>("Key"));
        VALUE_COLUMN.setCellValueFactory(new PropertyValueFactory<>("Value"));
        MESSAGE_PROPERTIES.setItems(properties);
    }

    @FXML
    public void addHeader(){
        String key = KEY.getText(), value = VALUE.getText();
        if (!(key.isEmpty() || key.isBlank())&&!(value.isBlank() ||value.isBlank())) {
            PulsarProperty property = new PulsarProperty(key,value);
            properties.add(property);
            Platform.runLater(()->{
                KEY.setText("");
                VALUE.setText("");
            });
        }
    }

    @FXML
    public void removeHeader() {
        PulsarProperty property  =MESSAGE_PROPERTIES.getSelectionModel().getSelectedItem();
        if (Objects.nonNull(property)) {
            MESSAGE_PROPERTIES.getItems().remove(property);
        }
    }
}
