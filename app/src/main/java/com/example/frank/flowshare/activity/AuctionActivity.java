package com.example.frank.flowshare.activity;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.frank.flowshare.R;

public class AuctionActivity extends AppCompatActivity {

    private EditText ftotal,ftime,fprice,fother;
    private Button fpublish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction);
        init();
    }

    private void init(){
        ftotal=(EditText)this.findViewById(R.id.editText);
        ftotal=(EditText)this.findViewById(R.id.editText2);
        ftotal=(EditText)this.findViewById(R.id.editText3);
        ftotal=(EditText)this.findViewById(R.id.editText4);
        fpublish=(Button)this.findViewById(R.id.button);

        Toast.makeText(AuctionActivity.this,"请手动打开—移动流量开关，否则服务无法提供！",Toast.LENGTH_LONG).show();
        fpublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AuctionActivity.this )
                        .setTitle("系统提示")
                        .setMessage("\n发布成功！")
                        .setPositiveButton("确定",null)
                        .show();
            }
        });
    }
}
