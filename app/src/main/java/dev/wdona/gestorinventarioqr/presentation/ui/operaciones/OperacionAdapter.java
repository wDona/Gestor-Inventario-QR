package dev.wdona.gestorinventarioqr.presentation.ui.operaciones;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dev.wdona.gestorinventarioqr.R;
import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public class OperacionAdapter extends RecyclerView.Adapter<OperacionAdapter.ViewHolder> {

    private List<Operacion> operaciones = new ArrayList<>();
    private OnOperacionClickListener listener;

    public interface OnOperacionClickListener {
        void onReintentarClick(Operacion operacion);
    }

    public OperacionAdapter(OnOperacionClickListener listener) {
        this.listener = listener;
    }

    public void setOperaciones(List<Operacion> operaciones) {
        this.operaciones = operaciones != null ? operaciones : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_operacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Operacion operacion = operaciones.get(position);
        holder.bind(operacion);
    }

    @Override
    public int getItemCount() {
        return operaciones.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTipoOperacion;
        private final TextView tvProductoId;
        private final TextView tvEstanteriaId;
        private final TextView tvCantidad;
        private final TextView tvEstado;
        private final TextView tvTimestamp;
        private final Button btnReintentar;
        private final View estadoIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipoOperacion = itemView.findViewById(R.id.tvTipoOperacion);
            tvProductoId = itemView.findViewById(R.id.tvProductoId);
            tvEstanteriaId = itemView.findViewById(R.id.tvEstanteriaId);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnReintentar = itemView.findViewById(R.id.btnReintentar);
            estadoIndicator = itemView.findViewById(R.id.estadoIndicator);
        }

        void bind(Operacion operacion) {
            // Tipo de operación
            String tipoTexto = getTipoOperacionTexto(operacion.getTipoOperacion());
            tvTipoOperacion.setText(tipoTexto);

            // IDs
            tvProductoId.setText("Producto ID: " + operacion.getProductoId());
            tvEstanteriaId.setText("Estantería ID: " + (operacion.getEstanteriaId() != null ? operacion.getEstanteriaId() : "-"));

            // Cantidad (solo mostrar si es relevante)
            if (operacion.getCantidad() > 0) {
                tvCantidad.setText("Cantidad: " + operacion.getCantidad());
                tvCantidad.setVisibility(View.VISIBLE);
            } else {
                tvCantidad.setVisibility(View.GONE);
            }

            // Estado con color
            String estado = operacion.getEstado();
            tvEstado.setText(estado);

            int colorEstado;
            if (EstadoOperacion.PENDIENTE.getValor().equals(estado)) {
                colorEstado = Color.parseColor("#FFA500"); // Naranja
                btnReintentar.setVisibility(View.VISIBLE);
            } else if (EstadoOperacion.ENVIADA.getValor().equals(estado)) {
                colorEstado = Color.parseColor("#4CAF50"); // Verde
                btnReintentar.setVisibility(View.GONE);
            } else if (EstadoOperacion.FALLIDA.getValor().equals(estado)) {
                colorEstado = Color.parseColor("#F44336"); // Rojo
                btnReintentar.setVisibility(View.VISIBLE);
            } else {
                colorEstado = Color.GRAY;
                btnReintentar.setVisibility(View.GONE);
            }

            tvEstado.setTextColor(colorEstado);
            if (estadoIndicator != null) {
                estadoIndicator.setBackgroundColor(colorEstado);
            }

            // Timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String fecha = sdf.format(new Date(operacion.getTimestamp()));
            tvTimestamp.setText(fecha);

            // Botón reintentar
            btnReintentar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReintentarClick(operacion);
                }
            });
        }

        private String getTipoOperacionTexto(String tipo) {
            if (tipo == null) return "Desconocido";
            switch (tipo) {
                case "ADD":
                    return "➕ Añadir unidades";
                case "REMOVE":
                    return "➖ Quitar unidades";
                case "ASSIGN":
                case "MOVE":
                    return "📦 Mover a estantería";
                default:
                    return tipo;
            }
        }
    }
}
