package io.github.concurspec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

public class DeadlockDetectorTest {

    @Test
    @DisplayName("데드락이 발생하면 DeadlockDetector가 이를 감지한다")
    void shouldDetectDeadlock() throws InterruptedException {
        Object lockA = new Object();
        Object lockB = new Object();
        CountDownLatch ready = new CountDownLatch(2);

        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                ready.countDown();
                sleepSilently(50);
                synchronized (lockB) {
                    // never reached
                }
            }
        }, "deadlock-t1");

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                ready.countDown();
                sleepSilently(50);
                synchronized (lockA) {
                    // never reached
                }
            }
        }, "deadlock-t2");

        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();

        ready.await();
        // give time to deadlock
        sleepSilently(200);

        Optional<DeadlockDetector.DeadlockSnapshot> result = DeadlockDetector.findDeadlock();
        assertThat(result)
                .as("deadlock should be detected")
                .isPresent();
    }

    private void sleepSilently(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
