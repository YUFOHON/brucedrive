package com.ywy.task;

import com.ywy.pojo.entity.SysFileInfo;
import com.ywy.pojo.enums.FileDelFlagEnum;
import com.ywy.pojo.param.FileInfoParam;
import com.ywy.service.SysFileInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileTask {
    @Resource
    private SysFileInfoService sysFileInfoService;

    /**
     * Scheduled cleanup of expired files in the recycle bin
     */
    @Scheduled(fixedDelay = 1000 * 60 * 3)
    public void deleteRecycleExpireFile() {
//Query expired files in the recycle bin
        FileInfoParam param = new FileInfoParam();
        param.setDelFlag(FileDelFlagEnum.RECYCLE.getFlag());
        param.setQueryExpireTime(true);
        List<SysFileInfo> fileInfoList = sysFileInfoService.findListByParam(param);
//Group by user ID and delete in batches
        Map<String, List<SysFileInfo>> listMap = fileInfoList.stream().collect(Collectors.groupingBy(SysFileInfo::getUserId));
        for (Map.Entry<String, List<SysFileInfo>> entry: listMap.entrySet()) {
//Filter out file IDs
            List<String> fileIds = entry.getValue().stream().map(f -> f.getId()).collect(Collectors.toList());
            sysFileInfoService.deleteFile(String.join(",", fileIds), entry.getKey(), false);
        }
    }
}