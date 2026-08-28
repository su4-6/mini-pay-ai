import java.util.HashMap;
import java.util.Map;

public class HashMapCollisionPractice {
    static class UserKey{
        private final String name;
        UserKey(String name){
            this.name=name;
        }
        @Override
        public int hashCode(){
            return 1;
        }
        @Override
        public boolean equals(Object obj){
            if(this==obj){
                return true;
            }
            if (!(obj instanceof UserKey other)){
                return false;
            }
            return name.equals(other.name);
        }
    }

    public static void main(String[] args) {
        Map<UserKey,Integer>scores=new HashMap<>();
        scores.put(new UserKey("小明"),95);
        scores.put(new UserKey("小明"),100);
        scores.put(new UserKey("小红"),88);

        System.out.println(scores.size());
        System.out.println(scores.get(new UserKey("小红")));
        System.out.println(scores.get(new UserKey("小明")));
    }
}

