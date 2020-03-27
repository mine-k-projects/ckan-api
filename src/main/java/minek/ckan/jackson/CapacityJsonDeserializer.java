package minek.ckan.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import minek.ckan.v3.enums.Capacity;

import java.io.IOException;

public class CapacityJsonDeserializer extends JsonDeserializer<Capacity> {

    @Override
    public Capacity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectCodec objectCodec = p.getCodec();
        JsonNode jsonNode = objectCodec.readTree(p);
        String s = jsonNode.asText();
        for (Capacity value : Capacity.values()) {
            if (value.getCode().equals(s)) {
                return value;
            }
        }
        return null;
    }
}