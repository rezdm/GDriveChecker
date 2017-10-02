package rezdm;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.api.services.drive.Drive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.StringTokenizer;

public class GDriveCheckerMain
{
    final static Logger log = LoggerFactory.getLogger(GDriveCheckerMain.class);
    private Drive.About.Get request;

    public static void main( String[] args )
    {
        log.info("GDriveChecker starting");
        String configFileName = null != args && args.length >= 1 ? args[0] : "GDriveChecker-config.xml";

        Configuration configuration = null;
        try {
            configuration = LoadConfiguration(configFileName);
            pre_run(configuration);
        } catch (IOException | GeneralSecurityException ex ) {
            log.error("Exception while loading configuration, exiting application", ex);
        }
        log.info("GDriveChecker finished");
    }

    private static Configuration LoadConfiguration(String configFileName) throws IOException {
        final File file = new File(configFileName);
        final XmlMapper xmlMapper = new XmlMapper();
        final String xml = inputStreamToString(new FileInputStream(file));
        return xmlMapper.readValue(xml, Configuration.class);
    }

    private static String inputStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    private static void pre_run(Configuration configuration) throws IOException, GeneralSecurityException {
        final GoogleServices services = new GoogleServices(configuration);
        final String rootFolderId = services.drive().files().get("root").setFields("id").execute().getId();
        configuration.getFolders().parallelStream().forEach((folder) -> pre_processSingleFolder(folder, rootFolderId, services.drive()));
    }

    private static void pre_processSingleFolder(String folder, String rootFolderId, Drive drive)  {
        log.info("Processing [" + folder + "]");

        try {
            final String folderId = LocateFolderId(rootFolderId, folder, drive);
            log.info("Folder [" + folder + "] has id [" + folderId + "]");
            final List<com.google.api.services.drive.model.File> files = EnumerateFiles(drive, folderId);
            log.info("Folder [" + folder + "] contains [" + files.size() + "]");
            files.parallelStream().forEach((file) -> pre_processSingleFile(file, folder, drive));
        } catch (IOException | InvalidPathException ex) {
            log.error("Error enumerating files [" + folder + "]", ex);
        }
    }

    private static String LocateFolderId(String folderId, String folder, Drive drive) throws IOException, InvalidPathException {
        final StringTokenizer st = new StringTokenizer(folder, "/");
        String parentFolderId = folderId;
        do {
            String folderName = st.nextToken();
            if (null != folderName) {
                final List<com.google.api.services.drive.model.File> files = drive.files().list().setQ("name = '" + folderName + "' and '" + parentFolderId + "' in parents").execute().getFiles();
                if(files.size() != 1) {
                    throw new InvalidPathException("Wrong path given: [" + folder + "]", "Cannot find on Google Drive");
                }
                final com.google.api.services.drive.model.File file = files.get(0);
                parentFolderId = file.getId();
            }
        } while(st.hasMoreTokens());

        return parentFolderId;
    }

    private static List<com.google.api.services.drive.model.File> EnumerateFiles(Drive drive, String folderId) throws IOException {
        return drive.files().list().setQ("'" + folderId + "' in parents").execute().getFiles();
    }

    private static void pre_processSingleFile(com.google.api.services.drive.model.File file, String folder, Drive drive){
        log.info("Processing file [" + folder + "]/[" + file.getName() + "]");
    }

}
