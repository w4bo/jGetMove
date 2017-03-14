package fr.jgetmove.jgetmove;

import fr.jgetmove.jgetmove.database.Database;
import fr.jgetmove.jgetmove.io.Input;

class Main {
    public static void main(String[] args) {

        Input inputObj = new Input("");
        Input inputTime = new Input("");

        Database database = new Database(inputObj, inputTime);

    }
}