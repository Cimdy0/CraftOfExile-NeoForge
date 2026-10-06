package com.cimdy.coe.data.codec.stronghold.task.helper.dispatch;

import com.cimdy.coe.data.codec.stronghold.entity.CapacityHelper;
import com.cimdy.coe.data.codec.stronghold.task.Task;
import com.cimdy.coe.data.codec.stronghold.task.TaskStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class TaskAssigner {

    public record AssignmentResult(
            List<ActiveTask> updatedTasks,
            Map<UUID, List<ActiveTask>> entityAssignments
    ) {}

    // ---------- 公开分配策略 ----------
    public static AssignmentResult assignSingleChain(List<ActiveTask> tasks, List<LivingEntity> workers) {
        return processTasks(tasks, workers, true);
    }

    public static AssignmentResult assignMultiSupplyChain(List<ActiveTask> tasks, List<LivingEntity> workers) {
        return processTasks(tasks, workers, false);
    }

    public static AssignmentResult assignBalanced(List<ActiveTask> tasks, List<LivingEntity> workers) {
        List<ActiveTask> updatedTasks = new ArrayList<>();
        Map<UUID, List<ActiveTask>> entityAssignments = new HashMap<>();
        int[] totalLoad = new int[workers.size()];
        Set<UUID> assignedThisRound = new HashSet<>(); // 本批次已分配实体

        for (ActiveTask at : tasks) {
            if (at.status() != TaskStatus.PENDING) {
                updatedTasks.add(at);
                continue;
            }
            Task task = at.task();
            if (!task.isSplittable()) {
                assignSingleTask(at, workers, totalLoad, updatedTasks, entityAssignments, assignedThisRound);
                continue;
            }

            int needed = task.getVolume();
            int minTransfer = task.getMinTransfer();
            int remaining = needed;
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < workers.size(); i++) order.add(i);
            order.sort(Comparator.comparingInt(i -> totalLoad[i]));

            for (int idx : order) {
                if (remaining <= 0) break;
                LivingEntity w = workers.get(idx);
                // 跳过本轮已分配过任务的实体
                if (assignedThisRound.contains(w.getUUID())) continue;
                int cap = CapacityHelper.getRemainingCapacity(w) - totalLoad[idx];
                if (cap < minTransfer) continue;
                int take = Math.min(remaining, cap);
                if (remaining - take > 0 && remaining - take < minTransfer) {
                    if (remaining >= minTransfer * 2) take = remaining - minTransfer;
                    else continue;
                }
                Task subTask = task.split(take);
                ActiveTask sub = ActiveTask.of(subTask, TaskStatus.ASSIGNED);
                updatedTasks.add(sub);
                entityAssignments.computeIfAbsent(w.getUUID(), k -> new ArrayList<>()).add(sub);
                assignedThisRound.add(w.getUUID());
                totalLoad[idx] += take;
                remaining -= take;
            }
            if (remaining > 0) {
                updatedTasks.add(ActiveTask.of(task.split(remaining)));
            }
        }
        return new AssignmentResult(updatedTasks, entityAssignments);
    }

    public static AssignmentResult assignPriorityFirst(List<ActiveTask> tasks, List<LivingEntity> workers) {
        List<ActiveTask> updatedTasks = new ArrayList<>();
        Map<UUID, List<ActiveTask>> entityAssignments = new HashMap<>();
        int[] load = new int[workers.size()];
        Set<UUID> assignedThisRound = new HashSet<>();

        for (ActiveTask at : tasks) {
            if (at.status() != TaskStatus.PENDING) {
                updatedTasks.add(at);
                continue;
            }
            Task task = at.task();
            if (task.isSplittable()) {
                LivingEntity bestWorker = findBestWorker(task, workers, assignedThisRound);
                if (bestWorker != null) {
                    List<ActiveTask> parts = tryAssignToSingleWorker(at, bestWorker, entityAssignments, assignedThisRound);
                    updatedTasks.addAll(parts);
                    int index = workers.indexOf(bestWorker);
                    for (ActiveTask part : parts) {
                        if (part.status() == TaskStatus.ASSIGNED) {
                            load[index] += part.task().getVolume();
                        }
                    }
                } else {
                    updatedTasks.add(at);
                }
            } else {
                assignSingleTask(at, workers, load, updatedTasks, entityAssignments, assignedThisRound);
            }
        }
        return new AssignmentResult(updatedTasks, entityAssignments);
    }

    // ---------- 内部辅助 ----------
    private static void assignSingleTask(ActiveTask at, List<LivingEntity> workers, int[] load,
                                         List<ActiveTask> updatedTasks, Map<UUID, List<ActiveTask>> entityAssignments,
                                         Set<UUID> assignedThisRound) {
        Task task = at.task();
        LivingEntity best = findBestWorker(task, workers, assignedThisRound);
        if (best != null) {
            ActiveTask assigned = at.updateStatus(TaskStatus.ASSIGNED);
            updatedTasks.add(assigned);
            entityAssignments.computeIfAbsent(best.getUUID(), k -> new ArrayList<>()).add(assigned);
            assignedThisRound.add(best.getUUID());
            load[workers.indexOf(best)] += task.getVolume();
        } else {
            updatedTasks.add(at);
        }
    }

    private static LivingEntity findBestWorker(Task task, List<LivingEntity> workers, Set<UUID> exclude) {
        LivingEntity best = null;
        int bestDist = Integer.MAX_VALUE;
        for (LivingEntity w : workers) {
            if (exclude.contains(w.getUUID())) continue;
            if (!task.canBeAssignedTo(w)) continue;
            int dist = distanceToEntity(w, task);
            if (dist < bestDist) {
                bestDist = dist;
                best = w;
            }
        }
        return best;
    }

    private static int distanceToEntity(LivingEntity entity, Task task) {
        BlockPos target = task.getTargetPositionForEntity(entity);
        return Math.abs(entity.getBlockX() - target.getX()) +
                Math.abs(entity.getBlockY() - target.getY()) +
                Math.abs(entity.getBlockZ() - target.getZ());
    }

    private static List<ActiveTask> tryAssignToSingleWorker(
            ActiveTask at, LivingEntity worker, Map<UUID, List<ActiveTask>> entityAssignments,
            Set<UUID> assignedThisRound) {
        if (worker == null || assignedThisRound.contains(worker.getUUID())) return List.of(at);
        Task task = at.task();
        int needed = task.getVolume();
        int minTransfer = task.getMinTransfer();
        int cap = CapacityHelper.getRemainingCapacity(worker);
        List<ActiveTask> parts = new ArrayList<>();

        while (needed > 0 && cap >= minTransfer) {
            int batch = Math.min(needed, cap);
            if (batch < minTransfer) break;
            Task subTask = task.split(batch);
            ActiveTask sub = ActiveTask.of(subTask, TaskStatus.ASSIGNED);
            parts.add(sub);
            entityAssignments.computeIfAbsent(worker.getUUID(), k -> new ArrayList<>()).add(sub);
            assignedThisRound.add(worker.getUUID());
            needed -= batch;
            cap -= batch;
        }
        if (needed > 0) {
            parts.add(ActiveTask.of(task.split(needed)));
        }
        if (parts.isEmpty()) {
            parts.add(at);
        }
        return parts;
    }

    private static List<ActiveTask> tryAssignToMultipleWorkers(
            ActiveTask at, List<LivingEntity> workers, Map<UUID, List<ActiveTask>> entityAssignments,
            Set<UUID> assignedThisRound) {
        Task task = at.task();
        int needed = task.getVolume();
        int minTransfer = task.getMinTransfer();
        List<ActiveTask> parts = new ArrayList<>();
        if (workers.isEmpty()) {
            parts.add(at);
            return parts;
        }

        int[] caps = workers.stream().mapToInt(CapacityHelper::getRemainingCapacity).toArray();

        for (int i = 0; i < workers.size() && needed > 0; i++) {
            LivingEntity w = workers.get(i);
            if (assignedThisRound.contains(w.getUUID())) continue;
            int cap = caps[i];
            if (cap < minTransfer) continue;
            int take = Math.min(needed, cap);
            if (needed - take > 0 && needed - take < minTransfer) {
                if (needed >= minTransfer * 2) take = needed - minTransfer;
                else continue;
            }
            Task subTask = task.split(take);
            ActiveTask sub = ActiveTask.of(subTask, TaskStatus.ASSIGNED);
            parts.add(sub);
            entityAssignments.computeIfAbsent(w.getUUID(), k -> new ArrayList<>()).add(sub);
            assignedThisRound.add(w.getUUID());
            needed -= take;
            caps[i] -= take;
        }

        if (needed > 0) {
            parts.add(ActiveTask.of(task.split(needed)));
        }
        if (parts.isEmpty()) {
            parts.add(at);
        }
        return parts;
    }

    private static AssignmentResult processTasks(List<ActiveTask> tasks, List<LivingEntity> workers, boolean singleWorker) {
        List<ActiveTask> updatedTasks = new ArrayList<>();
        Map<UUID, List<ActiveTask>> entityAssignments = new HashMap<>();
        int[] load = new int[workers.size()];
        Set<UUID> assignedThisRound = new HashSet<>();

        for (ActiveTask at : tasks) {
            if (at.status() != TaskStatus.PENDING) {
                updatedTasks.add(at);
                continue;
            }
            Task task = at.task();
            if (singleWorker) {
                LivingEntity best = findBestWorker(task, workers, assignedThisRound);
                if (best != null) {
                    List<ActiveTask> parts = tryAssignToSingleWorker(at, best, entityAssignments, assignedThisRound);
                    updatedTasks.addAll(parts);
                    int index = workers.indexOf(best);
                    for (ActiveTask part : parts) {
                        if (part.status() == TaskStatus.ASSIGNED) {
                            load[index] += part.task().getVolume();
                        }
                    }
                } else {
                    updatedTasks.add(at);
                }
            } else {
                if (task.isSplittable()) {
                    List<ActiveTask> parts = tryAssignToMultipleWorkers(at, workers, entityAssignments, assignedThisRound);
                    updatedTasks.addAll(parts);
                } else {
                    assignSingleTask(at, workers, load, updatedTasks, entityAssignments, assignedThisRound);
                }
            }
        }
        return new AssignmentResult(updatedTasks, entityAssignments);
    }
}