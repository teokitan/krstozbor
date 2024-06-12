import javax.swing.*;
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
    private LocalDateTime startTime;


    public TaskManager() {
        new Thread(this::consumeTasks).start();
    }

    public void startResutlsHandling() {
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
                System.out.println("Otvoreni: " + solutionCount.incrementAndGet());
                System.out.println("OD QUEUE");
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
                System.out.println("KRAJ");
                System.out.println(taskQueue.size());
                if (solution != null) {
                    solutionFound = true;
                    System.out.println("Vreme: " + Duration.between(startTime, LocalDateTime.now()).toMillis());
                    System.out.println("Start: " + Main.startTime);
                    System.out.println("Kraj: " + LocalDateTime.now());
                    Main.finalResenie.set(solution);
                    System.out.println("Solution found: " + solution);
                    executorService.shutdownNow();
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


