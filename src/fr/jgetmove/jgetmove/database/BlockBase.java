package fr.jgetmove.jgetmove.database;

public class BlockBase extends Base {

    private int id;

    public BlockBase(int id) {
        super();
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toPrettyString() {
        return "\n. id : " + id + super.toString();
    }
}
