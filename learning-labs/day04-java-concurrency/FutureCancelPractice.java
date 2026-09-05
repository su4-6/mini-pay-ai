import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureCancelPractice {
    // 输入是四个独立任务，输出用于证明 Future 句柄只控制它代表的那一个任务。
    public static void main(String[] args) {
        ExecutorService pool= Executors.newFixedThreadPool(2);
        try {
            Future<?>task1= pool.submit(()->runTask(1));
            Future<?>task2= pool.submit(()->runTask(2));
            // 保留任务 3 的句柄，用它取消单个排队任务，而不是关闭整个线程池。
            Future<?>task3= pool.submit(()->runTask(3));
            Future<?>task4= pool.submit(()->runTask(4));

            // false 表示：如果任务已经开始，不请求中断它。
            boolean cancelled=task3.cancel(false);
            System.out.println("任务3取消结果："+cancelled);
        }finally {
            pool.shutdown();
        }
    }
    private static  void runTask(int taskNo){
        System.out.println("开始任务"+taskNo);
        try {
            Thread.sleep(1000);
        }catch (InterruptedException exception){
            Thread.currentThread().interrupt();
            return;
        }
        System.out.println("完成任务"+taskNo);
    }
}
