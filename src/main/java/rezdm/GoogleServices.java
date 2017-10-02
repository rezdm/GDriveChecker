package rezdm;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.gmail.Gmail;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class GoogleServices {
    private final String _ApplicationName = "GDriveCheckerMailer";
    private final HttpTransport _HttpTransport = GoogleNetHttpTransport.newTrustedTransport();
    private final JacksonFactory _JacksonFactory = JacksonFactory.getDefaultInstance();
    private final Drive _drive;
    private final Gmail _gmail;

    private Credential authorize(Configuration configuration) throws IOException {
        InputStream in = new FileInputStream(configuration.getSecretFile());
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(this._JacksonFactory, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = (new GoogleAuthorizationCodeFlow.Builder(this._HttpTransport, this._JacksonFactory, clientSecrets, Arrays.asList("https://www.googleapis.com/auth/drive.metadata.readonly", "https://www.googleapis.com/auth/gmail.compose"))).setDataStoreFactory(new FileDataStoreFactory(new File(configuration.getCredentialsStore()))).setAccessType("offline").build();
        Credential credential = (new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())).authorize("user");
        return credential;
    }

    public GoogleServices(Configuration configuration) throws IOException, GeneralSecurityException {
        Credential credential = this.authorize(configuration);
        this._drive = (new com.google.api.services.drive.Drive.Builder(this._HttpTransport, this._JacksonFactory, credential)).setApplicationName("GDriveCheckerMailer").build();
        this._gmail = (new com.google.api.services.gmail.Gmail.Builder(this._HttpTransport, this._JacksonFactory, credential)).setApplicationName("GDriveCheckerMailer").build();
    }

    public Drive drive() {
        return this._drive;
    }

    public Gmail gmail() {
        return this._gmail;
    }
}
