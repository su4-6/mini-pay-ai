import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomThreadPoolPractice {
    public static void main(String[] args) {
        ThreadPoolExecutor pool=new ThreadPoolExecutor(
                2,
                4,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.AbortPolicy()

        );
        for (int i=1;i<=7;i++){
            int taskNo=i;

            try {
                pool.submit(()->{
                    System.out.println(
                            Thread.currentThread().getName()
                                    +"开始处理任务"+taskNo
                    );
                    try {
                        Thread.sleep(3000);
                    }catch (InterruptedException exception){
                        Thread.currentThread().interrupt();
                    }
                });
            }catch (Exception exception){
                System.out.println("任务"+taskNo+"被拒绝");
            }
        }
        pool.shutdown();
    }
}
