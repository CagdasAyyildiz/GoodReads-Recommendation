import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


public class GROAuth {

    public static InputStream auth() throws IOException {
        OAuthHmacSigner signer = new OAuthHmacSigner();
        // Get Temporary Token
        OAuthGetTemporaryToken getTemporaryToken = new OAuthGetTemporaryToken(Constants.TOKEN_SERVER_URL);
        signer.clientSharedSecret = Environment.GOODREADS_SECRET;
        getTemporaryToken.signer = signer;
        getTemporaryToken.consumerKey = Environment.GOODREADS_KEY;
        getTemporaryToken.transport = new NetHttpTransport();
        OAuthCredentialsResponse temporaryTokenResponse = null;
        try {
            temporaryTokenResponse = getTemporaryToken.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Build Authenticate URL
        OAuthAuthorizeTemporaryTokenUrl accessTempToken = new OAuthAuthorizeTemporaryTokenUrl(Constants.AUTHENTICATE_URL);
        accessTempToken.temporaryToken = Objects.requireNonNull(temporaryTokenResponse).token;
        String authUrl = accessTempToken.build();

        // Redirect to Authenticate URL in order to get Verifier Code
        System.out.println("Goodreads oAuth sample: Please visit the following URL to authorize:");
        System.out.println(authUrl);
        System.out.println("Waiting 5s to allow time for visiting auth URL and authorizing...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Waiting time complete - assuming access granted and attempting to get access token");
        // Get Access Token using Temporary token and Verifier Code
        OAuthGetAccessToken getAccessToken = new OAuthGetAccessToken(Constants.ACCESS_TOKEN_URL);
        getAccessToken.signer = signer;
        // NOTE: This is the main difference from the StackOverflow example
        signer.tokenSharedSecret = temporaryTokenResponse.tokenSecret;
        getAccessToken.temporaryToken = temporaryTokenResponse.token;
        getAccessToken.transport = new NetHttpTransport();
        getAccessToken.consumerKey = Environment.GOODREADS_KEY;
        OAuthCredentialsResponse accessTokenResponse = getAccessToken.execute();
        // Build OAuthParameters in order to use them while accessing the resource
        OAuthParameters oauthParameters = new OAuthParameters();
        signer.tokenSharedSecret = accessTokenResponse.tokenSecret;
        oauthParameters.signer = signer;
        oauthParameters.consumerKey = Environment.GOODREADS_KEY;
        oauthParameters.token = accessTokenResponse.token;

        // Use OAuthParameters to access the desired Resource URL
        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(oauthParameters);
        GenericUrl genericUrl = new GenericUrl("https://www.goodreads.com/api/auth_user");
        HttpResponse resp = null;
        try {
            resp = requestFactory.buildGetRequest(genericUrl).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp.getContent();
    }
}

