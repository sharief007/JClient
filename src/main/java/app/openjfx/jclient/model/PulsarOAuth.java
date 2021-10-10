package app.openjfx.jclient.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class PulsarOAuth {
    private URL IssuerURL, Credentials;
    private String audience;

    public PulsarOAuth(String issuerURL, File credentials, String audience) throws MalformedURLException {
        IssuerURL = new URL(issuerURL);
        Credentials = credentials.toURL();
        this.audience = audience;
    }

    public URL getIssuerURL() {
        return IssuerURL;
    }

    public void setIssuerURL(URL issuerURL) {
        IssuerURL = issuerURL;
    }

    public URL getCredentials() {
        return Credentials;
    }

    public void setCredentials(URL credentials) {
        Credentials = credentials;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
