package ru.ratauth.server.updateServices;

import ru.ratauth.updateServices.dto.UpdateServiceInput;
import rx.Observable;

public interface UpdateServiceExecutor {

    Observable<Object> getUpdateService(UpdateServiceInput updateServiceInput);
}
