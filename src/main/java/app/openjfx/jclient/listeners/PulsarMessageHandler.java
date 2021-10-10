package app.openjfx.jclient.listeners;

import app.openjfx.jclient.utilities.ErrorUtility;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Lazy
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PulsarMessageHandler implements MessageListener<byte[]> {
    private VBox messageBox;
    private boolean autoAck;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy-HH:mm:ss");

    public void setMessageBox(VBox messageBox) {
        this.messageBox = messageBox;
    }
    public void setAutoAck(boolean autoAck){
        this.autoAck = autoAck;
    }

    @Override
    public void received(Consumer<byte[]> consumer, Message<byte[]> msg) {
        byte[] message = msg.getValue();
        Label messageId = new Label("Message ID: "+ msg.getMessageId().toString());
        Pane pane = new Pane();
        FontAwesomeIconView ackView;
        if (autoAck) {
            ackView = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
        } else {
            ackView = new FontAwesomeIconView(FontAwesomeIcon.CHECK);
        }
        FontAwesomeIconView down = new FontAwesomeIconView(FontAwesomeIcon.ARROW_DOWN);
        Label size = new Label(calculateSize(message));
        size.setGraphic(down);
        Label time = new Label(calculateTime(msg.getPublishTime()));
        FontAwesomeIconView copyIcon = new FontAwesomeIconView(FontAwesomeIcon.COPY);
        Button copy = new Button();
        copy.setGraphic(copyIcon);
        HBox header = new HBox(ackView, messageId, pane, size, time, copy);
        header.setSpacing(5);
        header.setAlignment(Pos.CENTER);
        HBox.setHgrow(pane, Priority.ALWAYS);

        Label properties = new Label(msg.getProperties().toString());
        properties.setFont(Font.font("System", FontWeight.NORMAL, FontPosture.ITALIC, 14.0));
        properties.setWrapText(true);
//        properties.setPadding(new Insets(5.0,0,5.0,0));

        Label label = new Label(new String(message, StandardCharsets.UTF_8));
        label.setFont(Font.font(14.0));
        label.setWrapText(true);
        label.setPadding(new Insets(5.0,0,5.0,0));

        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);
        VBox vBox = new VBox( header, properties, label, separator);
        vBox.setPadding(new Insets(0,5.0,0,5.0));

        copy.setOnAction((event)-> Platform.runLater(()-> {
            ClipboardContent content = new ClipboardContent();
            content.putString(label.getText());
            Clipboard.getSystemClipboard().setContent(content);
        }));
        Platform.runLater(()->{
            try {
                this.messageBox.getChildren().add(vBox);
                if (autoAck){
                    consumer.acknowledge(msg);
                }
            } catch (PulsarClientException e) {
                ErrorUtility.showDetailedError(e.getLocalizedMessage(),e);
            }
        });
    }

    private String calculateTime(long eventTime) {
        Date date = new Date(eventTime);
        return dateFormat.format(date);
    }

    private String calculateSize(byte[] message) {
        String size = null;
        if (message.length >= 1000 ) {
            double d = message.length/1000;
            if (d >= 1000) {
                size = d/1000 + " MB";
            } else {
                size = d + " KB";
            }
        } else {
            size = message.length + " Byte";
        }
        return size;
    }
}
