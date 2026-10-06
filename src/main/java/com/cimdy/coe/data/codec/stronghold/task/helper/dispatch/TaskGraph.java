package com.cimdy.coe.data.codec.stronghold.task.helper.dispatch;

import com.cimdy.coe.data.codec.stronghold.task.helper.recipe.RecipeCacheManager;
import com.cimdy.coe.data.codec.stronghold.task.Task;

import java.util.*;

/**
 * 任务依赖图（方案 C，DAG 抽象层）。
 * <p>
 * 原任务链是线性链表（{@code Optional<Task> next}），只能串行表达「采木 → 合成」。
 * 本类以 {@link Task#getNextTasks()} 为边构建可达图，支持并行分支与汇合：
 * 一个任务可拥有多个后继，全部后继就绪后才视为整图完成。
 * <p>
 * 现有序列化 / RecipeCacheManager 的 {@code setNext} 逻辑完全不变；新增分支任务只需
 * 重写 {@code getNextTasks()} 返回多个后继即可接入 DAG，无需改动调度核心。
 */
public final class TaskGraph {
    private TaskGraph() {
    }

    /**
     * 从 head 出发，沿 {@link Task#getNextTasks()} 收集所有可达任务（去重，保持首次访问顺序）。
     */
    public static List<Task> collectAll(Task head) {
        List<Task> result = new ArrayList<>();
        Set<Task> visited = new HashSet<>();
        Deque<Task> stack = new ArrayDeque<>();
        if (head != null) stack.push(head);
        while (!stack.isEmpty()) {
            Task t = stack.pop();
            if (!visited.add(t)) continue;
            result.add(t);
            for (Task next : t.getNextTasks()) {
                if (next != null) stack.push(next);
            }
        }
        return result;
    }

}
