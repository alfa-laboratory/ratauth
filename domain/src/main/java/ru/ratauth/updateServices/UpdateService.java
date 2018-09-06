package ru.ratauth.updateServices;

import ru.ratauth.updateServices.dto.UpdateServiceInput;
import ru.ratauth.updateServices.dto.UpdateServiceOutput;
import rx.Observable;

public interface UpdateService {

    Observable<UpdateServiceOutput> update(UpdateServiceInput input);
}
