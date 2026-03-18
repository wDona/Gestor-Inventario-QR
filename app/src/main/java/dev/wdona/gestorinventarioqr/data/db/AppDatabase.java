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
import dev.wdona.gestorinventarioqr.data.entity.ProductoEstanteriaEntity;

@Database(entities = {ProductoEntity.class, EstanteriaEntity.class, OperacionEntity.class, ProductoEstanteriaEntity.class}, version = 12, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductoDao productoDao();
    public abstract EstanteriaDao estanteriaDao();
    public abstract OperacionDao operacionDao();
    public abstract ProductoEstanteriaDao productoEstanteriaDao();

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
                    est1.setId("EST-1");
                    est1.setNombre("Estanteria A - Electronica");
                    estanteriaDao.insertEstanteria(est1);

                    EstanteriaEntity est2 = new EstanteriaEntity();
                    est2.setId("EST-2");
                    est2.setNombre("Estanteria B - Herramientas");
                    estanteriaDao.insertEstanteria(est2);

                    EstanteriaEntity est3 = new EstanteriaEntity();
                    est3.setId("EST-3");
                    est3.setNombre("Estanteria C - Limpieza");
                    estanteriaDao.insertEstanteria(est3);

                    EstanteriaEntity est4 = new EstanteriaEntity();
                    est4.setId("EST-4");
                    est4.setNombre("Estanteria D - Oficina");
                    estanteriaDao.insertEstanteria(est4);

                    android.util.Log.d("AppDatabase", "4 Estanterias insertadas");

                    // === PRODUCTOS (solo datos base, sin cantidad ni estantería) ===
                    ProductoDao prodDao = productoDao();
                    ProductoEstanteriaDao peDao = productoEstanteriaDao();

                    ProductoEntity prod1 = new ProductoEntity();
                    prod1.setId("PROD-1");
                    prod1.setNombre("Cable USB-C");
                    prod1.setPrecio(5.99);
                    prodDao.insertProducto(prod1);

                    ProductoEntity prod2 = new ProductoEntity();
                    prod2.setId("PROD-2");
                    prod2.setNombre("Cargador 20W");
                    prod2.setPrecio(15.99);
                    prodDao.insertProducto(prod2);

                    ProductoEntity prod3 = new ProductoEntity();
                    prod3.setId("PROD-3");
                    prod3.setNombre("Auriculares Bluetooth");
                    prod3.setPrecio(29.99);
                    prodDao.insertProducto(prod3);

                    ProductoEntity prod4 = new ProductoEntity();
                    prod4.setId("PROD-4");
                    prod4.setNombre("Destornillador Phillips");
                    prod4.setPrecio(3.50);
                    prodDao.insertProducto(prod4);

                    ProductoEntity prod5 = new ProductoEntity();
                    prod5.setId("PROD-5");
                    prod5.setNombre("Llave inglesa");
                    prod5.setPrecio(12.00);
                    prodDao.insertProducto(prod5);

                    ProductoEntity prod6 = new ProductoEntity();
                    prod6.setId("PROD-6");
                    prod6.setNombre("Cinta metrica 5m");
                    prod6.setPrecio(4.99);
                    prodDao.insertProducto(prod6);

                    ProductoEntity prod7 = new ProductoEntity();
                    prod7.setId("PROD-7");
                    prod7.setNombre("Detergente 2L");
                    prod7.setPrecio(6.50);
                    prodDao.insertProducto(prod7);

                    ProductoEntity prod8 = new ProductoEntity();
                    prod8.setId("PROD-8");
                    prod8.setNombre("Escoba industrial");
                    prod8.setPrecio(8.99);
                    prodDao.insertProducto(prod8);

                    ProductoEntity prod9 = new ProductoEntity();
                    prod9.setId("PROD-9");
                    prod9.setNombre("Boligrafos pack 10");
                    prod9.setPrecio(2.99);
                    prodDao.insertProducto(prod9);

                    ProductoEntity prod10 = new ProductoEntity();
                    prod10.setId("PROD-10");
                    prod10.setNombre("Cuaderno A4");
                    prod10.setPrecio(1.50);
                    prodDao.insertProducto(prod10);

                    android.util.Log.d("AppDatabase", "11 Productos insertados");

                    // === RELACIONES ProductoEstanteria (producto-estanteria-cantidad) ===
                    insertarRelacion(peDao, "PROD-1", "EST-1", 50);  // Cable USB-C → Estanteria A: 50
                    insertarRelacion(peDao, "PROD-2", "EST-1", 30);  // Cargador 20W → Estanteria A: 30
                    insertarRelacion(peDao, "PROD-3", "EST-1", 20);  // Auriculares → Estanteria A: 20
                    insertarRelacion(peDao, "PROD-4", "EST-2", 100); // Destornillador → Estanteria B: 100
                    insertarRelacion(peDao, "PROD-5", "EST-2", 25);  // Llave inglesa → Estanteria B: 25
                    insertarRelacion(peDao, "PROD-6", "EST-2", 40);  // Cinta metrica → Estanteria B: 40
                    insertarRelacion(peDao, "PROD-7", "EST-3", 60);  // Detergente → Estanteria C: 60
                    insertarRelacion(peDao, "PROD-8", "EST-3", 15);  // Escoba → Estanteria C: 15
                    insertarRelacion(peDao, "PROD-9", "EST-4", 200); // Boligrafos → Estanteria D: 200
                    insertarRelacion(peDao, "PROD-10", "EST-4", 150);// Cuaderno → Estanteria D: 150

                    android.util.Log.d("AppDatabase", "Relaciones ProductoEstanteria insertadas");

                    // === OPERACIONES DE EJEMPLO ===
                    OperacionDao operacionDao = operacionDao();
                    dev.wdona.gestorinventarioqr.data.entity.OperacionEntity operacion1 = new dev.wdona.gestorinventarioqr.data.entity.OperacionEntity();
                    operacion1.setId(1L);
                    operacion1.setTimestamp(System.currentTimeMillis());
                    operacion1.setTipoOperacion(TipoOperacion.ADD.getValor());
                    operacion1.setProductoId("PROD-1");
                    operacion1.setEstanteriaId("EST-1");
                    operacion1.setCantidad(10);
                    operacion1.setEstado(EstadoOperacion.ENVIADA.getValor());
                    operacionDao.agregarOperacion(operacion1);

                    android.util.Log.d("AppDatabase", "Datos iniciales insertados correctamente");
                } else {
                    android.util.Log.d("AppDatabase", "Ya existen datos, no se insertan nuevos");
                }
            } catch (Exception e) {
                android.util.Log.e("AppDatabase", "Error en populateInitialData: " + e.getMessage(), e);
            }
        });
    }

    private void insertarRelacion(ProductoEstanteriaDao dao, String productoId, String estanteriaId, int cantidad) {
        ProductoEstanteriaEntity pe = new ProductoEstanteriaEntity();
        pe.setProductoId(productoId);
        pe.setEstanteriaId(estanteriaId);
        pe.setCantidad(cantidad);
        dao.insertar(pe);
    }
}
