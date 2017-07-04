package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;
import fr.jgetmove.jgetmove.debug.PrettyPrint;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 */
public class ItemsetsOfBlock implements PrettyPrint {
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
        return Debug.indent(toPrettyString());
    }

    @Override
    public String toPrettyString() {
        return "\n. id : " + id +
                "\n`-- itemsets :" + itemsets;
    }

    public ArrayList<Itemset> getItemsetArrayList() {
        return itemsetArrayList;
    }
}
