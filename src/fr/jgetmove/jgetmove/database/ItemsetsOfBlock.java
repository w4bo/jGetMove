package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 */
public class ItemsetsOfBlock {
    private final ArrayList<Itemset> itemsetArrayList;
    private final TreeSet<Itemset> itemsets;
    private int id;

    public ItemsetsOfBlock(int id, TreeSet<Itemset> itemsets) {
        this.id = id;
        this.itemsets = itemsets;
        this.itemsetArrayList = new ArrayList<>(itemsets);
    }

    public int getId() {
        return id;
    }

    public TreeSet<Itemset> getItemsets() {
        return itemsets;
    }

    @Override
    public String toString() {
        String str = "\n. id : " + id;
        str += "\n`-- itemsets :" + Debug.indent(itemsets.toString());
        return str;
    }

    public ArrayList<Itemset> getItemsetArrayList() {
        return itemsetArrayList;
    }
}
