import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapPractice2 {

    // 多线程共享的一张“在线用户表”。
    private static final Map<String, String> onlineUsers = new ConcurrentHashMap<>();

    // 多线程共享的一张“用户会话表”。
    private static final Map<String, String> sessions = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        Thread threadA = new Thread(
                () -> handleLogin("user1"),
                "线程A");

        Thread threadB = new Thread(
                () -> handleLogin("user1"),
                "线程B");

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();

        System.out.println("最终在线用户：" + onlineUsers);
        System.out.println("最终用户会话：" + sessions);
    }

    private static void handleLogin(String userId) {
        // key 不存在才放入；返回 null 代表当前线程首次放入成功。
        String oldValue = onlineUsers.putIfAbsent(userId, "ONLINE");

//        if (oldValue == null) {
//            System.out.println(
//                    Thread.currentThread().getName()
//                            + "：首次登录，成功标记在线");
//
//            // 如果 userId 没有会话，则创建会话并放入 Map。
//            String session = sessions.computeIfAbsent(
//                    userId,
//                    id -> "session-for-" + id);
//
//            System.out.println(
//                    Thread.currentThread().getName()
//                            + "：创建会话 " + session);
//        } else {
//            System.out.println(
//                    Thread.currentThread().getName()
//                            + "：用户已经在线，不重复创建");
//        }

        if (oldValue==null){
            System.out.println(Thread.currentThread().getName()
                                        +"：首次登录，成功标记在线");
        }else{
            System.out.println(Thread.currentThread().getName()
                                        +"：用户已经在线，不重复标记");
        }
        String session=sessions.computeIfAbsent(
                userId,
                id->{
                    System.out.println(
                            Thread.currentThread().getName()
                                    +"：真正创建会话");
                    return "session-for-"+id;
                }
        );
        System.out.println(
                Thread.currentThread().getName()
                        +"：得到会话 " + session);
        }
}