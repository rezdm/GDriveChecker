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

/*
*
* I am not sure on thread-safety of all components of Google Api for Java;
* It does not heart having this "thread independent"
*
 */

public class GoogleServices {
    //private final String _ApplicationName = "GDriveChecker";
    private final HttpTransport _HttpTransport = GoogleNetHttpTransport.newTrustedTransport();
    private final JacksonFactory _JacksonFactory = JacksonFactory.getDefaultInstance();

    private final ThreadLocal<Drive> _drive;
    private final ThreadLocal<Gmail> _gmail;

    private Credential authorize(Configuration configuration) throws IOException, GeneralSecurityException {
        final InputStream in = new FileInputStream(configuration.getSecretFile());
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(_JacksonFactory, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = (new GoogleAuthorizationCodeFlow.Builder(_HttpTransport, _JacksonFactory, clientSecrets, Arrays.asList("https://www.googleapis.com/auth/drive.metadata.readonly", "https://www.googleapis.com/auth/gmail.compose"))).setDataStoreFactory(new FileDataStoreFactory(new File(configuration.getCredentialsStore()))).setAccessType("offline").build();
        return (new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())).authorize("user");
    }

    GoogleServices(Configuration configuration) throws IOException, GeneralSecurityException {
        _drive = ThreadLocal.withInitial(() -> threadLocalDrive(configuration));
        _gmail = ThreadLocal.withInitial(() -> threadLocalGmail(configuration));
    }

    private Drive threadLocalDrive(Configuration configuration) {
        try {
            Credential credential = authorize(configuration);
            return (new Drive.Builder(_HttpTransport, _JacksonFactory, credential)).setApplicationName("GDriveChecker").build();
        } catch (IOException | GeneralSecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Gmail threadLocalGmail(Configuration configuration) {
        try {
            Credential credential = authorize(configuration);
            return (new Gmail.Builder(_HttpTransport, _JacksonFactory, credential)).setApplicationName("GDriveChecker").build();
        } catch (IOException | GeneralSecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    Drive drive() {
        return _drive.get();
    }

    public Gmail gmail() {
        return _gmail.get();
    }
}
