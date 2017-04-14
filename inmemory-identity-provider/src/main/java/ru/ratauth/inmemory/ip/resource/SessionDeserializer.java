package ru.ratauth.inmemory.ip.resource;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.apache.commons.lang3.time.DateUtils;
import ru.ratauth.entities.Session;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SessionDeserializer implements JsonDeserializer<Session[]> {

  @Override
  public Session[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    List<Session> sessions = new ArrayList<>();

    json.getAsJsonArray().forEach(jsonElement -> {
      Session session = context.deserialize(jsonElement, Session.class);
      session.setCreated(new Date());
      session.setExpiresIn(DateUtils.addDays(new Date(), 1));
      sessions.add(session);
    });

    return sessions.stream().toArray(Session[]::new);
  }
}