package com.taobao.weex.lint;

/**
 * Created by moxun on 16/10/14.
 */
public interface LintResultType {
    int WRONG_PARENT = 0;
    int WRONG_CHILD = 1;
    int WRONG_CONTENT = 3;
    int WRONG_VALUE_ENUM = 4;
    int WRONG_VALUE_TYPE = 5;
    int UNRESOLVED_VAR = 6;
    int NULL_VALUE = 7;

    int UNKNOW = 99;

    int PASSED = 100;
}
