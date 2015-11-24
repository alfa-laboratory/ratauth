package ru.ratauth.server.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ratpack.spring.config.RatpackProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;

/**
 * @author mgorelikov
 * @since 23/11/15
 */
@Component
@Primary
@ConfigurationProperties(prefix = "ratpack", ignoreUnknownFields = false)
public class RatAuthProperties extends RatpackProperties {
  @PostConstruct
  public void initBaseDir() {
    ClassPathResource classPath = new ClassPathResource("");
    try {
      if (classPath.getURL().toString().startsWith("jar:")) {
        this.setBasedir(classPath);
      }
    } catch (IOException e) {
      // Ignore
    }
    FileSystemResource resources = new FileSystemResource("server/src/main/resources");
    this.setBasedir(resources);
  }


  @Override
  public Path getBasepath() {
    try {
      return resourceToPath(this.getBasedir().getURL());
    } catch (IOException e) {
      throw new IllegalStateException("Cannot extract base dir URL", e);
    }
  }

  static Path resourceToPath(URL resource) {
    Objects.requireNonNull(resource, "Resource URL cannot be null");
    URI uri;
    try {
      uri = resource.toURI();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Could not extract URI", e);
    }

    String scheme = uri.getScheme();
    if (scheme.equals("file")) {
      String path = uri.toString().substring("file:".length());
      if (path.contains("//")) {
        path = StringUtils.cleanPath(path.replace("//", ""));
      }
      return Paths.get(new FileSystemResource(path).getFile().toURI());
    }

    if (!scheme.equals("jar")) {
      throw new IllegalArgumentException("Cannot convert to Path: " + uri);
    }

    String s = uri.toString();
    int separator = s.indexOf("!/");
    String entryName = s.substring(separator + 2);
    URI fileURI = URI.create(s.substring(0, separator));

    FileSystem fs;
    try {
      fs = FileSystems.newFileSystem(fileURI, Collections.<String, Object>emptyMap());
      return fs.getPath(entryName).toAbsolutePath();
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not create file system for resource: " + resource, e);
    }
  }
}
