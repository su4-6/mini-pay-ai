import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolPractice {
    // 输入是四个支付任务，输出用于观察两条工作线程、排队任务和协作中断。
    public static void main(String[] args) {
        ExecutorService pool= Executors.newFixedThreadPool(2);
        // submit 只提交任务并立即返回，因此 main 线程会继续执行下面的关闭逻辑。
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
                    // shutdownNow 只发出中断请求；任务在这里响应中断并主动返回。
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
//        pool.shutdown();
        //新增
        try {
            Thread.sleep(500);
        }catch (InterruptedException exception){
            Thread.currentThread().interrupt();
        }
        // 运行中的任务必须配合中断；仍在队列里的任务可能不会开始。
        pool.shutdownNow();
        //新增
    }
}
