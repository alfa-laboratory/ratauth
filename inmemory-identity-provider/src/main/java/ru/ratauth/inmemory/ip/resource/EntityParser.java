package ru.ratauth.inmemory.ip.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import ru.ratauth.entities.Session;

import java.io.InputStream;
import java.io.InputStreamReader;

public class EntityParser {

  private static final String UTF_8 = "UTF-8";
  private final EntityResourceLoader entityResourceLoader;

  public EntityParser(EntityResourceLoader entityResourceLoader) {
    this.entityResourceLoader = entityResourceLoader;
  }

  @SneakyThrows
  public <T> T load(String fileName, Class<T> clazz) {
    Gson gson = getGson();
    InputStream inputStream = entityResourceLoader.inputStream(fileName);
    return gson.fromJson(new InputStreamReader(inputStream, UTF_8), clazz);
  }

  private Gson getGson() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Session[].class, new SessionDeserializer());
//    return gsonBuilder.create();
    return new Gson();
  }

}
