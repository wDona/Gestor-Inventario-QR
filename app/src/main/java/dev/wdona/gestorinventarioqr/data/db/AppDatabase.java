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

@Database(entities = {ProductoEntity.class, EstanteriaEntity.class, OperacionEntity.class}, version = 9, exportSchema = false)
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

                    // === ESTANTERIAS ===
                    EstanteriaEntity est1 = new EstanteriaEntity();
                    est1.setId(1L);
                    est1.setNombre("Estanteria A - Electronica");
                    estanteriaDao.insertEstanteria(est1);

                    EstanteriaEntity est2 = new EstanteriaEntity();
                    est2.setId(2L);
                    est2.setNombre("Estanteria B - Herramientas");
                    estanteriaDao.insertEstanteria(est2);

                    EstanteriaEntity est3 = new EstanteriaEntity();
                    est3.setId(3L);
                    est3.setNombre("Estanteria C - Limpieza");
                    estanteriaDao.insertEstanteria(est3);

                    EstanteriaEntity est4 = new EstanteriaEntity();
                    est4.setId(4L);
                    est4.setNombre("Estanteria D - Oficina");
                    estanteriaDao.insertEstanteria(est4);

                    android.util.Log.d("AppDatabase", "4 Estanterias insertadas");

                    // === PRODUCTOS ===
                    ProductoDao prodDao = productoDao();

                    // Estanteria 1 - Electronica
                    ProductoEntity prod1 = new ProductoEntity();
                    prod1.setId(1L);
                    prod1.setNombre("Cable USB-C");
                    prod1.setPrecio(5.99);
                    prod1.setCantidad(50);
                    prod1.setFK_estanteriaId(1L);
                    prodDao.insertProducto(prod1);

                    ProductoEntity prod2 = new ProductoEntity();
                    prod2.setId(2L);
                    prod2.setNombre("Cargador 20W");
                    prod2.setPrecio(15.99);
                    prod2.setCantidad(30);
                    prod2.setFK_estanteriaId(1L);
                    prodDao.insertProducto(prod2);

                    ProductoEntity prod3 = new ProductoEntity();
                    prod3.setId(3L);
                    prod3.setNombre("Auriculares Bluetooth");
                    prod3.setPrecio(29.99);
                    prod3.setCantidad(20);
                    prod3.setFK_estanteriaId(1L);
                    prodDao.insertProducto(prod3);

                    // Estanteria 2 - Herramientas
                    ProductoEntity prod4 = new ProductoEntity();
                    prod4.setId(4L);
                    prod4.setNombre("Destornillador Phillips");
                    prod4.setPrecio(3.50);
                    prod4.setCantidad(100);
                    prod4.setFK_estanteriaId(2L);
                    prodDao.insertProducto(prod4);

                    ProductoEntity prod5 = new ProductoEntity();
                    prod5.setId(5L);
                    prod5.setNombre("Llave inglesa");
                    prod5.setPrecio(12.00);
                    prod5.setCantidad(25);
                    prod5.setFK_estanteriaId(2L);
                    prodDao.insertProducto(prod5);

                    ProductoEntity prod6 = new ProductoEntity();
                    prod6.setId(6L);
                    prod6.setNombre("Cinta metrica 5m");
                    prod6.setPrecio(4.99);
                    prod6.setCantidad(40);
                    prod6.setFK_estanteriaId(2L);
                    prodDao.insertProducto(prod6);

                    // Estanteria 3 - Limpieza
                    ProductoEntity prod7 = new ProductoEntity();
                    prod7.setId(7L);
                    prod7.setNombre("Detergente 2L");
                    prod7.setPrecio(6.50);
                    prod7.setCantidad(60);
                    prod7.setFK_estanteriaId(3L);
                    prodDao.insertProducto(prod7);

                    ProductoEntity prod8 = new ProductoEntity();
                    prod8.setId(8L);
                    prod8.setNombre("Escoba industrial");
                    prod8.setPrecio(8.99);
                    prod8.setCantidad(15);
                    prod8.setFK_estanteriaId(3L);
                    prodDao.insertProducto(prod8);

                    // Estanteria 4 - Oficina
                    ProductoEntity prod9 = new ProductoEntity();
                    prod9.setId(9L);
                    prod9.setNombre("Boligrafos pack 10");
                    prod9.setPrecio(2.99);
                    prod9.setCantidad(200);
                    prod9.setFK_estanteriaId(4L);
                    prodDao.insertProducto(prod9);

                    ProductoEntity prod10 = new ProductoEntity();
                    prod10.setId(10L);
                    prod10.setNombre("Cuaderno A4");
                    prod10.setPrecio(1.50);
                    prod10.setCantidad(150);
                    prod10.setFK_estanteriaId(4L);
                    prodDao.insertProducto(prod10);

                    android.util.Log.d("AppDatabase", "10 Productos insertados");

                    // === OPERACIONES DE EJEMPLO ===
                    OperacionDao operacionDao = operacionDao();

                    OperacionEntity operacion1 = new OperacionEntity();
                    operacion1.setId(1L);
                    operacion1.setTimestamp(System.currentTimeMillis());
                    operacion1.setTipoOperacion(TipoOperacion.ADD.getValor());
                    operacion1.setProductoId(1L);
                    operacion1.setEstanteriaId(1L);
                    operacion1.setCantidad(10);
                    operacion1.setEstado(EstadoOperacion.ENVIADA.getValor());
                    operacionDao.agregarOperacion(operacion1);

                    android.util.Log.d("AppDatabase", "Operaciones de ejemplo insertadas");
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
