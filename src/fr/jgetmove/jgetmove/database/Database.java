package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.*;

/**
 * Contient toutes les structures de données
 */
public class Database {
    private Input inputObj, inputTime;


    private HashMap<Integer, Cluster> clusters;
    private HashMap<Integer, Transaction> transactions;
    private HashMap<Integer, Time> times;
    private HashMap<Integer, Block> blocks;

    private TreeSet<Integer> clusterIdsTree;
    private TreeSet<Integer> transactionIdsTree;
    private TreeSet<Integer> timeIdsTree;
    private TreeSet<Integer> blockIdsTree;

    /**
     * Initialise Database en fonction des fichiers de données
     *
     * @param inputObj  fichier (transactionId [clusterId ...])
     * @param inputTime fichier (timeId clusterId)
     * @throws IOException              si inputObj ou inputTime est incorrect
     * @throws ClusterNotExistException si le cluster n'existe pas
     */
    public Database(Input inputObj, Input inputTime, int blockSize) throws IOException, ClusterNotExistException, MalformedTimeIndexException {
        this.inputObj = inputObj;
        this.inputTime = inputTime;

        blocks = new HashMap<>();
        clusters = new HashMap<>();
        transactions = new HashMap<>();
        times = new HashMap<>();

        blockIdsTree = new TreeSet<>();
        clusterIdsTree = new TreeSet<>();
        transactionIdsTree = new TreeSet<>();
        timeIdsTree = new TreeSet<>();

        //Initialisation des clusters et transactions
        initClusterAndTransaction();
        //Initialisation des temps
        initTimeAndCluster(blockSize);
    }

    @Deprecated
    public Database(Database database) {
        // TODO : copying database :) with blocks
        this.inputObj = null;
        this.inputTime = null;

        blocks = new HashMap<>();
        clusters = new HashMap<>();
        transactions = new HashMap<>();
        times = new HashMap<>();

        blockIdsTree = new TreeSet<>();
        clusterIdsTree = new TreeSet<>();
        transactionIdsTree = new TreeSet<>();
        timeIdsTree = new TreeSet<>();

        for (Transaction originalTransaction : database.getTransactions().values()) {
            Transaction transaction = getOrCreateTransaction(originalTransaction.getId());

            for (Cluster originalCluster : originalTransaction.getClusters().values()) {
                Cluster cluster = getOrCreateCluster(originalCluster.getId());

                transaction.add(cluster);
                cluster.add(transaction);
            }
        }

        for (Cluster originalCluster : database.getClusters().values()) {
            Cluster cluster = getOrCreateCluster(originalCluster.getId());

            for (Transaction originalTransaction : originalCluster.getTransactions().values()) {
                Transaction transaction = getOrCreateTransaction(originalTransaction.getId());

                transaction.add(cluster);
                cluster.add(transaction);
            }
        }

        for (Cluster originalCluster : database.getClusters().values()) {
            Cluster cluster = this.getCluster(originalCluster.getId());
            Time time = this.getTime(originalCluster.getTimeId());

            if (time == null) {
                time = new Time(originalCluster.getTimeId());
                this.add(time);
            }

            time.add(cluster);
            cluster.setTime(time);
        }
    }

    public Database(Collection<Transaction> transactions) {
        this.inputObj = null;
        this.inputTime = null;

        blocks = new HashMap<>();
        clusters = new HashMap<>();
        this.transactions = new HashMap<>();
        times = new HashMap<>();

        blockIdsTree = new TreeSet<>();
        clusterIdsTree = new TreeSet<>();
        transactionIdsTree = new TreeSet<>();
        timeIdsTree = new TreeSet<>();

        for (Transaction oldTransaction : transactions) {
            Transaction transaction = new Transaction(oldTransaction.getId());
            add(transaction);
            for (Cluster oldCluster : oldTransaction.getClusters().values()) {
                Cluster cluster = this.getCluster(oldCluster.getId());

                if (cluster == null) {
                    cluster = new Cluster(oldCluster.getId());
                    add(cluster);
                }

                cluster.add(transaction);
                transaction.add(cluster);
            }
        }
    }

    public Database() {
        this.inputObj = null;
        this.inputTime = null;

        blocks = new HashMap<>();
        clusters = new HashMap<>();
        transactions = new HashMap<>();
        times = new HashMap<>();

        blockIdsTree = new TreeSet<>();
        clusterIdsTree = new TreeSet<>();
        transactionIdsTree = new TreeSet<>();
        timeIdsTree = new TreeSet<>();
    }

    /**
     * @param transactionId l'id de la transaction à récuperer
     * @return La transaction crée ou récuperée
     */
    private Transaction getOrCreateTransaction(int transactionId) {
        Transaction transaction = this.getTransaction(transactionId);

        if (transaction == null) {
            transaction = new Transaction(transactionId);
            this.add(transaction);
        }
        return transaction;
    }

    private Cluster getOrCreateCluster(int clusterId) {
        Cluster cluster = this.getCluster(clusterId);

        if (cluster == null) {
            cluster = new Cluster(clusterId);
            this.add(cluster);
        }

        return cluster;
    }

    /**
     * Initialise les HashMap de clusters et transactions
     *
     * @throws IOException si le fichier est incorrect
     * @see Database#Database(Input, Input, int)
     */
    private void initClusterAndTransaction() throws IOException {
        String line;
        int transactionId = 0;
        while ((line = inputObj.readLine()) != null) {
            String[] lineClusterId = line.split("[ \\t]+");
            Transaction transaction = new Transaction(transactionId);

            for (String strClusterId : lineClusterId) {
                int clusterId = Integer.parseInt(strClusterId);
                Cluster cluster = getOrCreateCluster(clusterId);

                cluster.add(transaction);
                transaction.add(cluster);
            }

            this.add(transaction);

            transactionId++;
        }
    }

    /**
     * Initialise la liste des temps ainsi que les clusters associés
     *
     * @throws IOException              si le fichier inputTime est inccorect
     * @throws ClusterNotExistException si le cluster n'existe pas
     * @see Database#Database(Input, Input, int)
     */
    private void initTimeAndCluster(int blockSize) throws IOException, ClusterNotExistException, MalformedTimeIndexException {
        String line;
        int timeId;
        int clusterId;
        int counter = 0;

        Block block = new Block(1);
        while ((line = inputTime.readLine()) != null) {
            String[] splitLine = line.split("[ \\t]+");

            if (splitLine.length != 2) {//Check si non malformé
                throw new MalformedTimeIndexException();
            }


            timeId = Integer.parseInt(splitLine[0]);
            clusterId = Integer.parseInt(splitLine[1]);
            Time time;
            Cluster cluster;

            //Check si le temps existe
            if (times.get(timeId) == null) {
                time = new Time(timeId);
                this.add(time);
                block.add(time);
                ++counter;
            } else {
                time = times.get(timeId);
            }

            cluster = clusters.get(clusterId);

            //Check si le cluster existe
            if (cluster == null) {
                throw new ClusterNotExistException();
            }

            cluster.setTime(time);
            time.add(cluster);

            if (blockSize > 0 && counter == blockSize) {
                add(block);
                block = new Block(blocks.size() + 1);
                counter = 0;
            }
        }
        if (counter != 0) {
            add(block);
        }
    }

    private void add(Block block) {
        blocks.put(block.getId(), block);
        blockIdsTree.add(block.getId());
    }

    /**
     * @param cluster le cluster à ajouter à la base
     */
    public void add(Cluster cluster) {
        clusters.put(cluster.getId(), cluster);
        clusterIdsTree.add(cluster.getId());
    }

    /**
     * @param transaction la transaction à ajouter à la base
     */
    public void add(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        transactionIdsTree.add(transaction.getId());
    }

    /**
     * @param time le temps à ajouter à la base
     */
    public void add(Time time) {
        times.put(time.getId(), time);
        timeIdsTree.add(time.getId());
    }

    /**
     * @return l'ensemble des Clusters de la base
     */
    public HashMap<Integer, Cluster> getClusters() {
        return clusters;
    }

    /**
     * @return l'ensemble des ClustersIds de la base
     */
    public TreeSet<Integer> getClusterIds() {
        return clusterIdsTree;
    }

    /**
     * @param clusterId identifiant du cluster
     * @return le cluster ayant le clusterId
     */
    public Cluster getCluster(int clusterId) {
        return clusters.get(clusterId);
    }

    /**
     * @param clusterId identifiant du cluster
     * @return l'ensemble des transactions du cluster
     */
    public HashMap<Integer, Transaction> getClusterTransactions(int clusterId) {
        return getCluster(clusterId).getTransactions();
    }

    /**
     * @param clusterId l'identifiant du cluster
     * @return le Time du cluster
     */
    public Time getClusterTime(int clusterId) {
        return clusters.get(clusterId).getTime();
    }

    public int getClusterTimeId(int clusterId) {
        return clusters.get(clusterId).getTimeId();
    }

    /**
     * @return l'ensemble des Transactions de la base
     */
    public HashMap<Integer, Transaction> getTransactions() {
        return transactions;
    }

    public TreeSet<Integer> getTransactionIds() {
        return transactionIdsTree;
    }

    /**
     * @return La transaction de la base à l'index en paramètre
     */
    public Transaction getTransaction(int transactionId) {
        return transactions.get(transactionId);
    }

    /**
     * @return l'ensemble des Times de la base
     */
    public HashMap<Integer, Time> getTimes() {
        return times;
    }

    public TreeSet<Integer> getTimeIds() {
        return timeIdsTree;
    }

    public Time getTime(int timeId) {
        return times.get(timeId);
    }

    /**
     * @param index = id du cluster
     * @return L'ensemble des transaction d'un cluster sous forme d'une chaine de caractère pour toJSON()
     */
    public String printGetClusterTransactions(int index) {
        StringBuilder s = new StringBuilder();
        for (Transaction transaction : this.getClusterTransactions(index).values()) {
            s.append(transaction.getId()).append(",");
        }
        s = new StringBuilder(s.substring(0, s.length() - 1)); //retire la dernière virgule
        return s.toString();
    }

    /**
     * @return la database en format json
     */
    public JsonObjectBuilder toJSON() {
        int index = 0;
        JsonArrayBuilder linksArray = Json.createArrayBuilder();
        for (Transaction transaction : this.getTransactions().values()) {
            ArrayList<Integer> clustersIds = new ArrayList<>(transaction.getClusterIds());

            for (int i = 0; i < clustersIds.size() - 1; i++) {
                linksArray.add(Json.createObjectBuilder()
                        .add("id", i + index)
                        .add("source", clustersIds.get(i))
                        .add("target", clustersIds.get(i + 1))
                        .add("value", 1)
                        .add("label", transaction.getId()));
            }
            index += transaction.getClusters().size();
        }

        JsonArrayBuilder nodesArray = Json.createArrayBuilder();
        for (int i = 0; i < this.getClusters().size(); i++) {
            nodesArray.add(Json.createObjectBuilder()
                    .add("id", i)
                    .add("label", this.printGetClusterTransactions(i))
                    .add("time", this.getClusterTimeId(i)));
        }
        JsonObjectBuilder links = Json.createObjectBuilder();
        links.add("links", linksArray)
                .add("nodes", nodesArray);
        return links;
    }

    private String stringToJson(JsonObjectBuilder finalJson) {
        return finalJson.build().toString();
    }

    @Override
    public String toString() {
        String str = "\n|-- Fichiers :" + inputObj + "; " + inputTime;
        str += "\n|-- Blocks :" + blocks.values();
        str += "\n|-- Clusters :" + clusters.values();
        str += "\n|-- Transactions :" + transactions.values();
        str += "\n`-- Temps :" + times.values();
        return str;
    }

    /**
     * Supprime les associations {@link Cluster} <=> {@link Transaction}
     */
    public void cleanClusterTransactionBinding() {
        this.inputObj = null;
        this.inputTime = null;

        for (Cluster cluster : clusters.values()) {
            cluster.clear();
        }

        for (Transaction transaction : transactions.values()) {
            transaction.clear();
        }
    }

    /**
     * Supprime et recrée les associations {@link Cluster} <=> {@link Transaction} en fonction des transactions à prendre en compte
     * <p>
     * <pre>
     * Lcm::UpdateOccurenceDeriver(const Database &database, const vector<int> &transactionList, ClusterMatrix &occurence)
     * </pre>
     * this (occurence)
     *
     * @param defaultDatabase (database) base de données originale
     * @param transactionIds  (transactionList) transactions à prendre en compte
     */
    @Deprecated
    public void rebindRelations(Database defaultDatabase, Set<Integer> transactionIds) {
        this.cleanClusterTransactionBinding();

        for (int transactionId : transactionIds) {
            Transaction newtransaction = this.getTransaction(transactionId);
            Transaction transaction = defaultDatabase.getTransaction(transactionId);
            Set<Integer> clusterIds = transaction.getClusterIds();

            if (newtransaction == null) {
                newtransaction = new Transaction(transactionId);
                this.add(newtransaction);
            }

            for (int clusterId : clusterIds) {
                Cluster cluster = this.getCluster(clusterId);

                if (cluster == null) {
                    cluster = new Cluster(clusterId);
                    this.add(cluster);

                    Time time = this.getTime(defaultDatabase.getClusterTimeId(clusterId));
                    if (time == null) {
                        time = new Time(defaultDatabase.getClusterTimeId(clusterId));
                        add(time);
                    }

                    cluster.setTime(time);
                    time.add(cluster);
                }

                cluster.add(newtransaction);
                newtransaction.add(cluster);
            }
        }
    }

    /**
     * Verifie si le cluster est inclus dans toute la liste des transactions
     * <p>
     * Dans GetMove :
     * <pre>
     * Lcm::CheckItemInclusion(Database,transactionList,item)
     * </pre>
     *
     * @param transactionIds (transactionList) la liste des transactions
     * @param clusterId      (item) le cluster à trouver
     * @return vrai si le cluster est présent dans toute les transactions de la liste
     */
    public boolean isClusterInTransactions(Set<Integer> transactionIds, int clusterId) {
        for (int transactionId : transactionIds) {
            if (!this.getTransaction(transactionId).getClusterIds().contains(clusterId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retourne une liste de transactions qui contienent le cluster défini par clusterId, seules les transactions contenues dans transactionIds seront itérés
     *
     * @param transactionIds liste de transactions à filter
     * @param clusterId      le cluster qui doit être contenu par la transaction
     * @return liste des transactionIds contenant le cluster
     */
    public Set<Integer> getFilteredTransactionIdsIfHaveCluster(Set<Integer> transactionIds, int clusterId) {
        Set<Integer> filteredTransactionIds = new HashSet<>();

        for (int transactionId : transactionIds) {
            Transaction transaction = this.getTransaction(transactionId);

            if (transaction.getClusterIds().contains(clusterId)) {
                filteredTransactionIds.add(transactionId);
            }
        }

        return filteredTransactionIds;
    }
}
