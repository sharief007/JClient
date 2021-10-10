package app.openjfx.jclient.utilities;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Lazy
public class TabUtility {
    private final ApplicationContext context;
    private final Resource pulsarclient;

    public TabUtility(ApplicationContext context,
                      @Value("${spring.application.pulsarclient.fxml}") Resource pulsarclient) {
        this.context = context;
        this.pulsarclient = pulsarclient;
    }

    public Tab createNewPulsarClientTab(String title) {
        Tab tab = null;
        try {
            tab = new Tab(title);
            tab.setClosable(true);
            var loader = new FXMLLoader(pulsarclient.getURL());
            loader.setControllerFactory(context::getBean);
            VBox root = loader.load();
            tab.setContent(root);
        } catch (IOException e) {
            tab = null;
            ErrorUtility.showDetailedError(e.getLocalizedMessage(),e);
        }
        return tab;
    }
}
