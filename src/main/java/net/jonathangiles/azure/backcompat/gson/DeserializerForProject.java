package net.jonathangiles.azure.backcompat.gson;

import com.google.gson.*;
import net.jonathangiles.azure.backcompat.model.CompareRequest;
import net.jonathangiles.azure.backcompat.maven.MavenUtil;
import net.jonathangiles.azure.backcompat.maven.Version;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DeserializerForProject implements JsonDeserializer<List<CompareRequest>> {

    @Override
    public List<CompareRequest> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        System.out.println(jsonElement);

        if (jsonElement.isJsonPrimitive()) {
            String mavenGA = jsonElement.getAsJsonPrimitive().getAsString();
            List<Version> versions = MavenUtil.getLatestVersionInMavenCentral(mavenGA, true, 3);
            System.out.println(mavenGA + " : " + versions);

            List<CompareRequest> requests = new ArrayList<>();
            for (int i = 0; i < versions.size() - 1; i++) {
                requests.add(new CompareRequest(mavenGA, versions.get(i), versions.get(i + 1)));
            }

            return requests;
        }

        System.out.println("Can't parse JSON element: " + jsonElement);
        System.exit(-1);
        return null;
    }
}