package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.debug.Debug;

import java.util.TreeSet;

/**
 *
 */
public class PathsOfBlock {
    private int id;
    private TreeSet<Path> paths;

    public PathsOfBlock(int id, TreeSet<Path> paths) {
        this.id = id;
        this.paths = paths;
    }

    public int getId() {
        return id;
    }

    public TreeSet<Path> getPaths() {
        return paths;
    }

    @Override
    public String toString() {
        String str = "\n. id : " + id;
        str += "\n`-- paths :" + Debug.indent(paths.toString());
        return str;
    }
}
