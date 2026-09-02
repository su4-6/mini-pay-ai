import java.util.concurrent.*;

public class CountDownLatchPractice {
    public static void main(String[] args) throws InterruptedException{
        CountDownLatch latch=new CountDownLatch(3);
        ExecutorService pool= Executors.newFixedThreadPool(3);
        pool.submit(()->query("用户信息",latch));
        pool.submit(()->query("钱包展示信息",latch));
        pool.submit(()->query("支付方式",latch));

//        System.out.println("主线程：等待三个查询完成");
//        latch.await();
//
//        System.out.println("主线程：三个查询都完成，开始组装支付页");
//        pool.shutdown();

        //超时等待
        boolean allFinished=latch.await(2, TimeUnit.SECONDS);
        if (!allFinished){
            System.out.println("主线程：查询超时，不能组装完整支付页");
            pool.shutdown();
            return;
        }
        System.out.println("主线程：三个查询都完成，开始组装支付页");
    }
    private static  void query(String taskName,CountDownLatch latch){
        try {
            Thread.sleep(1000);
            System.out.println(
                    Thread.currentThread().getName()
                            +":完成查询"+taskName
            );
        }catch (InterruptedException exception){
            Thread.currentThread().interrupt();
            return;
        }finally {
            latch.countDown();
        }
    }
}
