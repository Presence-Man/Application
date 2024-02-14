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

package xxAROX.PresenceMan.Application.utils;

import lombok.Builder;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
public final class ThreadFactoryBuilder implements ThreadFactory {
    private static final ThreadFactory backingFactory = Executors.defaultThreadFactory();

    private final AtomicInteger count = new AtomicInteger(0);
    private final boolean daemon;
    private final String format;
    @Builder.Default
    private final int priority = Thread.currentThread().getPriority();
    private final Thread.UncaughtExceptionHandler exceptionHandler;

    private static String format(String format, int count) {
        return String.format(Locale.ROOT, format, count);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = backingFactory.newThread(runnable);
        if (format != null) thread.setName(format(format, count.getAndIncrement()));
        thread.setDaemon(daemon);
        thread.setPriority(priority);
        if (exceptionHandler != null) thread.setUncaughtExceptionHandler(exceptionHandler);
        return thread;
    }
}
