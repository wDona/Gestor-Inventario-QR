package dev.wdona.gestorinventarioqr.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.repository.ProductoRepository;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoViewModel extends ViewModel {
    ProductoRepository repository;

    public ProductoViewModel(ProductoRepository repository) {
        this.repository = repository;
    }

    private MutableLiveData<List<Producto>> _productosLiveData = new MutableLiveData<>();
    public LiveData<List<Producto>> productosLiveData = _productosLiveData;


    public void addUndsProduct(Producto producto, int unidadesSumadas) {
        if (unidadesSumadas <= 0) System.out.println("Error: No se pueden agregar unidades negativas o cero.");
        else {
            repository.addUndsProduct(producto, unidadesSumadas);
        }
    }
}
