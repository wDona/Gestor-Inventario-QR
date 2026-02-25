package dev.wdona.gestorinventarioqr.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.OperacionEntity;

@Dao
public interface OperacionDao {
        @Query("SELECT * FROM Operacion_pendiente WHERE id = :id")
        OperacionEntity getOperacionPendienteById(Long id);
        @Query("UPDATE Operacion_pendiente" +
                " SET estado = :nuevoEstado WHERE id = :id")
        void actualizarEstadoById(Long id, String nuevoEstado);

        @Query("SELECT * FROM Operacion_pendiente WHERE estado = 'PENDIENTE' OR estado = 'FALLIDA' ")
        List<OperacionEntity> getOperacionesPendientesSinEnviar();

        @Query("SELECT id FROM Operacion_pendiente ORDER BY id DESC LIMIT 1")
        Long getUltimoIdOperacionPendiente();

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void agregarOperacion(OperacionEntity operacion);

        @Query("SELECT * FROM Operacion_pendiente")
        List<OperacionEntity> getAllOperaciones();

        @Query("SELECT * FROM Operacion_pendiente WHERE estado = :estado")
        List<OperacionEntity> getOperacionesPorEstado(String estado);

        @Query("DELETE FROM Operacion_pendiente WHERE estado = :estado")
        void eliminarOperacionesPorEstado(String estado);
}
