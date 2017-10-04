package rezdm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rezdm.data.GDriveFileInfo;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

           log.info(String.format("Initialize local storage at [%s]", configuration.getDbLocation()));
           final LocalStorage storage = new LocalStorage(configuration.getDbLocation());

           final Map<String, List<GDriveFileInfo>> remoteFiles = new HashMap<>();
           final List<GDriveFileInfo> storedFiles = new ArrayList<>();

           final ExecutorService executor = Executors.newCachedThreadPool();

           log.info("Read from Google drive and read from local storage -- begin");
           RunSimultaneously(
               () -> {if(collector.collect()) { remoteFiles.putAll(collector.collected());}},
               () -> storedFiles.addAll(storage.ReadGDriveFileInfo())
           );
           log.info("Read from Google drive and read from local storage -- done");

        } catch (IOException | GeneralSecurityException | InterruptedException ex ) {
            log.error("Exception while loading configuration, exiting application", ex);
        }
        log.info("GDriveChecker finished");
    }

    private static void RunSimultaneously(Runnable ... tasks) throws InterruptedException {
        final ExecutorService executor = Executors.newCachedThreadPool();
        Arrays.stream(tasks).forEach(executor::submit);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }

}
