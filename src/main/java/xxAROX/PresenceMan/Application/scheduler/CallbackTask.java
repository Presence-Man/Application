package xxAROX.PresenceMan.Application.scheduler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Simple implementation of task which allows calling own callbacks with provided result after completition.
 * This task is especially handy when it comes to delayed tasks because
 * CompletableFuture is not providing us this kind of API at the moment.
 * @param <T> expected result
 */
public class CallbackTask<T> extends Task {
    private final Supplier<T> task;
    private List<Consumer<T>> callbacks;

    public CallbackTask(Supplier<T> task) {
        this.task = task;
    }

    public CallbackTask(Supplier<T> task, Consumer<T> callback) {
        this.task = task;
        addCallback(callback);
    }

    @Override
    public void onRun(int currentTick) {
        complete(this.task.get());
    }

    @Override
    public void onCancel() {
        complete(null);
    }

    private void complete(T result) {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (Consumer<T> callback : callbacks) callback.accept(result);
        }
    }

    public void addCallback(Consumer<T> callback) {
        if (callbacks == null) callbacks = Collections.synchronizedList(new ObjectArrayList<>());
        callbacks.add(callback);
    }
}
