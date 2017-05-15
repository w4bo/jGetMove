package fr.jgetmove.jgetmove.pattern;

import javax.json.JsonObject;
import java.util.ArrayList;

public interface Pattern {
    ArrayList<JsonObject> getLinksToJson(int index);
}
