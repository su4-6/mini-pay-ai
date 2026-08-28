public class CounterRacePractice {
    private static int count=0;
    //新增
    private static final Object LOCK=new Object();
    private static final int TIMES=100_000;

    public static void main(String[] args)throws InterruptedException {
        Thread threadA=new Thread(()->increase(),"线程A");
        Thread threadB=new Thread(()->increase(),"线程B");

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();

        System.out.println("期望次数："+TIMES*2);
        System.out.println("实际次数"+count);
    }

//   private static  void increase(){
//        for (int i=0;i<TIMES;i++){
//            count++;
//        }
//    }
//期望次数：200000
//实际次数143129
    private static void increase(){
        for (int i=0;i<TIMES;i++){
            synchronized (LOCK){
                count++;
            }
        }
    }
}
