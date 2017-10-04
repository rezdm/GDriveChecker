package rezdm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rezdm.data.GDriveFileInfo;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GDriveCheckerMain {
    private final static Logger log = LoggerFactory.getLogger(GDriveCheckerMain.class);

    public static void main( String[] args ) {
        log.info("GDriveChecker starting");
        String configFileName = null != args && args.length >= 1 ? args[0] : "GDriveChecker-config.xml";

       try {
           final Configuration configuration = Configuration.read(configFileName);
           final GoogleServices services = new GoogleServices(configuration);

           final GDriveFileCollector collector = new GDriveFileCollector(services.drive(), configuration.getFolders(), configuration.getParallelGDriveQueries());
           if(!collector.collect()) {
               log.info("Collecting files information resulted in no files, exiting application");
               return;
           }
           Map<String, List<GDriveFileInfo>> remoteFiles = collector.collected();

           LocalStorage x = new LocalStorage();
           remoteFiles.values().forEach(x::run);
           int z = 4/2;
        } catch (IOException | GeneralSecurityException | InterruptedException | ExecutionException ex ) {
            log.error("Exception while loading configuration, exiting application", ex);
        }
        log.info("GDriveChecker finished");
    }

}
