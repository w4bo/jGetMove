package fr.jgetmove.jgetmove.pattern;
import javax.json.*;

public interface Pattern {
	public JsonArrayBuilder getLinksToJson(int index,JsonArrayBuilder patternEntryArray);
}
