package ru.ratauth.inmemory.ip.resource;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static java.lang.String.format;

public class ClassPathEntityResourceLoader implements EntityResourceLoader {

  private final String path;

  public ClassPathEntityResourceLoader() {
    this("");
  }

  public ClassPathEntityResourceLoader(String path) {
    this.path = path;
  }

  @Override
  @SneakyThrows
  public InputStream inputStream(String fileName) {
    return new ClassPathResource(format("%s/%s", path, fileName)).getInputStream();
  }
}
