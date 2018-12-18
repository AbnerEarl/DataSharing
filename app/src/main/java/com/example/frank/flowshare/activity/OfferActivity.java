package com.example.frank.flowshare.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.frank.flowshare.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfferActivity extends AppCompatActivity {
    private MyAdapter offerAdapter;
    private ListView lv_flow_pack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);

        init();
    }

    private void init(){

        lv_flow_pack=(ListView)this.findViewById(R.id.lv_flow_pack);
        offerAdapter = new MyAdapter(this);//得到一个MyAdapter对象
        lv_flow_pack.setAdapter(offerAdapter);//为ListView绑定Adapter

        lv_flow_pack.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                String mess="\n总计流量："+offerAdapter.listItem.get(arg2).get("ItemTotal").toString()+
                        "MB\n使用时长："+offerAdapter.listItem.get(arg2).get("ItemTime").toString()+
                        "分钟\n最低价格："+offerAdapter.listItem.get(arg2).get("ItemPrice").toString()+
                        "元\n其他说明："+offerAdapter.listItem.get(arg2).get("ItemOther").toString();
                new AlertDialog.Builder(OfferActivity.this )
                        .setTitle("流量套餐详情")
                        .setMessage(mess)
                        .setPositiveButton("确定",null)
                        .show();

            }
        });

        HashMap<String,Object> map=new HashMap<>();
        map.put("ItemTotal","50");
        map.put("ItemTime","30");
        map.put("ItemPrice","5");
        map.put("ItemOther","请在规定时间内使用完。");
        offerAdapter.listItem.add(map);

        HashMap<String,Object> map2=new HashMap<>();
        map2.put("ItemTotal","50");
        map2.put("ItemTime","30");
        map2.put("ItemPrice","6");
        map2.put("ItemOther","请在规定时间内使用完。");
        offerAdapter.listItem.add(map2);

        HashMap<String,Object> map3=new HashMap<>();
        map3.put("ItemTotal","100");
        map3.put("ItemTime","60");
        map3.put("ItemPrice","8");
        map3.put("ItemOther","请在规定时间内使用完。");
        offerAdapter.listItem.add(map3);
        offerAdapter.notifyDataSetChanged();
    }

    private class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局

        ArrayList<HashMap<String, Object>> listItem = new ArrayList<>();
        /*构造函数*/
        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public int getCount() {

            return listItem.size();//返回数组的长度
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
        /*书中详细解释该方法*/
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final PermissViewHolder holder;
            //观察convertView随ListView滚动情况
            Log.v("MyListViewBase", "getView " + position + " " + convertView);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.offer_layout,null);
                holder = new PermissViewHolder();
                /*得到各个控件的对象*/

                holder.ftotal = (TextView) convertView.findViewById(R.id.textView9);
                holder.ftime = (TextView) convertView.findViewById(R.id.textView11);
                holder.fprice = (TextView) convertView.findViewById(R.id.textView12);
                holder.fother = (TextView) convertView.findViewById(R.id.textView13);
                holder.fauction=(Button)convertView.findViewById(R.id.button2);

                convertView.setTag(holder);//绑定ViewHolder对象
            }
            else{
                holder = (PermissViewHolder)convertView.getTag();//取出ViewHolder对象
            }

            holder.ftotal.setText(listItem.get(position).get("ItemTotal").toString()+" MB");
            holder.ftime.setText(listItem.get(position).get("ItemTime").toString()+" min");
            holder.fprice.setText(listItem.get(position).get("ItemPrice").toString()+" ¥");
            holder.fother.setText(listItem.get(position).get("ItemOther").toString());
            holder.fauction.setTag(position);

            holder.fauction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(OfferActivity.this,PayActivity.class);
                    startActivity(intent);
                }
            });

            return convertView;
        }

    }
    /*存放控件*/
    public final class PermissViewHolder{
        public TextView ftotal,ftime,fprice,fother;
        public Button fauction;

    }


}
