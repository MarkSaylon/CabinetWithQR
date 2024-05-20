
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemLogBinding
import java.io.File

class LogAdapter(private val logs: List<CabinetLog>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    inner class LogViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(log: CabinetLog) {
            binding.tvTime.text = log.time
            binding.tvState.text = log.state
            binding.tvCabinetName.text = log.cabinetName

            // Load the image using Glide or Picasso
            if (log.imageLink != null) {
                Glide.with(binding.ivImage.context)
                    .load(Uri.fromFile(File(log.imageLink)))
                    .into(binding.ivImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int {
        return logs.size
    }
}