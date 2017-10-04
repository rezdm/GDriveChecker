package rezdm;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rezdm.data.GDriveFileInfo;
import rezdm.data.GDriveFileInfoMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

class LocalStorage {
    private final static Logger log = LoggerFactory.getLogger(LocalStorage.class);

    void run(List<GDriveFileInfo> files) {
        try {
            final String resource = "rezdm/mybatis-config.xml";
            final InputStream inputStream = Resources.getResourceAsStream(resource);
            final Properties props = new Properties();
            props.setProperty("db_path", "./.gdrivechecker.dbXYZ/zz");
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, props);

            {
                SqlSession session = sqlSessionFactory.openSession();
                try {
                    GDriveFileInfoMapper mapper = session.getMapper(rezdm.data.GDriveFileInfoMapper.class);
                    mapper.createTable();
                    //
                    //final List<GDriveFileInfo> files = mapper.selectGDriveFileInfo();
                    //int zz = files.size();
                    //
                    mapper.insertGDriveFileInfo(files);
                    session.commit();
                } finally {
                    session.close();
                }
            }

            {
                SqlSession session = sqlSessionFactory.openSession();
                try {
                    GDriveFileInfoMapper mapper = session.getMapper(rezdm.data.GDriveFileInfoMapper.class);
                    //mapper.createTable();

                    final List<GDriveFileInfo> _files = mapper.selectGDriveFileInfo();
                    int zz = _files.size();
                    int zzz = zz/2;
                } finally {
                    session.close();
                }
            }

        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

    }
}
