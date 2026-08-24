import java.util.HashMap;
import java.util.Map;

public class HashMapPractice2 {
    public static void main(String[] args) {
        Map<String,Integer> scores=new HashMap<>();
        scores.put("math",95);
        scores.put("english",80);
        Integer math= scores.get("math");
        System.out.println(math);
    }
}
