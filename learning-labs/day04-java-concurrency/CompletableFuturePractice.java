import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CompletableFuturePractice {
    public static void main(String[] args) {
        CompletableFuture<String>userFuture=
                CompletableFuture.supplyAsync(()->query("用户信息"));

        CompletableFuture<String> walletFuture =
                CompletableFuture.supplyAsync(() -> query("钱包信息"));

        CompletableFuture<String> paymentFuture =
                CompletableFuture.supplyAsync(() -> query("支付方式"));

        CompletableFuture<String> paymentPageFuture=
                userFuture.thenCombine(walletFuture,(user,wallet)->
                        user+"+"+wallet)
                        .thenCombine(paymentFuture,(base,payment)->
                        "支付页："+base+"+"+payment);

        //新增
        CompletableFuture<String>recommendationFuture=
                CompletableFuture.supplyAsync(()->query("推荐商品"))
                        .exceptionally(exception->{
                            System.out.println("推荐查询失败，使用降级结果");
                            return "推荐暂不可用";
                        });

        CompletableFuture<String>finalPageFuture=
                paymentPageFuture.thenCombine(recommendationFuture,(paymentPage,recommendation)->
                        paymentPage+":"+recommendation);
        //新增
        try {
//            String page=paymentPageFuture.get(5, TimeUnit.SECONDS);
            String page=finalPageFuture.get(5,TimeUnit.SECONDS);
            System.out.println(page);
        }catch (Exception exception){
            System.out.println("支付页组装失败"+exception.getCause());
        }

    }
    private  static String query(String taskName){
        try {
//            //新增
//            if (taskName.equals("钱包信息")) {
//                throw new RuntimeException("钱包服务暂时不可用");
//            }
//            //新增
            //新增
            if (taskName.equals("推荐商品")){
                throw new RuntimeException("推荐服务暂时不可用");
            }
            //新增
           Thread.sleep(1000);
            System.out.println(taskName+"查询完成");
            return taskName+"结果";
        }catch (InterruptedException exception){
            Thread.currentThread().interrupt();
            throw new RuntimeException(taskName+"被中断",exception);
        }
    }
}
