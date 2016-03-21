package ru.ratauth.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ratauth.entities.Session;
import ru.ratauth.entities.Status;
import ru.ratauth.providers.auth.AuthProvider;
import ru.ratauth.providers.auth.dto.AuthInput;
import ru.ratauth.server.utils.DateUtils;
import ru.ratauth.services.SessionService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author mgorelikov
 * @since 17/03/16
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BackgroundSessionStatusChecker implements SessionStatusChecker {
  private final BlockingQueue<Session> queue = new LinkedBlockingQueue<>();
  private ExecutorService executorService;
  private final SessionService sessionService;
  private final Map<String, AuthProvider> authProviders;
  private final TokenCacheService tokenCacheService;

  @Value("${auth.session.check_threads}")
  private Integer threads;
  @Value("${auth.session.check_interval}")
  private Integer checkInterval;

  @PostConstruct
  public void init() {
    executorService = Executors.newFixedThreadPool(threads);
    for(int i = 0; i < threads; i++)
      executorService.submit(new Consumer(queue));
    executorService.shutdown();
  }

  @PreDestroy
  public void destroy() {
    executorService.shutdownNow();
  }

  @Override
  public void checkAndUpdateSession(Session session) {
    queue.offer(session);
  }

  private void process(Session session) {
    //if not yet time
    if(session.getLastCheck() != null
        && DateUtils.toLocal(session.getLastCheck()).plusSeconds(checkInterval).isAfter(LocalDateTime.now())
        || Status.BLOCKED == session.getStatus()) {
      return;
    }
    Map<String, String> userInfo =
        tokenCacheService.extractUserInfo(session.getUserInfo()).entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().toString()));
    authProviders.get(session.getIdentityProvider())
        .checkUserStatus(AuthInput.builder().relyingParty(session.getAuthClient()).data(userInfo).build())
        .flatMap(userNotBlocked -> {
          if (!userNotBlocked)
            return sessionService.invalidateSession(session.getId(), new Date());
          else
            return sessionService.updateCheckDate(session.getId(), new Date());
        })
        .toBlocking().single();
  }

  @RequiredArgsConstructor
  private class Consumer implements Runnable {
    private final BlockingQueue<Session> queue;

    @Override
    public void run() {
      Session checkTask;
      while (true && !Thread.currentThread().isInterrupted()) {
        try {
          checkTask = queue.poll();
          if(checkTask != null)
            process(checkTask);
          else
            Thread.sleep(100);
        } catch (Exception e) {
          log.error("Error during session check", e);
        }
      }
    }
  }
}
