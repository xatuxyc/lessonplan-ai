package com.lessonplan.entity.query;

/**
 * 教案文件查询参数
 */
public class LessonPlanQuery extends BaseParam {

    private static final long serialVersionUID = 1L;

    /** 文件名模糊查询 */
    private String fileNameFuzzy;

    /** 文件类型精确查询 */
    private String fileType;

    /** 上传人模糊查询 */
    private String uploaderFuzzy;

    /** 状态精确查询 */
    private Integer status;

    /** 上传时间起始 */
    private String uploadTimeStart;

    /** 上传时间结束 */
    private String uploadTimeEnd;

    public String getFileNameFuzzy() {
        return fileNameFuzzy;
    }

    public void setFileNameFuzzy(String fileNameFuzzy) {
        this.fileNameFuzzy = fileNameFuzzy;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getUploaderFuzzy() {
        return uploaderFuzzy;
    }

    public void setUploaderFuzzy(String uploaderFuzzy) {
        this.uploaderFuzzy = uploaderFuzzy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUploadTimeStart() {
        return uploadTimeStart;
    }

    public void setUploadTimeStart(String uploadTimeStart) {
        this.uploadTimeStart = uploadTimeStart;
    }

    public String getUploadTimeEnd() {
        return uploadTimeEnd;
    }

    public void setUploadTimeEnd(String uploadTimeEnd) {
        this.uploadTimeEnd = uploadTimeEnd;
    }
}
