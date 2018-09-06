package ru.ratauth.updateServices;

import ru.ratauth.updateServices.dto.UpdateServiceInput;
import ru.ratauth.updateServices.dto.UpdateServiceResult;
import rx.Observable;

public interface UpdateService {

    Observable<UpdateServiceResult> update(UpdateServiceInput input);
}
