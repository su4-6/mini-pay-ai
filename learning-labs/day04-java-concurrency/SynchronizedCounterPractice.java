public class SynchronizedCounterPractice {
    private static  int count=0;
    private static final int TIMES=100_000;

    public static void main(String[] args) throws InterruptedException {
        Thread threadA=new Thread(
                SynchronizedCounterPractice::increase,
                "线程A"
        );

        Thread threadB=new Thread(
                SynchronizedCounterPractice::increase,
                "线程B"
        );
        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();
        System.out.println("期望次数"+TIMES*2);
        System.out.println("实际次数"+count);

    }
    private static synchronized void increase(){
        for (int i=0;i<TIMES;i++){
            count++;
        }
    }
}
