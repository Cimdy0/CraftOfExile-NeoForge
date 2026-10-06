package com.cimdy.coe.event.combat.status;

import com.cimdy.coe.data.helper.stat.StatStoreHelper;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.MobEffectRegistry;
import com.cimdy.coe.registry.MobEffectTagRegistry;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntry;
import com.cimdy.coe.event.data.LoadEvent;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.List;
import java.util.Map;

import static com.cimdy.coe.data.helper.stat.StatStoreHelper.addStatEntry;
import static com.cimdy.coe.data.helper.stat.StatStoreHelper.removeStatEntry;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class EffectEvent {
    public static final ContextKey<Map<Holder<MobEffect>, MobEffectInstance>> MOB_EFFECT = new ContextKey<>(
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "mob_effect"));
    public static final ContextKey<Float> EYE_HEIGHT = new ContextKey<>(
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "eye_height"));
    public static final ContextKey<Float> BOX_HEIGHT = new ContextKey<>(
            Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "box_height"));

    @SubscribeEvent
    public static void onEffect(EntityRenderersEvent.AddLayers event) {
//        for (EntityType<?> entityType : event.getEntityTypes()) {
//            EntityRenderer<?, ?> renderer = event.getRenderer(entityType);
//            if (renderer instanceof LivingEntityRenderer livingEntityRenderer) {
//                livingEntityRenderer.addLayer(
//                        new AilmentEffectLayer<>(livingEntityRenderer, event.getContext()));
//            }
//        }
//        Set<PlayerModelType> skins = event.getSkins();
//        for (PlayerModelType skin : skins) {
//            if (event.getPlayerRenderer(skin) instanceof AvatarRenderer playerRenderer) {
//                playerRenderer.addLayer(
//                        new AilmentEffectLayer<>(playerRenderer, event.getContext()));
//            }
//        }
    }

    @SubscribeEvent
    public static void registerStateModifiersEvent(RegisterRenderStateModifiersEvent event) {
//        event.registerEntityModifier(
//                new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {},
//                (livingData, state) -> {
//                    if (!(livingData instanceof Player)) {
//                        if (livingData.level().isClientSide()) {
//                            ClientPacketDistributor.sendToServer(
//                                    new MobEffect(Optional.of(livingData.getUUID()), Optional.empty()));
//                        }
//                        if (!livingData.getActiveEffects().isEmpty()) {
//                            state.setRenderData(EFFECT, livingData.getActiveEffectsMap());
//                            state.setRenderData(EYE_HEIGHT, state.eyeHeight);
//                            state.setRenderData(BOX_HEIGHT, (float) (livingData.getBoundingBox().maxY - livingData.getY()));
//                        }
//                    }
//                });
//
//        event.registerEntityModifier(
//                new TypeToken<LivingEntityRenderer<Player, LivingEntityRenderState, ?>>() {},
//                (player, state) -> {
//                    if (player.hasEffect(MobEffectRegistry.SHOCK)) {
//                        state.setRenderData(EFFECT, player.getActiveEffectsMap());
//                    }
//                });
    }

    @SubscribeEvent
    public static void LivingMobEffectAddedEvent(MobEffectEvent.Added event) {
        LivingEntity living = event.getEntity();
        if (living.level() instanceof ServerLevel) {
            MobEffectInstance instance = event.getEffectInstance();
            List<StatEntry> entries = LoadEvent.EFFECT_MODIFIER_MAP.get(instance.getEffect());
            if (entries != null && !entries.isEmpty()) {
                double mult = instance.getAmplifier() + 1;
                // 以原版效果的剩余时长作为计时 tick：即使 MobEffectEvent.Remove 漏触发，
                // StatStore.tick() 也会在同等时长后自动过期；与 Remove 事件显式移除互补。
                int ticks = instance.getDuration();
                if (ticks < 0) ticks = 0; // 无限效果（-1）仅靠 Remove 事件移除
                String prefix = "effect:" + instance.getEffect().getRegisteredName();
                int idx = 0;
                for (StatEntry entry : entries)
                    addStatEntry(living, prefix + "#" + (idx++), entry.scaled(mult), ticks);
            }
        }
    }

    @SubscribeEvent
    public static void LivingMobEffectRemoveEvent(MobEffectEvent.Remove event) {
        LivingEntity living = event.getEntity();
        if (living.level() instanceof ServerLevel) {
            MobEffectInstance instance = event.getEffectInstance();
            if (instance != null) {
                // 按来源前缀回滚该药水效果贡献的全部 Stat 条目（键匹配 '#' 之前的字符）
                removeStatEntry(living, "effect:" + instance.getEffect().getRegisteredName());
                // 部分药水效果 禁止非自然移除
                if (instance.getEffect().is(MobEffectTagRegistry.IS_AILMENT) && instance.getDuration() > 0) {
                    event.setCanceled(true);
                }
            }
        }
    }
}