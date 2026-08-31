import java.util.concurrent.locks.ReentrantLock;

public class TryLockPractice {
    private static final ReentrantLock user1Lock=new ReentrantLock();

    public static void main(String[] args) {
        Thread threadA=new Thread(
                ()->startAgentRun("user1"),
                "线程A"
        );
        Thread threadB=new Thread(
                ()->startAgentRun("user1"),
                "线程B"
        );
        threadA.start();
        threadB.start();
    }
    private static void startAgentRun(String userId){
//        if (!user1Lock.tryLock()){
//            System.out.println(
//                    Thread.currentThread().getName()
//                            +":用户"+userId+"的任务正在执行"
//            );
//            return;
//        }
//        try {
//            System.out.println(
//                    Thread.currentThread().getName()
//                            +"开始执行用户"+userId+"的任务"
//            );
//            Thread.sleep(2000);
//
//            System.out.println(
//                    Thread.currentThread().getName()
//                            +":用户"+userId+"的任务完成"
//            );
//        }catch (InterruptedException exception){
//            Thread.currentThread().interrupt();
//        }finally {
//            user1Lock.unlock();
//        }
        user1Lock.lock();
        try {
            System.out.println(
                    Thread.currentThread().getName()
                            +"开始执行用户"+userId+"的任务"
            );
            Thread.sleep(2000);
            System.out.println(
                    Thread.currentThread().getName()
                            +"完成用户"+userId+"的任务"
            );

        }catch (InterruptedException exception){
            Thread.currentThread().interrupt();
        }finally {
            user1Lock.unlock();
        }
    }
}
