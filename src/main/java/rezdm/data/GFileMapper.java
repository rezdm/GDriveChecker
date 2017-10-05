package rezdm.data;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GFileMapper {
    List<GFile> selectGDriveFileInfo();
    void createTable();
    void insertGDriveFileInfo(@Param("GFileList") List<GFile> GFileList);
}
