package dev.wdona.gestorinventarioqr.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.repository.EstanteriaRepository;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaViewModel {
    EstanteriaRepository repository;

    public EstanteriaViewModel(EstanteriaRepository repository) {
        this.repository = repository;
    }

    private MutableLiveData<List<Estanteria>> _estanteriaLiveData = new MutableLiveData<>();
    public LiveData<List<Estanteria>> estanteriasLiveData = _estanteriaLiveData;

    public Estanteria getEstanteriaById(Long id) {
        return repository.getEstanteriaById(id); // esta deberia de traer tambien los productos, pero no estoy seguro
    }

    public Estanteria getEstanteriaConProductosById(Long idEstanteria) {
        return repository.getEstanteriaConProductosById(idEstanteria);
    }

    public void sincronizar(Estanteria... estanterias) {
        repository.sincronizar(estanterias);
    }
}
