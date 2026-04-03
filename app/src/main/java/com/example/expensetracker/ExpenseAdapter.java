package com.example.expensetracker;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private ArrayList<Expense> expenses;
    private int longClickedPosition;

    public ExpenseAdapter(ArrayList<Expense> expenses) {
        this.expenses = expenses;
    }

    public int getLongClickedPosition() {
        return longClickedPosition;
    }

    public void setLongClickedPosition(int longClickedPosition) {
        this.longClickedPosition = longClickedPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.expenseName.setText(expense.getName());
        holder.expenseAmount.setText(String.format("-$%.2f", expense.getAmount()));
        holder.itemView.setOnLongClickListener(v -> {
            setLongClickedPosition(holder.getAdapterPosition());
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView expenseName;
        public TextView expenseAmount;

        public ViewHolder(View itemView) {
            super(itemView);
            expenseName = itemView.findViewById(R.id.expense_name);
            expenseAmount = itemView.findViewById(R.id.expense_amount);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(Menu.NONE, 1, 1, "Edit");
            menu.add(Menu.NONE, 2, 2, "Delete");
        }
    }
}
