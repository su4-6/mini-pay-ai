import java.util.ArrayList;
import java.util.List;

public class ArrayListPractice {
    public static void main(String[] args) {
        List<String>tasks=new ArrayList<>();
        tasks.add("登录");
        tasks.add("查看钱包");
        tasks.add("发起支付");
        tasks.add("申请退款");
        tasks.remove(1);
//        System.out.println(tasks.get(1));
//        System.out.println(tasks.get(2));
//        System.out.println(tasks.size());

//        for (String task:tasks){
//            System.out.println(task);
//            if (task.equals("登录")){
//                tasks.remove(task);
//            }
//for (String task : tasks) 循环里只读取；不要直接 add 或 remove 同一个列表。
//注意这里故意删除第一个元素“登录”，运行时很可能会出现：
//ConcurrentModificationException
//它的中文意思可以理解为：“我正在按顺序查看这个列表，但你中途把列表改了，我无法继续安全地走下去。”
//这是 fail-fast（快速失败）：Java 不让程序悄悄在可能错乱的状态下继续运行。

            tasks.removeIf(task -> task.equals("登录"));
            for (String task:tasks){
                System.out.println(task);
//removeIf 的意思是：“逐项判断，符合条件就删除。”
//这里删除“登录”后，再用 for 只负责打印。
//其中 task 代表“列表当前正在检查的某一项”；-> 后面写“什么情况应删除”。
//它不会删除后面的 for 循环；删除完成后，for 才开始打印剩余内容。
        }
    }
}
