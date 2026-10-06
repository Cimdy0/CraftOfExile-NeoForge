package com.cimdy.coe;

import com.cimdy.coe.entity.custom.skill.totem.BallistaTotem;
import com.cimdy.coe.entity.custom.skill.totem.SkillTotem;
import com.cimdy.coe.entity.custom.skill.totem.TrapEntity;
import com.cimdy.coe.registry.world.BlockRegistry;
import com.cimdy.coe.registry.world.BlockEntityRegistry;
import com.cimdy.coe.client.animation.DodgeRollAnimation;
import com.cimdy.coe.client.animation.SkillCastAnimation;
import com.cimdy.coe.BlocksInfo;
import com.cimdy.coe.MobsInfo;
import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.cimdy.coe.registry.AttributeRegistry;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.registry.TicketTypeRegistry;
import com.cimdy.coe.registry.MobEffectRegistry;
import com.cimdy.coe.registry.SoundEventsRegistry;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.cimdy.coe.event.EventCatalog;
import com.cimdy.coe.registry.item.ItemRegistry;
import com.cimdy.coe.ui.menu.MenuRegistry;
import com.cimdy.coe.ui.widget.render.StylePresets;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@Mod(CraftOfExile.MODID)
public class CraftOfExile {
    public static final String MODID = "coe";

    public static final Identifier EMPTY_LOCATION = Identifier.fromNamespaceAndPath(MODID, "empty");

    public static final Logger LOGGER = LogUtils.getLogger();

    public CraftOfExile(IEventBus modEventBus, ModContainer modContainer) {
        EventCatalog.audit();
        CoeTab.CREATIVE_MODE_TABS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);
        BlockEntityRegistry.BLOCK_ENTITY_TYPES.register(modEventBus);
        EntityRegistry.ENTITY_TYPES.register(modEventBus);
        MobEffectRegistry.MOB_EFFECTS.register(modEventBus);
        SoundEventsRegistry.SOUNDS.register(modEventBus); // 附录 D.3：UI 附件自定义音效（资源占位，默认原版兜底）
        AttachmentRegistry.ATTACHMENTS.register(modEventBus);
        AttributeRegistry.ATTRIBUTES.register(modEventBus);
        DataComponentRegistry.DATA_COMPONENT_TYPES.register(modEventBus);
        TicketTypeRegistry.TICKET_TYPE.register(modEventBus);
        MenuRegistry.register(modEventBus);
        // 技能施放动画（PAL）：客户端 MOD 总线注册（工厂注册须早于玩家实体创建）
        SkillCastAnimation.register(modEventBus);
        DodgeRollAnimation.register(modEventBus);
        // 风格预设：客户端启动从 config/coe/styles.json 加载并应用（B2 预设系统）
        StylePresets.register(modEventBus);
        // 实体属性：SkillTotem Living 化后必须注册 MAX_HEALTH 等属性，
        // 否则 getHealth/getMaxHealth 因缺 attribute supplier 崩溃。
        modEventBus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) ->
                event.put(EntityRegistry.SKILL_TOTEM.get(),
                        com.cimdy.coe.entity.custom.skill.totem.SkillTotem.createAttributes().build()));
        // 箭塔复用 SkillTotem 的通用 Living 属性（createAttributes 为 static 通用实现，等价），
        // 但其注册为独立 EntityType（coe:ballista），必须单独 put，否则 getHealth/getMaxHealth 缺 supplier 崩溃。
        modEventBus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) ->
                event.put(EntityRegistry.BALLISTA.get(),
                        com.cimdy.coe.entity.custom.skill.totem.BallistaTotem.createAttributes().build()));
        // 陷阱与箭塔同构：复用 SkillTotem 的通用 Living 属性，但注册为独立 EntityType（coe:trap），
        // 必须单独 put，否则 getHealth/getMaxHealth 缺 supplier 崩溃。
        modEventBus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) ->
                event.put(EntityRegistry.TRAP.get(),
                        com.cimdy.coe.entity.custom.skill.totem.TrapEntity.createAttributes().build()));
        // 地雷与陷阱同构：复用 SkillTotem 的通用 Living 属性，但注册为独立 EntityType（coe:mine），
        // 必须单独 put，否则 getHealth/getMaxHealth 缺 supplier 崩溃。
        modEventBus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) ->
                event.put(EntityRegistry.MINE.get(),
                        com.cimdy.coe.entity.custom.skill.totem.MineEntity.createAttributes().build()));

        modContainer.registerConfig(ModConfig.Type.COMMON, CoeConfig.SPEC);
    }

    @WailaPlugin
    public static class JadePlugin implements IWailaPlugin {
        @Override
        public void register(IWailaCommonRegistration registration) {
            registration.registerEntityDataProvider(MobsInfo.INSTANCE, LivingEntity.class);
            registration.registerBlockDataProvider(BlocksInfo.INSTANCE, Block.class);
        }

        @Override
        public void registerClient(IWailaClientRegistration registration) {
            registration.registerEntityComponent(MobsInfo.Client.INSTANCE, LivingEntity.class);
            registration.registerBlockComponent(BlocksInfo.Client.INSTANCE, Block.class);
        }
    }
}
