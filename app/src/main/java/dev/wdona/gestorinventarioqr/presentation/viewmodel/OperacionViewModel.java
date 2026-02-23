package dev.wdona.gestorinventarioqr.presentation.viewmodel;

import dev.wdona.gestorinventarioqr.domain.repository.OperacionRepository;

public class OperacionViewModel {
    OperacionRepository repository;

    public OperacionViewModel(OperacionRepository repository) {
        this.repository = repository;
    }


}
