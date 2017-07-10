package fr.jgetmove.jgetmove.solver;

import fr.jgetmove.jgetmove.config.DefaultConfig;
import fr.jgetmove.jgetmove.database.Itemset;

import java.util.*;

public class BlockMerger {

    private int minSupport, maxPattern, minTime;

    /**
     * Initialise le solveur.
     */
    public BlockMerger(DefaultConfig config) {
        this.minSupport = config.getMinSupport();
        this.maxPattern = config.getMaxPattern();
        this.minTime = config.getMinTime();

    }

    /**
     * Creates a bitMatrix from the blocks
     *
     * @param blocks              the blocks containing the itemsets
     * @param transactionQuantity number of transacitons in the solver
     * @return an array containing a bitmatrix of (itemsets - transactions)
     */
    private static ArrayList<ArrayList<BitSet>> mapBitMatrix(ArrayList<ArrayList<Itemset>> blocks, int transactionQuantity) {
        ArrayList<ArrayList<BitSet>> blockMatrix = new ArrayList<>(blocks.size());

        // initializing blockMatrix
        for (ArrayList<Itemset> block : blocks) {

            ArrayList<BitSet> itemsetMatrix = new ArrayList<>(block.size());
            for (Itemset itemset : block) {
                BitSet bitSet = new BitSet(transactionQuantity);

                for (int transactionId : itemset.getTransactions()) {
                    bitSet.set(transactionId);
                }


                itemsetMatrix.add(bitSet);
            }

            blockMatrix.add(itemsetMatrix);
        }

        return blockMatrix;
    }

    public ArrayList<Itemset> fuse(ArrayList<ArrayList<Itemset>> blocks, int transactionQuantity) {
        ArrayList<Itemset> itemsets = new ArrayList<>();
        // setting a perf itemset-merged checker

        // ordering itemset in block for next optimized operations
        ArrayList<ArrayList<Boolean>> alreadyChecked = new ArrayList<>(blocks.size());
        for (ArrayList<Itemset> block : blocks) {
            block.sort((bigger, lesser) -> lesser.getTransactions().size() - bigger.getTransactions().size());
            alreadyChecked.add(new ArrayList<>(Collections.nCopies(block.size(), false)));

        }

        ArrayList<ArrayList<BitSet>> blockMatrix = mapBitMatrix(blocks, transactionQuantity);


        int itemsetId = 0;

        for (int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
            ArrayList<Itemset> block = blocks.get(blockIndex);
            for (int itemsetIndex = 0; itemsetIndex < block.size(); itemsetIndex++) {
                if (alreadyChecked.get(blockIndex).get(itemsetIndex)) {
                    continue;
                }

                // itemset iterate on other blocks
                BitSet itemsetBitSet = blockMatrix.get(blockIndex).get(itemsetIndex);

                Set<Integer> mergedClusters = new HashSet<>(block.get(itemsetIndex).getClusters());
                Set<Integer> mergedTimes = new HashSet<>(block.get(itemsetIndex).getTimes());
                Set<Integer> mergedTransactions = new HashSet<>(block.get(itemsetIndex).getTransactions());


                for (int mergeBlockIndex = 0; mergeBlockIndex < blocks.size(); mergeBlockIndex++) {

                    if (mergeBlockIndex == blockIndex) {
                        continue;
                    }
                    // foreach itemset in the other block
                    ArrayList<Itemset> mergeBlock = blocks.get(mergeBlockIndex);

                    for (int mergeItemsetIndex = 0; mergeItemsetIndex < mergeBlock.size(); mergeItemsetIndex++) {
                        if (mergeBlock.get(mergeItemsetIndex).getTransactions().size()
                                < block.get(itemsetIndex).getTransactions().size()) {
                            break;
                        } else if (alreadyChecked.get(mergeBlockIndex).get(mergeItemsetIndex)
                                && mergeBlock.get(mergeItemsetIndex).getTransactions().size() == block.get(itemsetIndex).getTransactions().size()) {
                            continue;
                        }


                        BitSet mergeItemsetBitSet = blockMatrix.get(mergeBlockIndex).get(mergeItemsetIndex);
                        int beforeAndSize = itemsetBitSet.cardinality();
                        BitSet clonedItemsetBitSet = (BitSet) itemsetBitSet.clone();
                        clonedItemsetBitSet.and(mergeItemsetBitSet);


                        if (beforeAndSize == clonedItemsetBitSet.cardinality()) {
                            if (beforeAndSize == mergeItemsetBitSet.cardinality()) {
                                alreadyChecked.get(mergeBlockIndex).set(mergeItemsetIndex, true);
                            }

                            mergedClusters.addAll(mergeBlock.get(mergeItemsetIndex).getClusters());
                            mergedTimes.addAll(mergeBlock.get(mergeItemsetIndex).getTimes());
                        }
                    }
                }

                if (mergedClusters.size() > minTime) {
                    itemsets.add(new Itemset(itemsetId, mergedTransactions, mergedClusters, mergedTimes));
                    ++itemsetId;
                }
            }
        }

        return itemsets;
    }
}
