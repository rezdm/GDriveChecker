package rezdm;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rezdm.data.GFile;
import rezdm.data.GFileMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

final class AutoCloseableSession implements AutoCloseable {
    private final SqlSession Session;

    AutoCloseableSession(SqlSessionFactory sessionFactory)  {
        Session = sessionFactory.openSession();
    }

    public void close(){
        Session.commit();
        Session.close();
    }

    SqlSession getSession() {
        return Session;
    }
}

class LocalStorage {
    private final static Logger log = LoggerFactory.getLogger(LocalStorage.class);
    private final SqlSessionFactory SqlSessionFactory;
    private final static Class<GFileMapper> MapperClass = GFileMapper.class;

    LocalStorage(String location) throws IOException {
        final String resource = "rezdm/mybatis-config.xml";
        final InputStream inputStream = Resources.getResourceAsStream(resource);
        final Properties props = new Properties();
        props.setProperty("db_path", location);
        log.info(String.format("Create DB SqlSessionFactory with db location [%s]", location));
        SqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, props);
        createTable();
    }

    private <T> T DaoRun(Function<GFileMapper, T> f){
        try (final AutoCloseableSession session = new AutoCloseableSession(SqlSessionFactory)) {
            final GFileMapper mapper = session.getSession().getMapper(MapperClass);
            return f.apply(mapper);
        }
    }

    private void DaoRunVoid(Consumer<GFileMapper> f){
        try (final AutoCloseableSession session = new AutoCloseableSession(SqlSessionFactory)) {
            final GFileMapper mapper = session.getSession().getMapper(MapperClass);
            f.accept(mapper);
        }
    }

    private void createTable() {
        log.info("Create table if not exists");
        DaoRunVoid(GFileMapper::createTable);
    }

    public List<GFile> ReadGDriveFileInfo(){
        log.info("Read db -- load list of files");
        return DaoRun(GFileMapper::selectGDriveFileInfo);
    }

    public void WriteGDriveFileInfo(final List<GFile> files){
        log.info(String.format("Read db -- load list of [%d] files", files.size()));
        if(files.size() > 0) {
            DaoRunVoid((mapper) -> mapper.insertGDriveFileInfo(files));
        }
    }
}
