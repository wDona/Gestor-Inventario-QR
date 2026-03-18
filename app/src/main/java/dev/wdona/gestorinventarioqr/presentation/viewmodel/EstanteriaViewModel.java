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

    public Estanteria getEstanteriaById(String id) {
        return repository.getEstanteriaById(id); // esta deberia de traer tambien los productos, pero no estoy seguro
    }

    public Estanteria getEstanteriaConProductosById(String idEstanteria) {
        return repository.getEstanteriaConProductosById(idEstanteria);
    }

    public void sincronizar(Estanteria... estanterias) {
        repository.sincronizar(estanterias);
    }

    public void createEstanteria(Estanteria estanteria) throws Exception {
        repository.createEstanteria(estanteria);
    }

    public void deleteEstanteria(String id) throws Exception {
        repository.deleteEstanteria(id);
    }
}
