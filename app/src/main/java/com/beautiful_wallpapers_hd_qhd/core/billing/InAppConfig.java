package com.beautiful_wallpapers_hd_qhd.core.billing;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.SkuManager;

import java.util.HashMap;
import java.util.Map;

/**
 * In-app products configuration.
 * <p/>
 * Created by krozov on 01.09.14.
 */
public final class InAppConfig {
    //premium upgrade (non-consumable)
    public static final String SKU_PRO_VERSION = "sku_pro_version";

    //todo change key
    //Google Play
    public static final String GOOGLE_PLAY_KEY
            = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5F8fASyrDFdaXrkoW8kNtwH5JIkLnNuTD5uE1a37TbI5LDZR" +
            "VgvMIYAtZ9CAHAfLnJ6OEZt0lvLLJSKVuS47VqYVhGZciOkX8TEihONBRwis6i9A3JnKfyqm0iiT+P0CEktOLuFLROIo13" +
            "utCIO++6h7A7/WLfxNV+Jnxfs9OEHyyPS+MdHxa0wtZGeAGiaN65BymsBQo7J/ABt2DFyMJP1R/nJM45F8yu4D6wSkUNKz" +
            "s/QbPfvHJQzq56/B/hbx59EkzkInqC567hrlUlX4bU5IvOTF/B1G+UMuKg80m3I1IcQk4FD2D9oJ3E+8IXG/1UdejrOsmq" +
            "DAzE7LkMl8xwIDAQAB";

    public static Map<String, String> STORE_KEYS_MAP;

    public static void init() {
        STORE_KEYS_MAP = new HashMap<>();
        STORE_KEYS_MAP.put(OpenIabHelper.NAME_GOOGLE, InAppConfig.GOOGLE_PLAY_KEY);

        SkuManager.getInstance()
                .mapSku(SKU_PRO_VERSION, OpenIabHelper.NAME_GOOGLE, "pro_version");
    }

    private InAppConfig() {
    }
}
