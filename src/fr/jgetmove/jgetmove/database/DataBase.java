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
 * Contains all the objects, has the job of managing data and saving informations
 */
public class DataBase extends Base {
    private Input inputObj, inputTime;

    /**
     * Initialises the database following the configurations in the files
     *
     * @param inputObj  file (transactionId [clusterId ...])
     * @param inputTime file (timeId clusterId)
     * @throws IOException              if inputObj or inputTime is incorrect
     * @throws ClusterNotExistException si le cluster n'existe pas
     */
    public DataBase(Input inputObj, Input inputTime) throws IOException, ClusterNotExistException, MalformedTimeIndexException {
        super();

        this.inputObj = inputObj;
        this.inputTime = inputTime;


        //Initialisation des clusters et transactions
        initClusterAndTransaction();
        //Initialisation des temps
        initTimeAndCluster();
    }

    /**
     * Initialise les HashMap de clusters et transactions
     *
     * @throws IOException si le fichier est incorrect
     * @see DataBase#DataBase(Input, Input)
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
     * @see DataBase#DataBase(Input, Input)
     */
    private void initTimeAndCluster() throws IOException, ClusterNotExistException, MalformedTimeIndexException {
        String line;

        while ((line = inputTime.readLine()) != null) {
            String[] splitLine = line.split("[ \\t]+");

            if (splitLine.length != 2) {//Check si non malformé
                throw new MalformedTimeIndexException();
            }


            int timeId = Integer.parseInt(splitLine[0]);
            int clusterId = Integer.parseInt(splitLine[1]);
            Time time;
            Cluster cluster;

            //Check si le temps existe
            if (times.get(timeId) == null) {
                time = new Time(timeId);
                this.add(time);
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

        }
    }


    /**
     * @param clusterId l'identifiant du cluster
     * @return le Time du cluster
     */
    public Time getClusterTime(int clusterId) {
        return clusters.get(clusterId).getTime();
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

    @Override
    public String toPrettyString() {
        return "\n|-- Fichiers : " + inputObj + "; " + inputTime +
                super.toPrettyString();
    }
}
