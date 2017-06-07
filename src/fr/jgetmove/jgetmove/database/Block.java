package fr.jgetmove.jgetmove.database;

import java.util.HashMap;

public class Block {

    private int id;

    /**
     * L'ensemble des temps du bloc (idTemps => Temps)
     */
    private HashMap<Integer, Time> times;

    /**
     * @param id identifiant du bloc
     */
    Block(int id) {
        this.id = id;
        times = new HashMap<>();
    }

    /**
     * @param time ajoute un temps à la liste des temps
     */
    void add(Time time) {
        times.put(time.getId(), time);
    }

    /**
     * @return retourne l'id du bloc
     */
    public int getId() {
        return id;
    }


    @Override
    public String toString() {
        return "{" + id + "=" + String.valueOf(times.keySet()) + "}";
    }

}
