package com.cimdy.coe.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Locale;

/**
 * 安全获取客户端语言 {@link Locale}（P3）：仅在客户端环境可达；
 * 服务端（或客户端尚未就绪）时回落 {@link Locale#US}。
 *
 * <p>调用方（物品 tooltip / 天赋树 / 珠宝悬浮文本）多为通用（common）代码路径，
 * 故以 {@link FMLEnvironment#getDist()} 守卫，避免服务端类加载期触碰 client-only 的 {@link Minecraft}。
 */
public final class ClientLocale {

    private ClientLocale() {
    }

    /** 客户端语言 Locale；非客户端、客户端未就绪或缺省时回落 {@link Locale#US}。 */
    public static Locale get() {
        try {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                // getSelected() 返回 MC 语言码（"en_us" / "zh_cn"），转 BCP-47（en-US / zh-CN）后构造 Locale。
                String code = Minecraft.getInstance().getLanguageManager().getSelected();
                if (code != null && !code.isBlank()) {
                    return Locale.forLanguageTag(code.replace('_', '-'));
                }
            }
        } catch (Exception ignored) {
            // FML 未加载（服务端 bootstrap / 无头工具 / 单元测试）或客户端尚未就绪：回落 Locale.US。
        }
        return Locale.US;
    }
}
