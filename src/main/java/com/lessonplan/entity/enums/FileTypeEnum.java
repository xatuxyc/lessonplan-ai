package com.lessonplan.entity.enums;

/**
 * 文件类型枚举
 */
public enum FileTypeEnum {

    PDF("pdf", "PDF文件"),
    DOC("doc", "Word 2003文件"),
    DOCX("docx", "Word 2007+文件");

    private String code;
    private String desc;

    FileTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FileTypeEnum getByCode(String code) {
        for (FileTypeEnum e : FileTypeEnum.values()) {
            if (e.getCode().equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(String suffix) {
        return getByCode(suffix) != null;
    }
}
