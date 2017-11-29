package de.lmu.settleBattle.catanServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

public class JSONStringBuilder {

    /**
     * overrides class turned it into JSON structured String
     */
    public String toJSONString() {
        final GsonBuilder builder = new GsonBuilder();

        //this setting says that the attributes marked with the annotation
        //@Expose are serialized to JSONObject
        builder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = builder.create();
        return gson.toJson(this);
    }

    /**
     * overrides class turned into JSON structured string
     * This method can be overriden in child class if some
     * data should not be accessible for everybody
     * @return
     */
    public String toJSONString_Unknown() { return toJSONString(); };
}
