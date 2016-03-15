package ru.ratauth.utils;

/**
 * @author mgorelikov
 * @since 10/02/16
 */
public class ExceptionUtils {

  public static <T extends Throwable> T getThrowable(Throwable throwable, Class<T> clazz, int maxDepth) {
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

}
