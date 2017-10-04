package rezdm.data;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GDriveFileInfoMapper {
    List<GDriveFileInfo> selectGDriveFileInfo();
    void createTable();
    void insertGDriveFileInfo(@Param("GDriveFileInfoList") List<GDriveFileInfo> GDriveFileInfoList);
}
