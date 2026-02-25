package dev.wdona.gestorinventarioqr.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.relation.RelacionEstanteriaProducto;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.repository.ProductoRepository;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoViewModel extends ViewModel {
    ProductoRepository repository;

    public ProductoViewModel(ProductoRepository repository) {
        this.repository = repository;
    }

    private MutableLiveData<List<Producto>> _productosLiveData = new MutableLiveData<>();
    public LiveData<List<Producto>> productosLiveData = _productosLiveData;

    public boolean addUndsProduct(Producto producto, int unidadesSumadas) {
        if (unidadesSumadas <= 0) {
            System.out.println("Error: No se pueden agregar unidades negativas o cero.");
            return false;
        } else {
            repository.addUndsProduct(producto, unidadesSumadas);
        }

        return true;
    }

    public boolean removeUndsProduct(Producto producto, int unidadesRestadas) {
        if (unidadesRestadas <= 0) {
            System.out.println("Error: No se pueden restar unidades negativas o cero.");
            return false;
        } else if (producto.getCantidad() < unidadesRestadas) {
            System.out.println("Error: No se pueden restar más unidades de las que hay en stock.");
            return false;
        } else {
            repository.removeUndsProduct(producto, unidadesRestadas);
        }

        return true;
    }

    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        repository.assignProductToEstanteria(producto, estanteria);
    }

    public Producto getProductoById(Long id) {
        return repository.getProductoById(id);
    }

    public void sincronizar(Producto... productos) {
        repository.sincronizar(productos);
    }
}
