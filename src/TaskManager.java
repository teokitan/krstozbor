import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager {

    private final BlockingQueue<Callable<Map<Position, Word>>> taskQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private final CompletionService<Map<Position, Word>> completionService = new ExecutorCompletionService<>(executorService);
    private volatile boolean solutionFound = false;
    private AtomicInteger solutionCount = new AtomicInteger(0);
    private final Semaphore semaphore = new Semaphore(8);
    private LocalDateTime startTime = null;


    public TaskManager() {
        new Thread(this::consumeTasks).start();
    }

    public void startResultsHandling() {
        startTime = LocalDateTime.now();
        new Thread(this::handleResults).start();
    }

    public void addTask(Callable<Map<Position, Word>> task) {
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private void consumeTasks() {
        try {
            while (Main.finalResenie.get() == null) {
                Callable<Map<Position, Word>> task = taskQueue.take();
                semaphore.acquire();
                solutionCount.getAndIncrement();
                completionService.submit(task);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private void handleResults() {
        try {
            while (Main.finalResenie.get() == null) {
                Future<Map<Position, Word>> future = completionService.take();
                Map<Position, Word> solution = future.get();
                semaphore.release();
                if (solution != null) {
                    solutionFound = true;
                    System.out.println("Checked: " + solutionCount.get());
                    System.out.println("Start: " + Main.startTime);
                    System.out.println("End: " + LocalDateTime.now());
                    Main.finalResenie.set(solution);
                    System.out.println("Solution found: " + solution);
                    if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
                        executorService.shutdownNow(); // Force shutdown if tasks don’t complete in time
                    }
                    break;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdownNow();
        }
    }

}


