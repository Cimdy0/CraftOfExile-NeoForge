package com.cimdy.coe.data.gen.asset;

import com.cimdy.coe.data.gen.gem.GemTierProperty;
import com.cimdy.coe.CraftOfExile;
import com.cimdy.coe.registry.world.BlockRegistry;
import com.cimdy.coe.block.custom.TerminalBlock;
import com.cimdy.coe.registry.DataComponentRegistry;
import com.cimdy.coe.data.gen.gem.GemGen;
import com.cimdy.coe.data.codec.item.essence.EssenceRegistry;
import com.cimdy.coe.data.codec.item.essence.EssenceState;
import com.cimdy.coe.data.codec.item.RuneRegistry;
import com.cimdy.coe.data.codec.item.state.RuneState;
import com.cimdy.coe.registry.item.ItemRegistry;
import com.cimdy.coe.item.gem.UncutGem;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;

public class CoeModelProvider extends ModelProvider {
    public CoeModelProvider(PackOutput output) {
        super(output, CraftOfExile.MODID);
    }

    /**
     * 去掉宝石 ID 路径中的颜色段（首段 red/green/blue），返回末段名称。
     * 例如 {@code red/rolling_slam -> rolling_slam}，用于生成不含颜色子目录的贴图路径。
     */
    private static String stripColor(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    /**
     * 注册单个宝石的物品模型（模型文件 + ITEM_MODEL 映射）。
     * 以 texName（去颜色段后的 basename）跨全局去重，防止同名技能跨组重复注册。
     */
    private void registerGemItemModel(ItemModelGenerators itemModels, Identifier gemId, Set<String> seen) {
        String texName = stripColor(gemId.getPath());
        if (!seen.add(texName)) {
            // 同一 texName 出现在多个宝石清单（如分类归属冲突 / 重复登记）时，第二个会被静默跳过，
            // 导致「清单里明明有、模型却没生成」难以排查。保留去重（防 modelResourceId 重复注册），
            // 但把冲突 id 打成 warn，便于日后发现。
            CraftOfExile.LOGGER.warn(
                    "[CoeModelProvider] duplicate gem texName '{}' skipped (id '{}' already registered via another gem list)",
                    texName, gemId);
            return;
        }
        Identifier itemModelId = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, texName);
        Identifier modelResourceId = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "item/" + texName);
        TextureMapping mapping = new TextureMapping().put(TextureSlot.LAYER0,
                new Material(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "item/gem/" + texName)));
        ModelTemplates.FLAT_ITEM.create(modelResourceId, mapping, itemModels.modelOutput);
        itemModels.itemModelOutput.register(itemModelId,
                new ClientItem(ItemModelUtils.plainModel(modelResourceId), ClientItem.Properties.DEFAULT));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        List<DeferredItem<Item>> allItems = new ArrayList<>(ItemRegistry.allCurrencyItem);
        allItems.add(ItemRegistry.ENDER_BALL);
        allItems.add(ItemRegistry.SKILL_GEM);
        allItems.add(ItemRegistry.SUPPORT_GEM);
        allItems.add(ItemRegistry.SPIRIT_GEM);
        allItems.add(ItemRegistry.FOCI);
        allItems.add(ItemRegistry.BUCKLER);
        // 珠宝类物品也需物品模型定义，否则 ModelProvider 校验会报 Missing item model definitions。
        allItems.add(ItemRegistry.JEWEL);
        allItems.add(ItemRegistry.JEWEL_OF_RADIANCE);
        // Flask（POE2 实证）：复用原版药水瓶扁平图标（纹理为后续美术项，机制先行）。
        allItems.add(ItemRegistry.LIFE_FLASK);
        allItems.add(ItemRegistry.MANA_FLASK);
        // Charm（护符，POE2 实证）：复用扁平物品图标（纹理为后续美术项，机制先行）。
        allItems.add(ItemRegistry.RUBY_CHARM);
        allItems.add(ItemRegistry.SAPPHIRE_CHARM);
        allItems.add(ItemRegistry.TOPAZ_CHARM);
        allItems.add(ItemRegistry.AMETHYST_CHARM);
        allItems.add(ItemRegistry.DOUSING_CHARM);
        allItems.add(ItemRegistry.THAWING_CHARM);
        allItems.add(ItemRegistry.GROUNDING_CHARM);
        allItems.add(ItemRegistry.STAUNCHING_CHARM);
        allItems.add(ItemRegistry.ANTIDOTE_CHARM);
        allItems.add(ItemRegistry.STONE_CHARM);
        allItems.add(ItemRegistry.SILVER_CHARM);
        allItems.add(ItemRegistry.GOLDEN_CHARM);
        // 饰品栏物品（腿甲/箭袋/护身符）：注册为真实物品（coe:legs / coe:quiver / coe:amulet），
        // 必须在此登记物品模型定义，否则 ModelProvider 校验抛 Missing item model definitions 并中断全部 datagen。
        allItems.add(ItemRegistry.LEGS);
        allItems.add(ItemRegistry.QUIVER);
        allItems.add(ItemRegistry.AMULET);
        // 核心装备载体物品（武器/护甲/戒指/腰带/盾牌/界桩/护符）：均为数据驱动的真实物品，
        // 同样必须登记物品模型定义，否则 ModelProvider.finalizeAndValidate 抛 Missing item model definitions，
        // 中断全部 datagen（含 stat 子段 Provider，导致新增 JSON 无法生成）。纹理为后续美术项，机制先行，沿用扁平图标。
        allItems.add(ItemRegistry.BOW);
        allItems.add(ItemRegistry.ONE_HAND_SWORD);
        allItems.add(ItemRegistry.TWO_HAND_SWORD);
        allItems.add(ItemRegistry.ONE_HAND_AXE);
        allItems.add(ItemRegistry.TWO_HAND_AXE);
        allItems.add(ItemRegistry.CROSS_BOW);
        allItems.add(ItemRegistry.CLAW);
        allItems.add(ItemRegistry.DAGGER);
        allItems.add(ItemRegistry.ONE_HAND_MACE);
        allItems.add(ItemRegistry.TWO_HAND_MACE);
        allItems.add(ItemRegistry.SCEPTRE);
        allItems.add(ItemRegistry.SPEAR);
        allItems.add(ItemRegistry.FLAIL);
        allItems.add(ItemRegistry.WAND);
        allItems.add(ItemRegistry.STAFF);
        allItems.add(ItemRegistry.QUARTERSTAFF);
        allItems.add(ItemRegistry.TRAP);
        allItems.add(ItemRegistry.TALISMAN);
        allItems.add(ItemRegistry.SHIELD);
        allItems.add(ItemRegistry.CHEST);
        allItems.add(ItemRegistry.HEAD);
        allItems.add(ItemRegistry.FEET);
        allItems.add(ItemRegistry.RING);
        allItems.add(ItemRegistry.BELT);
        // PoE2 美术实装测试物品：贴图 item/<snake>.png（由 poe2_assets 生成），扁平模型。
        allItems.add(ItemRegistry.SHABBY_HOOD);
        allItems.add(ItemRegistry.ABYSSAL_CUIRASS);
        allItems.add(ItemRegistry.BRONZE_GREAVES);
        allItems.add(ItemRegistry.BLACKSTEEL_GAUNTLETS);

        allItems.forEach(item -> {
            itemModels.createFlatItemModel(item.get(), ModelTemplates.FLAT_ITEM);
            itemModels.itemModelOutput.accept(item.get(), ItemModelUtils.plainModel(
                    Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "item/" + item.getId().getPath())));
        });

        // 精髓物品：贴图与既有 CamelCase 图标一致（item/essence/<id>.png）。
        EssenceRegistry.init();
        for (EssenceState s : EssenceRegistry.all()) {
            String regName = s.id().toLowerCase(Locale.ROOT);
            Identifier modelResourceId = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "item/" + regName);
            Identifier itemModelId = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, regName);
            String essTex = s.modelTexture();
            TextureMapping mapping = new TextureMapping().put(TextureSlot.LAYER0,
                    new Material(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, essTex)));
            ModelTemplates.FLAT_ITEM.create(modelResourceId, mapping, itemModels.modelOutput);
            itemModels.itemModelOutput.register(itemModelId,
                    new ClientItem(ItemModelUtils.plainModel(modelResourceId), ClientItem.Properties.DEFAULT));
        }

        // 符文物品：贴图由 icon 字段精确指向（item/rune/<id>.png，每阶独立纹理）。
        RuneRegistry.init();
        for (RuneState s : RuneRegistry.all()) {
            String regName = s.id().toLowerCase(Locale.ROOT);
            Identifier modelResourceId = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "item/" + regName);
            Identifier itemModelId = Identifier.fromNamespaceAndPath(CraftOfExile.MODID, regName);
            String runeTex = s.modelTexture();
            TextureMapping mapping = new TextureMapping().put(TextureSlot.LAYER0,
                    new Material(Identifier.fromNamespaceAndPath(CraftOfExile.MODID, runeTex)));
            ModelTemplates.FLAT_ITEM.create(modelResourceId, mapping, itemModels.modelOutput);
            itemModels.itemModelOutput.register(itemModelId,
                    new ClientItem(ItemModelUtils.plainModel(modelResourceId), ClientItem.Properties.DEFAULT));
        }

        // 未切割宝石：按等阶生成各自模型（纹理 uncut_<type>_N.png），
        // 物品模型用 minecraft:select + 自定义属性 coe:gem_tier（由 GemTierProperty 从 MODIFIABLE_STATE 取等阶）
        // 按等阶切换贴图。属性 id 必须经 RegisterSelectItemModelPropertyEvent 登记，否则 datagen 编码模型时抛
        // "Element with unknown id"。
        this.registerUncutGemModels(itemModels, ItemRegistry.UNCUT_GEM, "uncut_skill_gem", UncutGem.SKILL_TIER_MAX);
        this.registerUncutGemModels(itemModels, ItemRegistry.UNCUT_SUPPORT_GEM, "uncut_support_gem", UncutGem.SUPPORT_TIER_MAX);
        this.registerUncutGemModels(itemModels, ItemRegistry.UNCUT_SPIRIT_GEM, "uncut_spirit_gem", UncutGem.SKILL_TIER_MAX);

        itemModels.createFlatItemModel(BlockRegistry.TERMINAL_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(BlockRegistry.TERMINAL_ITEM.get(), ItemModelUtils.plainModel(
                Identifier.fromNamespaceAndPath(CraftOfExile.MODID, "item/" + BlockRegistry.TERMINAL_ITEM.getId().getPath())));

        // ===================== 全宝石物品模型（统一去重） =====================
        // 技能/辅助/精魂宝石均为虚拟物品，ITEM_MODEL 指向 coe:<name>。
        // 因同一技能名可能跨组出现（如 herald_of_thunder 同时是技能和精魂宝石），
        // 必须用全局 Set 去重，避免 modelResourceId 重复注册引发 IllegalStateException。
        Set<String> seenAllGemTex = new HashSet<>();
        for (Identifier gemId : GemGen.SKILL_IDS) {
            registerGemItemModel(itemModels, gemId, seenAllGemTex);
        }
        for (Identifier gemId : GemGen.SUPPORT_GEM_IDS) {
            registerGemItemModel(itemModels, gemId, seenAllGemTex);
        }
        for (Identifier gemId : GemGen.SPIRIT_IDS) {
            registerGemItemModel(itemModels, gemId, seenAllGemTex);
        }

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        BlockRegistry.TERMINAL.get(),
                        BlockModelGenerators.
                                variant(new Variant(Identifier.
                                        fromNamespaceAndPath(CraftOfExile.MODID,
                                                "block/" + BlockRegistry.TERMINAL.getId().getPath())))
                ).with(
                        PropertyDispatch.modify(TerminalBlock.FACING)
                                .select(Direction.EAST, BlockModelGenerators.Y_ROT_270)
                                .select(Direction.NORTH, BlockModelGenerators.Y_ROT_180)
                                .select(Direction.SOUTH, BlockModelGenerators.NOP)
                                .select(Direction.WEST, BlockModelGenerators.Y_ROT_90)
                )
        );

        // ===================== 界桩 / 研究台：单态方块（cube_all），引用手写 models/block/*.json =====================
        // 模型 JSON 与 16x16 材质在 src/main/resources/assets/coe/{models,textures}/block 下；
        // 用 plainVariant 直接指定方块模型 Identifier，避免 createTrivialCube 强制生成模型文件覆盖手写版。
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        BlockRegistry.STRONGHOLD_MARKER.get(),
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(
                                CraftOfExile.MODID, "block/hold_marker"))
                )
        );
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        BlockRegistry.RESEARCH_BENCH.get(),
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(
                                CraftOfExile.MODID, "block/research_bench"))
                )
        );

        // 物品模型：方块物品直接引用方块模型（手持显示 3D 方块），无需额外 item 模型文件。
        itemModels.itemModelOutput.accept(BlockRegistry.STRONGHOLD_MARKER_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(
                        CraftOfExile.MODID, "block/hold_marker")));
        itemModels.itemModelOutput.accept(BlockRegistry.RESEARCH_BENCH_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(
                        CraftOfExile.MODID, "block/research_bench")));

        // ===================== 城镇水井：单态方块（cube_all），引用手写 models/block/well.json =====================
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        BlockRegistry.WELL.get(),
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(
                                CraftOfExile.MODID, "block/well"))
                )
        );
        itemModels.itemModelOutput.accept(BlockRegistry.WELL_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(
                        CraftOfExile.MODID, "block/well")));
    }

    /**
     * 为未切割宝石按等阶生成模型：每个等阶一份 {@code item/<texBase>_N} 模型资源（引用对应纹理），
     * 物品模型则使用 {@code minecraft:select} + 自定义属性 {@code coe:gem_tier}，
     * 按物品上统一组件 {@code MODIFIABLE_STATE} 内的 {@code GemState} 等阶在等阶间切换贴图（由 {@code GemTierProperty} 提取）。
     * 该属性在 {@code GemTierProperty} 中经 {@code RegisterSelectItemModelPropertyEvent} 登记，
     * 客户端与 datagen 两条路径共用同一份注册。
     */
    private void registerUncutGemModels(ItemModelGenerators itemModels, DeferredItem<Item> item,
                                        String texBase, int maxTier) {
        String modid = CraftOfExile.MODID;
        // 每个等阶一份扁平模型资源
        for (int t = 1; t <= maxTier; t++) {
            Identifier modelId = Identifier.fromNamespaceAndPath(modid, "item/" + texBase + "_" + t);
            TextureMapping mapping = new TextureMapping().put(TextureSlot.LAYER0,
                    new Material(Identifier.fromNamespaceAndPath(modid, "item/" + texBase + "_" + t)));
            ModelTemplates.FLAT_ITEM.create(modelId, mapping, itemModels.modelOutput);
        }
        // 物品模型：按 gem_tier 组件选择对应等阶模型（fallback 取等阶 1）
        SelectItemModelProperty<Integer> property =
                new GemTierProperty();
        ItemModel.Unbaked fallback = ItemModelUtils.plainModel(
                Identifier.fromNamespaceAndPath(modid, "item/" + texBase + "_1"));
        List<SelectItemModel.SwitchCase<Integer>> cases = new ArrayList<>();
        for (int t = 1; t <= maxTier; t++) {
            cases.add(ItemModelUtils.when(t, ItemModelUtils.plainModel(
                    Identifier.fromNamespaceAndPath(modid, "item/" + texBase + "_" + t))));
        }
        ItemModel.Unbaked model = ItemModelUtils.select(property, fallback, cases);
        itemModels.itemModelOutput.accept(item.get(), model);
    }
}
