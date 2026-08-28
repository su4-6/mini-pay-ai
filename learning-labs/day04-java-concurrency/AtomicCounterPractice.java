import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounterPractice {
    private static final AtomicInteger count=new AtomicInteger(0);
    private static final int TIMES=100_00;

    public static void main(String[] args) throws InterruptedException {
        Thread threadA=new Thread(()->increase(),"线程A");
        Thread threadB=new Thread(()->increase(),"线程B");

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();

        System.out.println("期望次数："+TIMES*2);
        System.out.println("实际次数"+count.get());
    }
    private static void increase(){
        for (int i=0;i<TIMES;i++){
            count.incrementAndGet();
        }
    }
}
