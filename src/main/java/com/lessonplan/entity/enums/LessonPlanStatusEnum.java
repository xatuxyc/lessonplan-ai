package com.lessonplan.entity.enums;

/**
 * 教案文件状态枚举
 */
public enum LessonPlanStatusEnum {

    UPLOADED(0, "已上传"),
    PARSING(1, "解析中"),
    PARSE_SUCCESS(2, "解析成功"),
    PARSE_FAILED(3, "解析失败");

    private Integer status;
    private String desc;

    LessonPlanStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static LessonPlanStatusEnum getByStatus(Integer status) {
        for (LessonPlanStatusEnum e : LessonPlanStatusEnum.values()) {
            if (e.getStatus().equals(status)) {
                return e;
            }
        }
        return null;
    }
}
