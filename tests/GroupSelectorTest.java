import java.util.*;
import org.oppo.drawergroups.GroupSelector;

public class GroupSelectorTest {
    static void check(boolean ok, String name) { if (!ok) throw new AssertionError(name); }
    static List<String> groups(String... specs) {
        List<String> r = new ArrayList<>();
        for (String spec : specs) { String[] s = spec.split(":"); for (int i=0;i<Integer.parseInt(s[1]);i++) r.add(s[0]); }
        return r;
    }
    static List<String> run(List<String> groups, String selected, int cols) { return GroupSelector.select(groups, groups, selected, cols); }
    public static void main(String[] args) {
        List<String> a = groups("K:3","L:4","M:4","N:4","O:6");
        check(run(a,"M",4).equals(groups("L:4","M:4","N:4")), "limit to three groups");
        List<String> b = groups("K:2","L:10","M:10","N:10","O:2");
        check(run(b,"M",4).equals(groups("L:10","M:10","N:10")), "include both neighbors without truncation");
        List<String> c = groups("A:1","B:1","D:1","E:1","F:18","G:1");
        check(run(c,"A",4).equals(groups("A:1","B:1")), "edge and sparse letters");
        List<String> d = groups("L:2","M:2","N:2");
        check(run(d,"M",5).equals(d), "insufficient total");
        check(run(d,"X",5)==null && run(d,"M",0)==null, "fallback invalid inputs");
        List<String> e = groups("L:1","M:16","N:0","O:1");
        check(run(e,"M",4).equals(e), "skip empty adjacent groups");
        check(run(groups("A:1","B:1","C:1","D:1"),"C",5).equals(groups("B:1","C:1","D:1")), "do not expand to meet five rows");
        check(run(groups("A:1","B:1","C:1"),"C",5).equals(groups("B:1","C:1")), "last letter boundary");
        check(run(new ArrayList<String>(),"A",5)==null, "empty list fallback");
        System.out.println("9 selection tests passed");
    }
}
