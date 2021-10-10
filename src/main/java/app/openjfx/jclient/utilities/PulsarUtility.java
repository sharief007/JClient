package app.openjfx.jclient.utilities;

import app.openjfx.jclient.model.PulsarBatchFactory;
import app.openjfx.jclient.model.PulsarOAuth;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Lazy
public class PulsarUtility {
    public Optional<PulsarOAuth> showOAuthDailog() {

        //create Content
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setPrefSize(300.0, 200.0);
        vBox.setSpacing(15.0);

        //Issuer URL
        Label keyLabel = new Label("Issuer URL");
        keyLabel.setPadding(new Insets(0, 5.0, 0, 5.0));
        TextField key = new TextField();
        key.setPromptText("Issuer URL");
        HBox keybox = new HBox(keyLabel, key);
        keybox.setSpacing(15.0);
        keybox.setAlignment(Pos.CENTER);

        //Audience
        Label audience = new Label("Audience");
        audience.setPadding(new Insets(0, 5.0, 0, 5.0));
        TextField audienceValue = new TextField();
        audienceValue.setPromptText("Audience");
        HBox hBox = new HBox(audience, audienceValue);
        hBox.setSpacing(15.0);
        hBox.setAlignment(Pos.CENTER);

        //Credentials File
        Label valueLabel = new Label("Credentials");
        Button value = new Button("Choose File");
        HBox valuebox = new HBox(valueLabel, value);
        valuebox.setSpacing(15.0);
        value.setPrefWidth(150.0);
        valuebox.setAlignment(Pos.CENTER);


        AtomicReference<File> file = new AtomicReference<>();
        SimpleBooleanProperty disable = new SimpleBooleanProperty(true);
        value.setOnAction(e -> {
            var filechooser = new FileChooser();
            file.set(filechooser.showOpenDialog(null));
            value.setText(file.get().getName());
            if (!key.getText().isEmpty() && !audienceValue.getText().isEmpty()) {
                disable.set(false);
            }
        });


        vBox.getChildren().addAll(keybox, hBox, valuebox);

        //set Dailog
        Dialog<PulsarOAuth> dailog = new Dialog<>();
        dailog.setTitle("Provide OAuth 2.0 Details");
        dailog.setResizable(false);
        dailog.getDialogPane().setContent(vBox);


        //add Buttons
        ButtonType ok = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);
        dailog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ok);


        //apply button
        Node apply = dailog.getDialogPane().lookupButton(ok);

        //set disabled initially
        apply.setDisable(true);


        key.textProperty().addListener(e -> disable.set(key.getText().isEmpty() || file.get() == null || audienceValue.getText().isEmpty()));
        audienceValue.textProperty().addListener(e -> disable.set(key.getText().isEmpty() || file.get() == null || audienceValue.getText().isEmpty()));
        apply.disableProperty().bindBidirectional(disable);


        //result
        dailog.setResultConverter(buttonType -> {
            if (buttonType.equals(ok)) {
                try {
                    return new PulsarOAuth(key.getText(), file.get(), audienceValue.getText());
                } catch (MalformedURLException e) {
                    ErrorUtility.showDetailedError(e.getLocalizedMessage(), e);
                }
            }
            return null;
        });

        Platform.runLater(key::requestFocus);

        return dailog.showAndWait();
    }

    public Optional<String> showJWTdialog() {
        TextArea textArea = new TextArea();
        textArea.setPromptText("Paste your JWT Token here...");
        VBox.setVgrow(textArea, Priority.ALWAYS);

        //create Content
        VBox vBox = new VBox(textArea);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPrefSize(300.0, 200.0);

        //set Dailog
        Dialog<String> dailog = new Dialog<>();
        dailog.setTitle("Provide JWT Token");
        dailog.getDialogPane().setContent(vBox);

        //add Buttons
        ButtonType ok = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);
        dailog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ok);

        //apply button
        Node apply = dailog.getDialogPane().lookupButton(ok);

        //set disabled initially
        apply.setDisable(true);

        textArea.textProperty().addListener((observable, old, newv) -> apply.setDisable(newv.isEmpty() || newv.isBlank()));

        //result
        dailog.setResultConverter(buttonType -> {
            if (buttonType.equals(ok)) {
                return textArea.getText();
            }
            return null;
        });
        Platform.runLater(textArea::requestFocus);
        return dailog.showAndWait();
    }

    public Optional<PulsarBatchFactory> showProducerBatchOptions() {
        //create Content
        VBox vBox = new VBox();
        vBox.setSpacing(15.0);

        //SendTimeOut
        Label label1 = new Label("Send TimeOut MS");
        label1.setPadding(new Insets(0, 5.0, 0, 5.0));
        TextField timeout = new TextField("30000");
        timeout.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                timeout.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        HBox hbox1 = new HBox(label1, timeout);
        hbox1.setSpacing(15.0);
        hbox1.setAlignment(Pos.CENTER_LEFT);

        Label label2 = new Label("Max Pending Messages");
        label2.setPadding(new Insets(0, 5.0, 0, 5.0));
        TextField pendingmsgs = new TextField("10");
        pendingmsgs.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                pendingmsgs.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        HBox hbox2 = new HBox(label2, pendingmsgs);
        hbox2.setSpacing(15.0);
        hbox2.setAlignment(Pos.CENTER_LEFT);

        Label label3 = new Label("Max Publish DelayMicros");
        label3.setPadding(new Insets(0, 5.0, 0, 5.0));
        TextField delay = new TextField("10000");
        delay.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                delay.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        HBox hbox3 = new HBox(label3, delay);
        hbox3.setSpacing(15.0);
        hbox3.setAlignment(Pos.CENTER_LEFT);

        Label label4 = new Label("Queue Size");
        label4.setPadding(new Insets(0, 5.0, 0, 5.0));
        TextField queueSize = new TextField("10");
        queueSize.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                queueSize.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        HBox hbox4 = new HBox(label4, queueSize);
        hbox4.setSpacing(15.0);
        hbox4.setAlignment(Pos.CENTER_LEFT);

        CheckBox queueFull = new CheckBox("Block if Queue Full");

        vBox.getChildren().addAll(hbox1,hbox2,hbox3,hbox4,queueFull);

        //set Dailog
        Dialog<PulsarBatchFactory> dailog = new Dialog<>();
        dailog.setTitle("Provide OAuth 2.0 Details");
        dailog.setResizable(false);
        dailog.getDialogPane().setContent(vBox);


        //add Buttons
        ButtonType ok = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);
        dailog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ok);


        dailog.setResultConverter(buttonType -> {
            if (buttonType.equals(ok)) {
                int v1 = timeout.getText().isEmpty() ? 30000 : Integer.parseInt(timeout.getText());
                int v2 = queueSize.getText().isEmpty() ? 10 : Integer.parseInt(queueSize.getText());
                int v3 = pendingmsgs.getText().isEmpty() ? 10 : Integer.parseInt(pendingmsgs.getText());
                int v4 = delay.getText().isEmpty() ? 10000 : Integer.parseInt(delay.getText());
                boolean b = queueFull.isSelected();
                return new PulsarBatchFactory(v1, v2,v3, v4,b);
            }
            return null;
        });

        return dailog.showAndWait();
    }
}
