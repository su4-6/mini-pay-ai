public class MvcServiceFlowPractice {
    public static void main(String[] args) {
        PayRequest request=new PayRequest("小明",100);
        PayService service=new PayService();
        PayResult result=service.calculate(request);
        System.out.println(result.message());
    }
    record PayRequest(String userName,int amount){
    }
    record PayResult(String message){
    }
    static class PayService{
        PayResult calculate(PayRequest request){
            return new PayResult(
              request.userName()+"支付"+request.amount()+"元"
            );
        }
    }
}
