package app.openjfx.jclient;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JclientApplication {

    public static void main(String[] args) {
        Application.launch(JfxApplication.class,args);
    }

}
