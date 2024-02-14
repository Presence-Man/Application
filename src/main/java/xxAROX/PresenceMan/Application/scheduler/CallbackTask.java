/*
 * Copyright (c) 2024. By Jan-Michael Sohn also known as @xxAROX.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
