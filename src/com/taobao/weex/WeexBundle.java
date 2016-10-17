package com.taobao.weex;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * Created by moxun on 16/10/11.
 */
public class WeexBundle {
    /**
     * The {@link ResourceBundle} path.
     */
    @NonNls
    protected static final String BUNDLE_NAME = "weex.localization.strings";
    /**
     * The {@link ResourceBundle} instance.
     *
     * @see #BUNDLE_NAME
     */
    protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private WeexBundle() {
    }

    /**
     * Load a {@link String} from the {@link #BUNDLE} {@link ResourceBundle}.
     *
     * @param key    the key of the resource.
     * @param params the optional parameters for the specific resource.
     * @return the {@link String} value or {@code null} if no resource found for the key.
     */
    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }

    /**
     * Load a {@link String} from the {@link #BUNDLE} {@link ResourceBundle}.
     *
     * @param key    the key of the resource.
     * @param params the optional parameters for the specific resource.
     * @return the {@link String} value or an empty {@link String} if no resource found for the key.
     */
    public static String messageOrBlank(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        return CommonBundle.messageOrDefault(BUNDLE, key, "test:foo", params);
    }
}
