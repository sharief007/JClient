package app.openjfx.jclient.services;

import org.apache.pulsar.client.api.Authentication;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.auth.oauth2.AuthenticationFactoryOAuth2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@Lazy
public class PulsarApi {

    public PulsarClient newPulsarClient(String url) throws PulsarClientException {
        return PulsarClient.builder().serviceUrl(url).build();
    }

    public PulsarClient newOAuthPulsarClient(String url, URL issuerUrl, URL credentialsURL, String audience) throws PulsarClientException {
        return PulsarClient.builder().serviceUrl(url)
                .authentication(AuthenticationFactoryOAuth2.clientCredentials(issuerUrl,credentialsURL,audience))
                .build();
    }

    public PulsarClient newJWTPulsarClient(String url,String jwt) throws PulsarClientException {
        return PulsarClient.builder().serviceUrl(url)
                .authentication(AuthenticationFactory.token(jwt))
                .build();
    }
}
