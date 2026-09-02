import java.util.concurrent.*;
import java.util.function.Function;

public class CountDownLatchFuturePractice {
    public static void main(String[] args) throws Exception {
        CountDownLatch latch=new CountDownLatch(3);
        ExecutorService pool= Executors.newFixedThreadPool(3);
        try {
            Future<String> userFuture=
                    pool.submit(()->query("用户信息",latch));

            Future<String>walletFuture=
                    pool.submit(()->query("钱包信息",latch));

            Future<String>paymentFuture=
                    pool.submit(()->query("支付方式",latch));

            boolean allFinished=latch.await(5,TimeUnit.SECONDS);

            if (!allFinished){
                System.out.println("查询超时，不能组装支付页");
                return;
            }

//            String user=userFuture.get();
//            String wallet=walletFuture.get();
//            String payment=paymentFuture.get();
//
//            System.out.println("开始组装支付页：");
//            System.out.println(user);
//            System.out.println(wallet);
//            System.out.println(payment);
//        }finally {
//            pool.shutdown();

        try {
            String user = userFuture.get();
            String wallet = walletFuture.get();
            String payment = paymentFuture.get();

            System.out.println("开始组装支付页：");
            System.out.println(user);
            System.out.println(wallet);
            System.out.println(payment);
        }catch (java.util.concurrent.ExecutionException exception){
            System.out.println(
                    "支付页数据不完整，不能组装："
                            +exception.getCause().getMessage()
            );
            return;
        }
        }finally {
            pool.shutdown();
        }

    }

    private static String query(String taskname,CountDownLatch latch){
        try {
            Thread.sleep(3000);

            //错误试验
            if(taskname.equals("钱包信息")){
                throw new RuntimeException("钱包服务暂时不可用");

            }
            //错误试验

            System.out.println(taskname+"查询完成");
            return taskname+"结果";
        }catch (InterruptedException exception){
            Thread.currentThread().interrupt();
            throw new RuntimeException(taskname+"被中断",exception);
        }finally {
            latch.countDown();
        }
    }
}
