import java.util.*;
import org.oppo.drawergroups.RowPlan;
public class RowPlanTest {
    public static void main(String[] args) {
        List<String> n=Arrays.asList("L","L","M","M","M","M","M","M","N");
        List<RowPlan.Row> r=RowPlan.build(n,5);
        if(r.size()!=4 || r.get(0).start!=0 || r.get(0).end!=2 || r.get(1).start!=2 || r.get(1).end!=7 || r.get(2).end!=8 || r.get(2).label!=null || r.get(3).start!=8 || !"N".equals(r.get(3).label)) throw new AssertionError();
        System.out.println("grouped row mapping test passed");
    }
}
