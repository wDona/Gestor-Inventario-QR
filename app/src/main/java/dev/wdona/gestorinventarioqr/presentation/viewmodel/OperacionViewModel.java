package dev.wdona.gestorinventarioqr.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;
import dev.wdona.gestorinventarioqr.domain.repository.OperacionRepository;

public class OperacionViewModel {
    OperacionRepository repository;

    public OperacionViewModel(OperacionRepository repository) {
        this.repository = repository;
    }

    private MutableLiveData<List<Operacion>> _operacionLiveData = new MutableLiveData<>();
    public LiveData<List<Operacion>> operacionLiveData = _operacionLiveData;

    public void cargarOperaciones() {
        List<Operacion> operaciones = repository.getAllOperaciones();
        _operacionLiveData.postValue(operaciones);
    }

    public void actualizarEstadoOperacion(Long id, EstadoOperacion nuevoEstado) {
        repository.actualizarEstadoById(id, nuevoEstado);
        cargarOperaciones();
    }

    public void agregarOperacion(Operacion operacion) {
        repository.agregarOperacionPendiente(operacion);
        cargarOperaciones();
    }

    public boolean reintentarEnvioOperacion(Operacion operacion) {
        boolean exito = repository.reintentarOperacion(operacion);
        if (exito) {
            cargarOperaciones();
            return true;
        }
        return false;
    }

    public boolean reintentarEnvioAllOperaciones() {
        boolean exito = repository.reintentarAllOperaciones();
        cargarOperaciones();

        return exito;
    }

    public void cargarOperacionesPorEstados(EstadoOperacion ... estado) {
        List<Operacion> operaciones = new ArrayList<>();
        for (EstadoOperacion e : estado) {
            operaciones.addAll(repository.getOperacionesPorEstado(e));
        }

        _operacionLiveData.postValue(operaciones);
    }

    public Operacion getOperacionById(Long id) {
        return repository.getOperacionPendienteById(id);
    }
}
