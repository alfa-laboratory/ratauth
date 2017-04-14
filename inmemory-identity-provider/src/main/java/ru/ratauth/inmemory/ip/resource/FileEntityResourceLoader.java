package ru.ratauth.inmemory.ip.resource;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.UrlResource;

import java.io.FileInputStream;
import java.io.InputStream;

import static java.lang.String.format;

@RequiredArgsConstructor
public class FileEntityResourceLoader implements EntityResourceLoader {

  private final String dir;

  @Override
  @SneakyThrows
  public InputStream inputStream(String fileName) {
    return new FileInputStream(format("%s/%s", dir, fileName));
  }
}
