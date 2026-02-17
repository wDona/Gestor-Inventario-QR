package dev.wdona.gestorinventarioqr.data.repository;

import dev.wdona.gestorinventarioqr.data.datasource.local.impl.ProductoLocalDataSourceImpl;
import dev.wdona.gestorinventarioqr.data.datasource.remote.impl.ProductoRemoteDataSourceImpl;
import dev.wdona.gestorinventarioqr.domain.repository.ProductoRepository;
import dev.wdona.gestorinventarioqr.domain.model.Estanteria;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;
import dev.wdona.gestorinventarioqr.domain.model.Producto;

public class ProductoRepositoryImpl implements ProductoRepository {
    ProductoRemoteDataSourceImpl remote;
    ProductoLocalDataSourceImpl local;
    OperacionRepositoryImpl registro;


    ProductoRepositoryImpl(ProductoRemoteDataSourceImpl remote, ProductoLocalDataSourceImpl local) {
        this.remote = remote;
        this.local = local;
    }

    @Override
    public void addUndsProduct(Producto producto, int cantidad) {
        try {
            remote.addUndsProduct(producto, cantidad);
        } catch (Exception e) {
            registro.agregarOperacionPendiente(
                    new Operacion(
                         registro.getUltimoIdOperacionPendiente() + 1,
                            System.currentTimeMillis(),
                            "ADD",
                            producto.getId(),
                            producto.getEstanteria().getId(),
                            cantidad,
                            "PENDIENTE"
                    )
            );
            local.addUndsProduct(producto, cantidad);
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void removeUndsProduct(Producto producto, int cantidad) {
        try {
            remote.removeUndsProduct(producto, cantidad);
        } catch (Exception e) {
            registro.agregarOperacionPendiente(
                    new Operacion(
                            registro.getUltimoIdOperacionPendiente() + 1,
                            System.currentTimeMillis(),
                            "REMOVE",
                            producto.getId(),
                            producto.getEstanteria().getId(),
                            cantidad,
                            "PENDIENTE"
                    )
            );
            local.removeUndsProduct(producto, cantidad);
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void assignProductToEstanteria(Producto producto, Estanteria estanteria) {
        try {
            remote.assignProductToEstanteria(producto, estanteria);
        } catch (Exception e) {
            registro.agregarOperacionPendiente(
                    new Operacion(
                            registro.getUltimoIdOperacionPendiente() + 1,
                            System.currentTimeMillis(),
                            "MOVE",
                            producto.getId(),
                            estanteria.getId(),
                            producto.getCantidad(),
                            "PENDIENTE"
                    )
            );
            local.assignProductToEstanteria(producto, estanteria);
            System.out.println("Error: " + e.getMessage());
        }
    }
}
