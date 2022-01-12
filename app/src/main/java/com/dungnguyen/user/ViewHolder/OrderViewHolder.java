package com.dungnguyen.user.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dungnguyen.user.Interface.ItemClickListener;
import com.dungnguyen.user.R;


public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtGmail,txtTotal, txtRestaurantPhone;
    private ItemClickListener itemClickListener;
    public Button btnReport, btnCancel, btnOrderComplete;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtGmail = itemView.findViewById(R.id.order_gmail);
        txtTotal = itemView.findViewById(R.id.order_total);
        txtRestaurantPhone = itemView.findViewById(R.id.txtRestaurantPhone);
        btnReport = itemView.findViewById(R.id.btnReport);
        btnCancel = itemView.findViewById(R.id.btnCancel);
        btnOrderComplete = itemView.findViewById(R.id.btnOrderComplete);
      //  itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
