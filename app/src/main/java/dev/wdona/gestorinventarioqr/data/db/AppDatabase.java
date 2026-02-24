package dev.wdona.gestorinventarioqr.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.data.TipoOperacion;
import dev.wdona.gestorinventarioqr.data.entity.EstanteriaEntity;
import dev.wdona.gestorinventarioqr.data.entity.OperacionEntity;
import dev.wdona.gestorinventarioqr.data.entity.ProductoEntity;

@Database(entities = {ProductoEntity.class, EstanteriaEntity.class, OperacionEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductoDao productoDao();
    public abstract EstanteriaDao estanteriaDao();
    public abstract OperacionDao operacionDao();


    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();


    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "inventario_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public void populateInitialData() {
        databaseWriteExecutor.execute(() -> {
            try {
                EstanteriaDao estanteriaDao = estanteriaDao();
                int count = estanteriaDao.getCount();
                android.util.Log.d("AppDatabase", "Conteo de estanterias: " + count);

                if (count == 0) {
                    android.util.Log.d("AppDatabase", "Insertando datos iniciales...");

                    EstanteriaEntity est1 = new EstanteriaEntity();
                    est1.setId(1L);
                    est1.setNombre("Estanteria 1");
                    estanteriaDao.insertEstanteria(est1);

                    EstanteriaEntity est2 = new EstanteriaEntity();
                    est2.setId(2L);
                    est2.setNombre("Estanteria 2");
                    estanteriaDao.insertEstanteria(est2);
                    android.util.Log.d("AppDatabase", "Estanterias insertadas");

                    // También insertar productos de prueba
                    ProductoDao prodDao = productoDao();
                    ProductoEntity prod1 = new ProductoEntity();
                    prod1.setId(1L);
                    prod1.setNombre("Producto A");
                    prod1.setPrecio(10.0);
                    prod1.setCantidad(10);
                    prod1.setFK_estanteriaId(1L);
                    prodDao.insertProducto(prod1);
                    android.util.Log.d("AppDatabase", "Producto A insertado en estanteria 1");

                    ProductoEntity prod2 = new ProductoEntity();
                    prod2.setId(2L);
                    prod2.setNombre("Producto B");
                    prod2.setPrecio(20.0);
                    prod2.setCantidad(5);
                    prod2.setFK_estanteriaId(2L);
                    prodDao.insertProducto(prod2);
                    android.util.Log.d("AppDatabase", "Producto B insertado en estanteria 2");

                    OperacionDao operacionDao = operacionDao();
                    OperacionEntity operacion1 = new OperacionEntity();
                    operacion1.setId(1L);
                    operacion1.setTimestamp(System.currentTimeMillis());
                    operacion1.setTipoOperacion(TipoOperacion.ADD_UNDS_PRODUCT.getValor());
                    operacion1.setProductoId(1L);
                    operacion1.setEstanteriaId(1L);
                    operacion1.setCantidad(10);
                    operacion1.setEstado(EstadoOperacion.PENDIENTE.getValor());
                    operacionDao.agregarOperacion(operacion1);

                    OperacionEntity operacion2 = new OperacionEntity();
                    operacion2.setId(2L);
                    operacion2.setTimestamp(System.currentTimeMillis());
                    operacion2.setTipoOperacion(TipoOperacion.REMOVE_UNDS_PRODUCT.getValor());
                    operacion2.setProductoId(2L);
                    operacion2.setEstanteriaId(2L);
                    operacion2.setCantidad(5);
                    operacion2.setEstado(EstadoOperacion.PENDIENTE.getValor());
                    operacionDao.agregarOperacion(operacion2);

                    OperacionEntity operacion3 = new OperacionEntity();
                    operacion3.setId(3L);
                    operacion3.setTimestamp(System.currentTimeMillis());
                    operacion3.setTipoOperacion(TipoOperacion.ASSIGN_PRODUCT_TO_ESTANTERIA.getValor());
                    operacion3.setProductoId(1L);
                    operacion3.setEstanteriaId(2L);
                    operacion3.setCantidad(0);
                    operacion3.setEstado(EstadoOperacion.PENDIENTE.getValor());
                    operacionDao.agregarOperacion(operacion3);

                    OperacionEntity operacion4 = new OperacionEntity();
                    operacion4.setId(4L);
                    operacion4.setTimestamp(System.currentTimeMillis());
                    operacion4.setTipoOperacion(TipoOperacion.ADD_UNDS_PRODUCT.getValor());
                    operacion4.setProductoId(2L);
                    operacion4.setEstanteriaId(1L);
                    operacion4.setCantidad(5);
                    operacion4.setEstado(EstadoOperacion.ENVIADA.getValor());
                    operacionDao.agregarOperacion(operacion4);

                    android.util.Log.d("AppDatabase", "Operaciones insertadas");
                    android.util.Log.d("AppDatabase", "Datos iniciales insertados correctamente");
                } else {
                    android.util.Log.d("AppDatabase", "Ya existen datos, no se insertan nuevos");
                }
            } catch (Exception e) {
                android.util.Log.e("AppDatabase", "Error en populateInitialData: " + e.getMessage(), e);
            }
        });
    }
}
