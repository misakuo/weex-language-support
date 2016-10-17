package com.taobao.weex.lint;

/**
 * Created by moxun on 16/10/14.
 */
public class LintResult {

    private int code;
    private String desc;

    public LintResult() {
        this(LintResultType.UNKNOW, "");
    }

    public LintResult(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean passed() {
        return code == LintResultType.PASSED;
    }
}
