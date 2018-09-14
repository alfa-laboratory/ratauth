package ru.ratauth.update.services;

import ru.ratauth.update.services.dto.UpdateServiceInput;
import ru.ratauth.update.services.dto.UpdateServiceResult;
import rx.Observable;

public interface UpdateService {

    Observable<UpdateServiceResult> update(UpdateServiceInput input);
}
