package rezdm;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class LocalStorage {
    private final static Logger log = LoggerFactory.getLogger(LocalStorage.class);

    public void run() {
        try {
            final String resource = "rezdm/mybatis-config.xml";
            final InputStream inputStream = Resources.getResourceAsStream(resource);
            final Properties props = new Properties();
            props.setProperty("db_path", "./gdrivechecker.dbXYZ");
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, props);

            int z = 4/2;
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

    }
}
