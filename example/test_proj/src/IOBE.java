import java.util.*;

public class IOBE{
    public static void main(String[] args) {
        new IOBE().trigger_null_pointer_exp();
    }

    public void trigger_null_pointer_exp() {
        List<Integer> res = new ArrayList<>();
        add_elems(res, 5);
        rm_elems(res);
        int elem =find_elem_by_idx(res, 1);
        System.out.println(elem);
    }

    public void add_elems(List<Integer> res, int range) {
        for(int i=0; i<range; i++) {
            res.add(i);
        }
    }

    public void rm_elems(List<Integer> res) {
        res.clear();
    }

    public int find_elem_by_idx(List<Integer> res, int idx) {
        return res.get(idx); // BUG: trigger the IndexOutOfBoundsException here
    }
}
