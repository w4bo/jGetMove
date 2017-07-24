package fr.jgetmove.jgetmove.database;

/**
 * @version 1.1.0
 * @since 0.2.0
 */
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
        return "\n. id : " + id + super.toPrettyString();
    }
}
