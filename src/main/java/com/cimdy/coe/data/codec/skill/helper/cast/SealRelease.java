package com.cimdy.coe.data.codec.skill.helper.cast;

import com.cimdy.coe.data.codec.skill.helper.state.EchoState;
import com.cimdy.coe.data.codec.skill.core.SkillInstance;
import com.cimdy.coe.data.codec.skill.runtime.SkillPhaseInstance;
import com.cimdy.coe.data.codec.skill.phase.SkillPhase;

import java.util.ArrayList;
import java.util.List;

/**
 * 封印释放调度器 — 存满封印后下一次施放触发，按持有封印数延迟重放阶段实例。
 *
 * <p>镜像 {@link EchoState}：重放经 {@link SkillPhase#repeat} 完成（不二次消耗资源 / 冷却 /
 * 触发「施放时」效果），仅重做投递物与阶段实例。延迟以 tick 计（下限 1 tick），
 * 每批重放落在 {@code step × 批次序号} tick 后，避免同一帧堆叠多份投递物。
 */
public class SealRelease {

    private record Pending(SkillPhase phase, int delay, double areaInc, double areaMore) {}

    private final List<Pending> pending = new ArrayList<>();

    /** 登记一次释放：对本次施放的 fresh 阶段按 {@code repeats} 批次各重放一次（每批延迟 step×批序号）。 */
    public void schedule(List<SkillPhaseInstance> fresh, int repeats, int step, double areaInc) {
        if (fresh == null || fresh.isEmpty() || repeats <= 0) return;
        for (int r = 0; r < repeats; r++) {
            int delay = Math.max(1, step) * (r + 1);
            for (SkillPhaseInstance inst : fresh) {
                SkillPhase phase = inst.getPhase();
                if (phase != null) pending.add(new Pending(phase, delay, areaInc, 1.0));
            }
        }
    }

    /** 每 tick 递减延迟，返回本 tick 到期的重放阶段实例。 */
    public List<SkillPhaseInstance> tick(SkillInstance si) {
        if (pending.isEmpty()) return List.of();
        List<SkillPhaseInstance> out = new ArrayList<>();
        List<Pending> carried = new ArrayList<>();
        for (Pending p : pending) {
            if (p.delay() <= 0) {
                out.addAll(p.phase().repeat(si, p.areaInc(), p.areaMore()));
            } else {
                carried.add(new Pending(p.phase(), p.delay() - 1, p.areaInc(), p.areaMore()));
            }
        }
        pending.clear();
        pending.addAll(carried);
        return out;
    }

    /** 是否仍有未到期的释放重放（供宿主判断技能是否应继续存活）。 */
    public boolean hasPending() {
        return !pending.isEmpty();
    }
}
