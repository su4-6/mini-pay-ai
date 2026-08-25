public class ExceptionPractice {
    public static void main(String[] args) {
//      String input="一百";
        String input="100";
//Integer.parseInt(...) 只能把“看起来像整数的文本”转换成整数；“一百”是汉字文本，不能转换。
//      int amount=Integer.parseInt(input);
//      System.out.println(amount);
//Exception in thread "main" java.lang.NumberFormatException: For input string: "一百"

        try {
            int amount=Integer.parseInt(input);
            System.out.println("金额是："+amount);
//catch (Exception e)
//它能工作，但范围太大：它会接住很多不同类型的异常。这里我们已明确知道是“文本不能转整数”，所以改得更准确一些：
//关键不是“所有错误都 catch”，而是：预期可能失败的操作，要明确知道失败后怎么处理。
        } catch (NumberFormatException e) {
            System.out.println("请输入阿拉伯数字，例如100");
        }
    }
}
