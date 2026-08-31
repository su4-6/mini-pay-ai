import java.util.concurrent.Semaphore;

public class SemaphorePractice {
    private static final Semaphore MODEL_PERMITS=new Semaphore(2);

    public static void main(String[] args) throws InterruptedException {
        Thread task1=new Thread(()->callModel(1),"线程A");
        Thread task2=new Thread(()->callModel(2),"线程B");
        Thread task3=new Thread(()->callModel(3),"线程C");
        Thread task4=new Thread(()->callModel(4),"线程D");

        task1.start();
        task2.start();
        task3.start();
        task4.start();

        task1.join();
        task2.join();
        task3.join();
        task4.join();

        System.out.println("所有模型任务结束");

    }
    private static void callModel(int taskNo){
        boolean acquired=false;

        try {
            System.out.println(Thread.currentThread().getName()
                    +"：任务"+taskNo+"等待模型许可证");
//            MODEL_PERMITS.acquire();
//            acquired=true;

//          tryAcquire用法
            acquired=MODEL_PERMITS.tryAcquire();
            if (!acquired){
                System.out.println(Thread.currentThread().getName()
                        +":任务"+taskNo+"模型繁忙，不等待，直接结束");
                return;
            }
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName()
                    +":任务"+taskNo+"完成模型调用");
        }catch (InterruptedException exception){
            Thread.currentThread().interrupt();
        }finally {
            if (acquired){
                MODEL_PERMITS.release();
                System.out.println(Thread.currentThread().getName()
                        +":归还模型许可证");
            }
        }
    }
}
