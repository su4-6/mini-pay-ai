import java.util.HashMap;
import java.util.Map;

public class HashMapPractice {
    public static void main(String[] args) {
        Map<String,String>user=new HashMap<>();
        user.put("name","小明");
        user.put("city","上海");
        System.out.println(user.size());
        user.put("city","北京");
        System.out.println(user.size());
        String name= user.get("name");
        String city= user.get("city");
//        String city= user.get("country");
        System.out.println(name);
        System.out.println(city);
    }
}