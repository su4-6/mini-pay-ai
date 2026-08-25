public class StringIntegerPractice {
    public static void main(String[] args) {
        String name="小明";
        int score=95;
        Integer level=2;
        Integer vipLevel=null;
//int score = null;// 不允许
//int score = 0;             → 分数明确是 0
//Integer vipLevel = null;   → 等级暂时未知/未设置
        System.out.println(vipLevel);
        System.out.println(name);
        System.out.println(score);
        System.out.println(level);
    }
}
