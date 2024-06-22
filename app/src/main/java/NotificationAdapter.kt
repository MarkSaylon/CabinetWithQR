import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.NotifLogBinding

class NotificationAdapter(private val logs: List<DoorLog>) : RecyclerView.Adapter<NotificationAdapter.NotifHolder>() {

    inner class NotifHolder(private val binding:NotifLogBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(log:DoorLog){
            binding.tvTime.text = log.time
            binding.tvNotif.text = log.doorState
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifHolder {
        val binding = NotifLogBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NotifHolder(binding)
    }

    override fun onBindViewHolder(holder: NotifHolder, position:Int){
        holder.bind(logs[position])
    }

    override fun getItemCount():Int {
        return logs.size
    }
}