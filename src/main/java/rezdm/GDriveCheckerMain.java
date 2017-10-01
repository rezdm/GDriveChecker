package rezdm;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class GDriveCheckerMain
{
    final static Logger log = LoggerFactory.getLogger(GDriveCheckerMain.class);
    public static void main( String[] args )
    {
        log.info("GDriveChecker starting");
        String configFileName = null != args && args.length >= 1 ? args[0] : "GDriveChecker-config.xml";

        Configuration configuration = null;
        try {
            configuration = LoadConfiguration(configFileName);
            pre_run(configuration);
        } catch (IOException ex ) {
            log.error("Exception while loading configuration, exiting application", ex);
        }
        log.info("GDriveChecker finished");
    }

    private static Configuration LoadConfiguration(String configFileName) throws IOException {
        final File file = new File(configFileName);
        final XmlMapper xmlMapper = new XmlMapper();
        final String xml = inputStreamToString(new FileInputStream(file));
        final Configuration configuration = xmlMapper.readValue(xml, Configuration.class);
        return configuration;
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

    private static void pre_run(Configuration configuration) {
        configuration.getFolders().parallelStream().forEach(GDriveCheckerMain::pre_processSingleFolder);
    }

    private static void pre_processSingleFolder(String folder) {
        log.info("Processing [" + folder + "]");
    }

}
