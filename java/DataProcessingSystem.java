import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class DataProcessingSystem {

    // Shared task queue
    static class TaskQueue {
        private final Queue<Task> queue = new LinkedList<>();

        public synchronized void addTask(Task task) {
            queue.add(task);
            System.out.println("[QUEUE] Added " + task);
        }

        public synchronized Task getTask() {
            return queue.poll();
        }

        public synchronized boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    // Shared result writer
    static class ResultWriter {
        private final String filePath;
        private final ReentrantLock lock = new ReentrantLock();

        public ResultWriter(String filePath) {
            this.filePath = filePath;
        }

        public void writeResult(String result) throws IOException {
            lock.lock();

            try (FileWriter fileWriter = new FileWriter(filePath, true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                 PrintWriter writer = new PrintWriter(bufferedWriter)) {

                writer.println(result);

            } finally {
                lock.unlock();
            }
        }
    }

    // Worker implementation
    static class Worker implements Runnable {
        private final int workerId;
        private final TaskQueue taskQueue;
        private final ResultWriter resultWriter;

        public Worker(int workerId,
                      TaskQueue taskQueue,
                      ResultWriter resultWriter) {

            this.workerId = workerId;
            this.taskQueue = taskQueue;
            this.resultWriter = resultWriter;
        }

        @Override
        public void run() {

            System.out.println(
                    "[WORKER-" + workerId + "] Started."
            );

            while (true) {

                Task task = taskQueue.getTask();

                // Queue is empty and all available tasks have been retrieved.
                if (task == null) {
                    System.out.println(
                            "[WORKER-" + workerId + "] No more tasks. Stopping."
                    );
                    break;
                }

                try {

                    System.out.println(
                            "[WORKER-" + workerId + "] Processing "
                                    + task
                    );

                    // Simulate computational work.
                    Thread.sleep(300 + (workerId * 50L));

                    String result =
                            "Worker-" + workerId
                                    + " processed Task-" + task.getId()
                                    + " | Input: " + task.getData()
                                    + " | Result: "
                                    + task.getData().toUpperCase();

                    resultWriter.writeResult(result);

                    System.out.println(
                            "[WORKER-" + workerId + "] Completed Task-"
                                    + task.getId()
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    System.err.println(
                            "[ERROR] Worker-" + workerId
                                    + " was interrupted: "
                                    + e.getMessage()
                    );

                    break;

                } catch (IOException e) {

                    System.err.println(
                            "[ERROR] Worker-" + workerId
                                    + " encountered an I/O error: "
                                    + e.getMessage()
                    );
                }
            }

            System.out.println(
                    "[WORKER-" + workerId + "] Finished."
            );
        }
    }

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("     JAVA MULTI-THREADED DATA PROCESSING");
        System.out.println("==============================================");

        String outputFile = "data/results.txt";

        TaskQueue taskQueue = new TaskQueue();
        ResultWriter resultWriter = new ResultWriter(outputFile);

        // Create tasks.
        for (int i = 1; i <= 20; i++) {

            Task task = new Task(
                    i,
                    "data_record_" + i
            );

            taskQueue.addTask(task);
        }

        System.out.println();
        System.out.println("[SYSTEM] 20 tasks created.");
        System.out.println("[SYSTEM] Starting 4 worker threads...");
        System.out.println();

        // Create a fixed-size worker pool.
        ExecutorService executor =
                Executors.newFixedThreadPool(4);

        long startTime = System.currentTimeMillis();

        // Submit workers.
        for (int i = 1; i <= 4; i++) {

            executor.submit(
                    new Worker(
                            i,
                            taskQueue,
                            resultWriter
                    )
            );
        }

        // Stop accepting new tasks.
        executor.shutdown();

        try {

            // Wait for all workers to finish.
            if (!executor.awaitTermination(
                    60,
                    TimeUnit.SECONDS)) {

                System.err.println(
                        "[ERROR] Workers did not finish within "
                                + "the expected time."
                );

                executor.shutdownNow();
            }

        } catch (InterruptedException e) {

            System.err.println(
                    "[ERROR] Main thread interrupted: "
                            + e.getMessage()
            );

            executor.shutdownNow();

            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();

        System.out.println();
        System.out.println("==============================================");
        System.out.println("          PROCESSING COMPLETED");
        System.out.println("==============================================");

        System.out.println(
                "[SYSTEM] Remaining queue empty: "
                        + taskQueue.isEmpty()
        );

        System.out.println(
                "[SYSTEM] Results saved to: "
                        + outputFile
        );

        System.out.println(
                "[SYSTEM] Execution time: "
                        + (endTime - startTime)
                        + " ms"
        );

        System.out.println(
                "[SYSTEM] All workers terminated safely."
        );
    }
}
