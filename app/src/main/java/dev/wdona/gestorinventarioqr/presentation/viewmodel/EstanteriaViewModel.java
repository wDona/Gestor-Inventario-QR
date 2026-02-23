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
        return repository.getEstanteriaById(id);
    }

    public Estanteria getEstanteriaConProductosById(Long idEstanteria) {
        RelacionEstanteriaProducto relacion = repository.getEstanteriaConProductosById(idEstanteria);

        // Verificar que relacion no sea null
        if (relacion == null || relacion.estanteria == null) {
            System.out.println("Relacion o estanteria es null para id: " + idEstanteria);
            return null;
        }

        Estanteria estanteria = getEstanteriaById(idEstanteria);

        if (estanteria != null && Objects.equals(relacion.estanteria.id, estanteria.getId())) {
            // Paso los productos de relacion a dominio, asignandole esta estanteria
            List<Producto> productos = ProductoMapper.toDomainList(relacion.productos, estanteria);
            estanteria.setProductos(productos);

            return estanteria;
        }
        return null;
    }
}
