package com.cimdy.coe.data.codec.support.stat.condition;

import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * 实体集运算（{@code and / or / not / chain} 共用）。
 * <p>
 * 按<b>实体身份</b>去重（{@code IdentityHashMap}）：实体的相等语义不由本模型定义，
 * 集合运算只应比较「是不是同一个实体」，故不依赖 {@code equals}。
 */
public final class ConditionSets {
    private ConditionSets() {}

    /** 去重（保持首次出现顺序）。 */
    public static List<LivingEntity> dedupe(Collection<LivingEntity> in) {
        List<LivingEntity> out = new ArrayList<>();
        Set<LivingEntity> seen = newIdentitySet();
        for (LivingEntity e : in) {
            if (e != null && seen.add(e)) out.add(e);
        }
        return out;
    }

    /** 交集（保持 a 中的顺序）。 */
    public static List<LivingEntity> intersect(Collection<LivingEntity> a, Collection<LivingEntity> b) {
        if (a.isEmpty() || b.isEmpty()) return List.of();
        Set<LivingEntity> other = newIdentitySet();
        for (LivingEntity e : b) if (e != null) other.add(e);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : a) {
            if (e != null && other.contains(e) && !containsIdentity(out, e)) out.add(e);
        }
        return out;
    }

    /** 并集（保持 a 在前、b 中新增者追加）。 */
    public static List<LivingEntity> union(Collection<LivingEntity> a, Collection<LivingEntity> b) {
        List<LivingEntity> out = new ArrayList<>();
        Set<LivingEntity> seen = newIdentitySet();
        for (LivingEntity e : a) if (e != null && seen.add(e)) out.add(e);
        for (LivingEntity e : b) if (e != null && seen.add(e)) out.add(e);
        return out;
    }

    /** 补集（相对入参集：base − removed）。 */
    public static List<LivingEntity> subtract(Collection<LivingEntity> base, Collection<LivingEntity> removed) {
        if (base.isEmpty()) return List.of();
        Set<LivingEntity> gone = newIdentitySet();
        for (LivingEntity e : removed) if (e != null) gone.add(e);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : base) {
            if (e != null && !gone.contains(e) && !containsIdentity(out, e)) out.add(e);
        }
        return out;
    }

    private static Set<LivingEntity> newIdentitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private static boolean containsIdentity(List<LivingEntity> list, LivingEntity e) {
        for (LivingEntity x : list) {
            if (x == e) return true;
        }
        return false;
    }
}
