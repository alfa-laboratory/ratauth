package ru.ratauth.inmemory.ip.resource;

import java.io.InputStream;

public interface EntityResourceLoader {

  InputStream inputStream(String fileName);

}