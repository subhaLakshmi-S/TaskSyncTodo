package com.todo.todolist

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.todo.todolist.data.Todo
import com.todo.todolist.data.TodoDao
import com.todo.todolist.databinding.AddCardBinding

class CardAdapter(private val listener: TaskActionListener,
                  private val viewModel: TodoViewModel):
ListAdapter<Todo, CardAdapter.CardViewHolder>(TodoDiffCallback()){

        inner class CardViewHolder(val binding: AddCardBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent:ViewGroup,viewType: Int): CardViewHolder {
            val binding= AddCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return CardViewHolder(binding)
        }
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            val card=getItem(position)
            val cardHolder=holder.binding
            cardHolder.cardHeading.text=card.title
            cardHolder.cardContent.text= Html.fromHtml(card.content,Html.FROM_HTML_MODE_LEGACY)
            cardHolder.cardDate.text="From ${card.startDate} to ${card.endDate}"
            cardHolder.priorityIcon.visibility = if(card.priority == "High") View.VISIBLE else View.GONE

            cardHolder.favIcon.setImageResource(
                if(card.isFavorite) R.drawable.baseline_star_24 else R.drawable.baseline_star_outline_24)
            cardHolder.favIcon.setOnClickListener{
                val updatedCard = card.copy(isFavorite = !card.isFavorite)
                viewModel.update(updatedCard)
                notifyItemChanged(position)
            }
            cardHolder.doneIcon.visibility = if(card.isCompleted) View.VISIBLE else View.GONE

            cardHolder.optionMenu.setOnClickListener {
                val popup = PopupMenu(holder.itemView.context, cardHolder.optionMenu)
                popup.menuInflater.inflate(R.menu.card_menu, popup.menu)

                val completeMenuItem = popup.menu.findItem(R.id.action_complete)
                completeMenuItem.title = if(card.isCompleted) "Unmark as Complete" else "Mark as completed"

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            listener.onEditTask(card)
                        }
                        R.id.action_delete -> {
                            viewModel.delete(card)
                            Snackbar.make(holder.itemView, "Task deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    viewModel.insert(card)
                                }.show()
                        }
                        R.id.action_complete -> {
                            val updatedCard = card.copy(isCompleted = !card.isCompleted)
                            viewModel.update(updatedCard)
                            notifyItemChanged(position)
                            return@setOnMenuItemClickListener true
                        }
                    }
                    true
                }
                popup.show()
            }
        }
}
class TodoDiffCallback : DiffUtil.ItemCallback<Todo>() {
    override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem == newItem
    }
}
interface TaskActionListener{
    fun onEditTask(todo: Todo)
}


