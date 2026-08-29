import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolPractice {
    public static void main(String[] args) {
        ExecutorService pool= Executors.newFixedThreadPool(2);
        for (int i=1;i<=4;i++){
            int taskNo=i;
            pool.submit(()->{
                System.out.println(
                        Thread.currentThread().getName()
//                                +"正在处理支付任务"+taskNo
                                  +"开始处理支付任务"+taskNo
                );
                //新增
                try {
                    Thread.sleep(2000);
                }catch (InterruptedException exception){
                    Thread.currentThread().interrupt();
                    return;
                }
                System.out.println(
                        Thread.currentThread().getName()
                                +"完成支付任务"+taskNo
                );
                //新增

            });
        }
        pool.shutdown();
    }
}
