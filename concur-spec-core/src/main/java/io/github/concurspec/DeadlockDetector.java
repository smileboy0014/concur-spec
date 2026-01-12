package io.github.concurspec;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Optional;

/**
 * Deadlock detector using ThreadMXBean.
 * Note: This checks *current JVM state*. It doesn't guarantee that a deadlock
 * did not occur transiently during the run unless you poll periodically.
 */
public final class DeadlockDetector {

    private DeadlockDetector() {
    }

    public static Optional<DeadlockSnapshot> findDeadlock() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();

        long[] ids = bean.findDeadlockedThreads();
        if (ids == null || ids.length == 0) return Optional.empty();

        ThreadInfo[] infos = bean.getThreadInfo(ids, true, true);
        return Optional.of(new DeadlockSnapshot(ids, infos));
    }

    public record DeadlockSnapshot(long[] threadIds, ThreadInfo[] threadInfos) {
        public String toPrettyString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Deadlock detected: threadIds=").append(Arrays.toString(threadIds)).append("\n");
            for (ThreadInfo ti : threadInfos) {
                if (ti == null) continue;
                sb.append("\"").append(ti.getThreadName()).append("\" ")
                        .append(" id=").append(ti.getThreadId()).append(" state=").append(ti.getThreadState()).append("\n")
                        .append(ti.toString()).append("\n");
            }
            return sb.toString();
        }
    }
}
