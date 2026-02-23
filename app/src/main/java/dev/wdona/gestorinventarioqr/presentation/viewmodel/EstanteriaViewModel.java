package dev.wdona.gestorinventarioqr.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Objects;

import dev.wdona.gestorinventarioqr.data.datasource.mapper.ProductoMapper;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;
import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Producto;
import dev.wdona.gestorinventarioqr.domain.repository.EstanteriaRepository;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public class EstanteriaViewModel {
    EstanteriaRepository repository;

    public EstanteriaViewModel(EstanteriaRepository repository) {
        this.repository = repository;
    }

    private MutableLiveData<List<Estanteria>> _estanteriaLiveData = new MutableLiveData<>();
    public LiveData<List<Estanteria>> estanteriaLiveData = _estanteriaLiveData;

    public Estanteria getEstanteriaById(Long id) {
        return repository.getEstanteriaById(id); // esta deberia de traer tambien los productos, pero no estoy seguro
    }

    public Estanteria getEstanteriaConProductosById(Long idEstanteria) {
        return repository.getEstanteriaConProductosById(idEstanteria);
    }
}
