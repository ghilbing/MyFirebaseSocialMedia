package com.hilbing.myfirebasesocialmedia.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hilbing.myfirebasesocialmedia.R;
import com.hilbing.myfirebasesocialmedia.activities.ChatActivity;
import com.hilbing.myfirebasesocialmedia.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<ModelUser> userList;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_user.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();
        //set data
        holder.nameTv.setText(userName);
        holder.emailTv.setText(userEmail);
        try{
            Glide.with(context).load(userImage).centerCrop().placeholder(R.drawable.ic_default_image).into(holder.avatarIv);
           // Picasso.get().load(userImage).fit().centerInside().placeholder(R.drawable.ic_default_image).into(holder.avatarIv);
        }catch (Exception e){

        }
        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(context, userEmail, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUID", hisUID);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.avatar_user_iv)
        CircleImageView avatarIv;
        @BindView(R.id.name_user_tv)
        TextView nameTv;
        @BindView(R.id.email_user_tv)
        TextView emailTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
