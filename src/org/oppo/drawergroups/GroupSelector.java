package org.oppo.drawergroups;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GroupSelector {
    public static <T> List<T> select(List<T> apps, List<String> names, String selected, int columns) {
        if (apps == null || names == null || apps.size() != names.size() || selected == null || columns <= 0) return null;
        List<String> keys = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (String name : names) {
            if (name == null || name.isEmpty()) continue;
            int i = keys.indexOf(name);
            if (i < 0) { keys.add(name); counts.add(1); }
            else counts.set(i, counts.get(i) + 1);
        }
        int center = keys.indexOf(selected);
        if (center < 0) return null;
        int left = Math.max(0, center - 1), right = Math.min(keys.size() - 1, center + 1);
        // Show only the current group and its nearest nonempty neighbor on each side.
        List<T> result = new ArrayList<>();
        for (int i = 0; i < apps.size(); i++) {
            int index = keys.indexOf(names.get(i));
            if (index >= left && index <= right && Objects.equals(names.get(i), keys.get(index))) result.add(apps.get(i));
        }
        return result;
    }
    private GroupSelector() {}
}
