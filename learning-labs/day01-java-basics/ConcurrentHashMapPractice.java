import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapPractice {
    public static void main(String[] args) {
        Map<String,Integer>onlineUsers=new ConcurrentHashMap<>();
        onlineUsers.put("user1",1);
        onlineUsers.put("user2",1);
//        Integer user1= onlineUsers.get("user1");
//        Integer user2= onlineUsers.get("user2");
//      oldValue 的含义是“放入之前已有的旧值”。
//      user3 原先不存在，没有旧值，所以是 null；随后它才被成功放入为 user3 → 2。
        Integer oldValue= onlineUsers.putIfAbsent("user3",2);
        System.out.println(oldValue);
        System.out.println(onlineUsers.get("user3"));
        System.out.println(onlineUsers.size());
    }
}
