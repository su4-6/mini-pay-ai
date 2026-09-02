import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLocalPractice {
    private  static  final ThreadLocal<String>REQUEST_ID=new ThreadLocal<>();

    public static void main(String[] args)throws Exception {
        ExecutorService pool= Executors.newFixedThreadPool(1);

        pool.submit(()->{
            REQUEST_ID.set("req-A001");
//            System.out.println("请求A放入；"+REQUEST_ID.get());
            try {
                System.out.println("请求A放入；"+REQUEST_ID.get());
            }finally {
                REQUEST_ID.remove();
            }
        }).get();
        pool.submit(()->{
            System.out.println("请求B没有放入，却读到："+REQUEST_ID.get());
            REQUEST_ID.remove();
        }).get();
        pool.shutdown();
    }
}
