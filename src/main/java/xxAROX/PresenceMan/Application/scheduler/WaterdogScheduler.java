package xxAROX.PresenceMan.Application.scheduler;

import io.netty.util.internal.PlatformDependent;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.utils.ThreadFactoryBuilder;
import xxAROX.PresenceMan.Application.utils.exception.SchedulerException;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WaterdogScheduler {
    private static WaterdogScheduler instance;
    private final ExecutorService threadedExecutor;

    private final Map<Integer, TaskHandler<?>> taskHandlerMap = new ConcurrentHashMap<>();
    private final Map<Integer, LinkedList<TaskHandler<?>>> assignedTasks = new ConcurrentHashMap<>();
    private final Queue<TaskHandler<?>> pendingTasks = PlatformDependent.newMpscQueue();

    private final AtomicInteger currentId = new AtomicInteger();

    public WaterdogScheduler() {
        if (instance != null) throw new RuntimeException("Scheduler was already initialized!");
        instance = this;

        ThreadFactoryBuilder builder = ThreadFactoryBuilder.builder().format("Scheduler Executor - #%d").build();
        this.threadedExecutor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), builder);
    }

    public static WaterdogScheduler getInstance() {
        return instance;
    }

    public <T extends Runnable> TaskHandler<T> scheduleAsync(T task) {
        return this.scheduleTask(task, true);
    }

    public <T extends Runnable> TaskHandler<T> scheduleTask(T task, boolean async) {
        return this.addTask(task, 0, 0, async);
    }

    public <T extends Runnable> TaskHandler<T> scheduleDelayed(T task, int delay) {
        return this.scheduleDelayed(task, delay, false);
    }

    public <T extends Runnable> TaskHandler<T> scheduleDelayed(T task, int delay, boolean async) {
        return this.addTask(task, delay, 0, async);
    }

    public <T extends Runnable> TaskHandler<T> scheduleRepeating(T task, int period) {
        return this.scheduleRepeating(task, period, false);
    }

    public <T extends Runnable> TaskHandler<T> scheduleRepeating(T task, int period, boolean async) {
        return this.addTask(task, 0, period, async);
    }

    public <T extends Runnable> TaskHandler<T> scheduleDelayedRepeating(T task, int delay, int period) {
        return this.scheduleDelayedRepeating(task, delay, period, false);
    }

    public <T extends Runnable> TaskHandler<T> scheduleDelayedRepeating(T task, int delay, int period, boolean async) {
        return this.addTask(task, delay, period, async);
    }

    public <T extends Runnable> TaskHandler<T> addTask(T task, int delay, int period, boolean async) {
        if (delay < 0 || period < 0) throw new SchedulerException("Attempted to register a task with negative delay or period!");
        int currentTick = getCurrentTick();
        int taskId = currentId.getAndIncrement();

        TaskHandler<T> handler = new TaskHandler<>(task, taskId, async);
        handler.setDelay(delay);
        handler.setPeriod(period);
        handler.setNextRunTick(handler.isDelayed() ? currentTick + delay : currentTick);

        pendingTasks.offer(handler);
        taskHandlerMap.put(taskId, handler);
        return handler;
    }

    public void onTick(int currentTick) {
        // 1. Assign all tasks to queue by nextRunTick
        TaskHandler<?> task;
        while ((task = pendingTasks.poll()) != null) {
            int tick = Math.max(currentTick, task.getNextRunTick());
            assignedTasks.computeIfAbsent(tick, integer -> new LinkedList<>()).add(task);
        }
        // 2. Run all tasks assigned to current tick
        LinkedList<TaskHandler<?>> queued = this.assignedTasks.remove(currentTick);
        if (queued == null) return;

        for (TaskHandler<?> taskHandler : queued) runTask(taskHandler, currentTick);
    }

    private void runTask(TaskHandler<?> taskHandler, int currentTick) {
        if (taskHandler.isCancelled()) {
            taskHandlerMap.remove(taskHandler.getTaskId());
            return;
        }
        if (taskHandler.isAsync()) threadedExecutor.execute(() -> taskHandler.onRun(currentTick));
        else taskHandler.onRun(currentTick);

        if (taskHandler.calculateNextTick(currentTick)) {
            pendingTasks.offer(taskHandler);
            return;
        }
        taskHandlerMap.remove(taskHandler.getTaskId()).cancel();
    }

    public void shutdown() {
        App.getInstance().getLogger().debug("Scheduler shutdown initialized!");
        this.threadedExecutor.shutdown();

        int count = 25;
        while (!threadedExecutor.isTerminated() && count-- > 0) {
            try {
                threadedExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    public ExecutorService getThreadedExecutor() {
        return this.threadedExecutor;
    }

    public int getCurrentTick() {
        return App.getInstance().getCurrentTick();
    }
}
