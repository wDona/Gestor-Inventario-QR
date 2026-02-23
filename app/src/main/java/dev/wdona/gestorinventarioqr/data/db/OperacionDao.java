package dev.wdona.gestorinventarioqr.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import dev.wdona.gestorinventarioqr.data.entity.OperacionEntity;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;

@Dao
public interface OperacionDao {
        @Query("SELECT * FROM Operacion_pendiente WHERE id = :id")
        OperacionEntity getOperacionPendienteById(Long id);
        @Query("UPDATE Operacion_pendiente" +
                " SET estado = :nuevoEstado WHERE id = :id")
        void actualizarEstadoById(Long id, String nuevoEstado);

        @Query("SELECT * FROM Operacion_pendiente WHERE estado = 'PENDIENTE'")
        List<OperacionEntity> getOperacionesPendientesSinEnviar();

        @Query("SELECT id FROM Operacion_pendiente ORDER BY id DESC LIMIT 1")
        Long getUltimoIdOperacionPendiente();

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void agregarOperacionPendiente(OperacionEntity operacion);

        @Query("SELECT * FROM Operacion_pendiente")
        List<OperacionEntity> getTodasLasOperaciones();

        @Query("SELECT * FROM Operacion_pendiente WHERE estado = :estado")
        List<OperacionEntity> getOperacionesPorEstado(String estado);
}
