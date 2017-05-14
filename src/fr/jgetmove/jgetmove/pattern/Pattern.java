package fr.jgetmove.jgetmove.pattern;

import javax.json.JsonArrayBuilder;

public interface Pattern {
    public JsonArrayBuilder getLinksToJson(int index, JsonArrayBuilder patternEntryArray);
}
