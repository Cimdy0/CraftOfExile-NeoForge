package com.cimdy.coe.data.codec.stronghold.task.work;

import com.cimdy.coe.data.codec.stronghold.task.Task;

import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * 工作任务工厂契约：将「站点方块谓词」与「任务生成器」绑定为一个可注册单元。
 * <p>
 * 内建工种由 {@link WorkTaskRegistry} 显式注册；第三方模组可实现本接口并在
 * {@code META-INF/services/com.cimdy.coe.data.codec.stronghold.task.work.WorkTaskFactory}
 * 中声明，即可在不修改核心的前提下为「方块 → 工作任务」映射新增条目。
 */
public interface WorkTaskFactory {
    /** 匹配站点方块状态的谓词（通常与对应 {@link Site} 的方块谓词一致）。 */
    Predicate<BlockState> predicate();

    /** 在站点方块匹配时，依据服务器等级与站点生成对应的工作任务。 */
    BiFunction<ServerLevel, Site, Task> generator();
}
