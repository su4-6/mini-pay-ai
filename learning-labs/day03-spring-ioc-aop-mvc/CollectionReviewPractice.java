import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class CollectionReviewPractice {
    public static void main(String[] args) {
        // LinkedList 仍是“有顺序”的 List：下标 1 对应第二个步骤“支付”。
        // 本练习只比较集合特点；它不代表 MiniPay 用 LinkedList 保存订单或资金真相。
        LinkedList<String>steps=new LinkedList<>();
        steps.add("登录");
        steps.add("支付");
        steps.add("退款");

        // HashSet 只保留不重复值：第二次加入 Pay 不会产生第二份权限。
        // 它适合单 JVM 临时去重，不能代替消息 Inbox 或数据库唯一约束。
        Set<String>permissions=new HashSet<>();
        permissions.add("Pay");
        permissions.add("REFUND");
        permissions.add("Pay");

        // 预期输出：支付、2。
        System.out.println(steps.get(1));
        System.out.println(permissions.size());
    }
}
