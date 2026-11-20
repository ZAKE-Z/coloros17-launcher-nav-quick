package org.oppo.drawergroups;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public final class RowPlan {
    public static final class Row {
        public final int start, end, group;
        public final String label;
        Row(int start, int end, int group, String label) { this.start=start; this.end=end; this.group=group; this.label=label; }
    }
    public static List<Row> build(List<String> names, int columns) {
        if (columns <= 0) throw new IllegalArgumentException();
        List<Row> rows = new ArrayList<>();
        int start=0, group=0;
        while(start<names.size()) {
            String label=names.get(start);
            if(label==null || label.isEmpty()) throw new IllegalArgumentException();
            int end=start+1;
            while(end<names.size() && Objects.equals(label,names.get(end))) end++;
            for(int p=start;p<end;p+=columns) rows.add(new Row(p,Math.min(p+columns,end),group,p==start?label:null));
            start=end; group++;
        }
        return rows;
    }
    private RowPlan() {}
}
