package com.cimdy.coe.event.registry;

import com.cimdy.coe.entity.custom.skill.OfferingSpike;
import com.cimdy.coe.entity.custom.skill.visual.SkillBanner;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.BlockEntityRegistry;
import com.cimdy.coe.client.render.TerminalBlockEntityRender;
import com.cimdy.coe.data.codec.item.ItemInstances;
import com.cimdy.coe.data.codec.skill.core.SkillEntries;
import com.cimdy.coe.data.codec.entity.render.EntityRenderTypes;
import com.cimdy.coe.data.codec.entity.trigger.EntityTriggerTypes;
import com.cimdy.coe.data.codec.skill.input.InputModes;
import com.cimdy.coe.data.codec.skill.modifier.SkillModifierTypes;
import com.cimdy.coe.data.codec.skill.phase.action.ActionTypes;
import com.cimdy.coe.data.codec.skill.phase.delivery.DeliveryTypes;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.AreaDuration;
import com.cimdy.coe.data.codec.skill.phase.delivery.area.Shape;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.CollisionPolicy;
import com.cimdy.coe.data.codec.skill.phase.delivery.projectile.FlightPattern;
import com.cimdy.coe.data.codec.stronghold.task.TaskTypes;
import com.cimdy.coe.data.codec.stronghold.task.site.SiteTypes;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntries;
import com.cimdy.coe.data.codec.support.math.MathExpressions;
import com.cimdy.coe.data.codec.support.stat.entry.StatEntries;
import com.cimdy.coe.registry.world.EntityRegistry;
import com.cimdy.coe.entity.custom.skill.render.SkillEntityRender;
import com.cimdy.coe.data.codec.entity.behavior.EntityBehaviorTypes;
import com.cimdy.coe.data.codec.entity.behavior.EntityMotionTypes;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import com.cimdy.coe.data.codec.item.mod.core.ModifierEntry;

@EventBusSubscriber(modid = CraftOfExile.MODID)
public class RegisterEvent {

    @SubscribeEvent
    public static void NewRegistryEvent(NewRegistryEvent event) {
        event.register(MathExpressions.REGISTRY);
        event.register(ItemInstances.REGISTRY);
        event.register(ActionTypes.REGISTRY);
        event.register(DeliveryTypes.REGISTRY);
        event.register(InputModes.REGISTRY);
        event.register(SkillModifierTypes.REGISTRY);
        event.register(TaskTypes.REGISTRY);
        event.register(SiteTypes.REGISTRY);
        event.register(CollisionPolicy.REGISTRY);
        event.register(FlightPattern.REGISTRY);
        event.register(AreaDuration.REGISTRY);
        event.register(Shape.REGISTRY);
        event.register(StatEntries.REGISTRY);
        // 词缀分发注册表（ModifierEntry 多态派发）：与 StatEntries / MathExpressions 同构，漏注册会在
        // datagen 或首次取 Type 时抛 IllegalStateException。
        event.register(ModifierEntries.REGISTRY);
        // SkillEntries（技能条目派发注册表）此前漏注册：与 StatEntries 同构却未列入，
        // 只能靠静态初始化自行 create()，一旦在 NewRegistryEvent 之后首次被加载（例如 datagen
        // dump 阶段 SkillRead.getType() 取 SkillEntries.READ）即抛 IllegalStateException。
        event.register(SkillEntries.REGISTRY);
        // 道具渲染层（EntityRenderTypes）：与 ActionTypes / DeliveryTypes 同构的注册式多态，
        // 漏注册会在 datagen 首次取 Type 时抛 IllegalStateException（同 SkillEntries 既有缺陷）。
        event.register(EntityRenderTypes.REGISTRY);
        // 道具触发效果（EntityTriggerTypes）：与 EntityRenderTypes 同构，漏注册同样会在 datagen 取 Type 时抛异常。
        event.register(EntityTriggerTypes.REGISTRY);
        // 实体运动 / 行为组件（EntityMotionTypes / EntityBehaviorTypes）：实体与行为拆分后的两个分类维度，
        // 与 EntityTriggerTypes 同构 —— 漏注册会在 datagen 取 Type 时抛 IllegalStateException。
        event.register(EntityMotionTypes.REGISTRY);
        event.register(EntityBehaviorTypes.REGISTRY);
    }

    @SubscribeEvent
    public static void registerSpecialModelRendererEvent(RegisterSpecialModelRendererEvent event) {

    }

    @SubscribeEvent
    public static void RegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {

    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                BlockEntityRegistry.TERMINAL_BLOCK_ENTITY.get(), TerminalBlockEntityRender::new);
        event.registerEntityRenderer(
                EntityRegistry.ENDER_BALL.get(), ThrownItemRenderer::new);

        event.registerEntityRenderer(
                EntityRegistry.SKILL_ARROW.get(), SkillEntityRender::new);
        event.registerEntityRenderer(
                EntityRegistry.SKILL_TOTEM.get(), NoopRenderer::new);
        // 战旗：渲染体为 Noop（与图腾同范式），可见性由 SkillBanner 自身发射的粒子提供
        event.registerEntityRenderer(
                EntityRegistry.SKILL_BANNER.get(), NoopRenderer::new);
        // 世界物件统一渲染器：coe:prop / coe:orb / coe:grenade / coe:skill_ball 共用同一个注册基类，
        // 外观全部由 coe:entity_render 数据（同步的 render id + 半径）决定，故一个渲染器覆盖全部四类。
        event.registerEntityRenderer(
                EntityRegistry.PROP.get(), SkillEntityRender::new);
        event.registerEntityRenderer(
                EntityRegistry.ORB.get(), SkillEntityRender::new);
        event.registerEntityRenderer(
                EntityRegistry.GRENADE.get(), SkillEntityRender::new);
        event.registerEntityRenderer(
                EntityRegistry.SKILL_BALL.get(), SkillEntityRender::new);
        // 裂隙：同为 SkillEntity 承载（RenderIdCarrier），故复用同一渲染器即可；
        // 不注册则 coe:entity_render/fissure 无任何渲染器取用（该实体也无从显示）。
        event.registerEntityRenderer(
                EntityRegistry.SKILL_FISSURE.get(), SkillEntityRender::new);
        // 奉献尖刺：渲染体为 Noop（与图腾同范式），可见性由 OfferingSpike 自身发射的粒子提供
        event.registerEntityRenderer(
                EntityRegistry.OFFERING_SPIKE.get(), NoopRenderer::new);

    }

    @SubscribeEvent
    public static void registerRenderStateModifiersEvent(RegisterRenderStateModifiersEvent event) {
        // 投射物（coe:skill_arrow）现统一由 SkillEntityRender（ModelEntityRender）渲染，
        // 其贴图经 SkillSkinDefs.resolveRender、描边经 SkillSkinDefs.get(renderId).tint() 解析，
        // 不再需要 SkillArrow 专属的渲染态修饰器。
    }

}
