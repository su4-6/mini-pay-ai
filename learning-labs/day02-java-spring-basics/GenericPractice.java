import java.util.ArrayList;
import java.util.List;

public class GenericPractice {
    public static void main(String[] args) {
        List<Integer>scores=new ArrayList<>();
        scores.add(95);
        scores.add(80);
//scores.add("小明");
//泛型的作用是：提前规定容器里能放什么类型，防止把“小明”错误放进分数列表。
//这里的 <Integer> 表示：scores 这个列表只能装整数。
        System.out.println(scores.get(0));
        System.out.println(scores.size());
//为什么写 Integer，不写 int？
//int 是基本整数。
//Integer 是整数的对象形式。
//泛型容器，例如 List<...>，要求填写对象类型，所以用 Integer。
    }
}
