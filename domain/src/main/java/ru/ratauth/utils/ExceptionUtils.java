package ru.ratauth.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author mgorelikov
 * @since 10/02/16
 */
public class ExceptionUtils {

  public static <T extends Throwable> T getThrowable(Throwable throwable, Class<T> clazz, int maxDepth) {
    return searchThrowable(throwable, clazz, maxDepth, 0);
  }

  public static Throwable getThrowable(Throwable throwable, List<Class<? extends Throwable>> clazz, int maxDepth) {
    return searchThrowable(throwable, clazz, maxDepth, 0);
  }

  private static <T extends Throwable> T searchThrowable(Throwable throwable, Class<T> clazz, int maxDepth, int count) {
    if(throwable == null)
      return null;
    if(clazz.isAssignableFrom(throwable.getClass()))
      return clazz.cast(throwable);
    Throwable cause = throwable.getCause();
    if (cause != null && clazz.isAssignableFrom(cause.getClass()))
      return clazz.cast(cause);
    else {
      if (count > maxDepth)
        return null;
      else
        return searchThrowable(cause, clazz, maxDepth, count + 1);
    }
  }

  private static Throwable searchThrowable(Throwable throwable, List<Class<? extends Throwable>> clazz, int maxDepth, int count) {
    if(throwable == null)
      return null;
    Optional<Throwable> result = checkAndCast(clazz, throwable);
    if(result.isPresent())
      return result.get();
    Throwable cause = throwable.getCause();
    if (cause != null && (result = checkAndCast(clazz, cause)).isPresent())
      return result.get();
    else {
      if (count > maxDepth)
        return null;
      else
        return searchThrowable(cause, clazz, maxDepth, count + 1);
    }
  }

  private static  Optional<Throwable> checkAndCast(List<Class<? extends Throwable>> clazz, Throwable throwable) {
    return clazz.stream().filter(cl -> cl.isAssignableFrom(throwable.getClass())).map(cl -> throwable).findFirst();
  }

}