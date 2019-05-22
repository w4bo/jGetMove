/*
 * Copyright 2019 jGetMove
 *
 * Cette œuvre est mise à disposition sous licence Attribution - Pas d’Utilisation Commerciale - Partage dans les Mêmes Conditions 3.0 France. Pour voir une copie de cette licence, visitez http://creativecommons.org/licenses/by-nc-sa/3.0/fr/ ou écrivez à Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class containing all the basic data. Allows to bind the data from input files
 * <p>
 * All the data is binded once, note that adding or rebinding data in here is expensive. Use {@link ClusterMatrix} for a dynamic database.
 *
 * @author stardisblue
 * @author jframos0
 * @author Carmona-Anthony
 * @version 1.0.0
 * @since 0.1.0
 */
public class DataBase extends Base {
    private Input inputObj, inputTime;

    /**
     * Initialises the database following the configurations in the files
     *
     * @param inputObj  file (transactionId [clusterId ...])
     * @param inputTime file (timeId clusterId)
     * @throws IOException              if inputObj or inputTime is incorrect
     * @throws ClusterNotExistException if the cluster doesn't exist
     */
    public DataBase(Input inputObj,
                    Input inputTime) throws IOException, ClusterNotExistException, MalformedTimeIndexException {
        super();

        this.inputObj = inputObj;
        this.inputTime = inputTime;

        initClusterAndTransaction();
        initTimeAndCluster();
    }

    /**
     * Initiates the Cluster and Transactions HashMap, linking each other.
     *
     * @throws IOException if the input file is incorrect
     * @implSpec Iterates over each line of transaction cluster file and parses all the data within as clusters.
     * uses {@link Transaction#add(Cluster)} and {@link Cluster#add(Transaction)}
     * @see DataBase#DataBase(Input, Input)
     */
    protected void initClusterAndTransaction() throws IOException {
        String line;
        int transactionId = 0;

        while ((line = inputObj.readLine()) != null) { // foreach line
            String[] lineClusterId = line.split("[ \\t]+");
            Transaction transaction = new Transaction(transactionId);

            for (String strClusterId : lineClusterId) {// foreach cluster
                int clusterId = Integer.parseInt(strClusterId);
                Cluster cluster = getOrCreateCluster(clusterId);

                cluster.add(transaction);
                transaction.add(cluster);
            }

            this.add(transaction);

            transactionId++;
        }

        inputObj.close();
    }

    /**
     * Initializes the link between Time and Cluster
     * <p>
     * to be used after {@link #initClusterAndTransaction}
     *
     * @throws IOException                 if the inputTime file is incorrect
     * @throws ClusterNotExistException    if the cluster does not exist
     * @throws MalformedTimeIndexException if there is more clusters on the same timeline
     * @implSpec iterates over each line and sets the first int as the timeId and the second as the clusterId. Checks wether the clusterId exists throws an exception otherwise.
     * @see DataBase#DataBase(Input, Input)
     * @see #initClusterAndTransaction()
     */
    protected void initTimeAndCluster() throws IOException, ClusterNotExistException, MalformedTimeIndexException {
        String line;

        while ((line = inputTime.readLine()) != null) {
            String[] splitLine = line.split("[ \\t]+");

            if (splitLine.length != 2) {//checks if the file is correctly formed
                throw new MalformedTimeIndexException();
            }


            int timeId = Integer.parseInt(splitLine[0]);
            int clusterId = Integer.parseInt(splitLine[1]);

            Time time = times.get(timeId);
            //Check if the time exists
            if (time == null) {
                time = new Time(timeId);
                this.add(time);
            }

            Cluster cluster = clusters.get(clusterId);

            //Check if the cluster exists
            if (cluster == null) {
                throw new ClusterNotExistException();
            }

            cluster.setTime(time);
            time.add(cluster);
        }

        inputTime.close();
    }

    /**
     * @param index = id du cluster
     * @return L'ensemble des transaction d'un cluster sous forme d'une chaine de caractère pour toJson()
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
     * // TODO: 26/07/2017
     */
    public JsonObjectBuilder toJson() {
        //int index = 0;
        JsonArrayBuilder linksArray = Json.createArrayBuilder();
        for (Transaction transaction : this.getTransactions().values()) {
            ArrayList<Integer> clustersIds = new ArrayList<>(transaction.getClusterIds());

            for (int i = 0; i < clustersIds.size() - 1; i++) {
                linksArray.add(Json.createObjectBuilder()
                                   .add("id", transaction.getId())
                                   .add("source", clustersIds.get(i))
                                   .add("target", clustersIds.get(i + 1))
                                   .add("value", 1)
                                   .add("label", transaction.getId()));
            }
            //index += transaction.getClusters().size();
        }

        JsonArrayBuilder nodesArray = Json.createArrayBuilder();
        for (int i : this.getClusterIds()) {
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

    @Override
    public String toPrettyString() {
        return "\n|-- Fichiers : " + inputObj + "; " + inputTime +
                super.toPrettyString();
    }
}
