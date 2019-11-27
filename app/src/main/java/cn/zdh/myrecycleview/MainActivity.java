package cn.zdh.myrecycleview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.zdh.myrecycleview.view.MyRecyclerView;

public class MainActivity extends AppCompatActivity {
    private MyRecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();


    }


    private void init() {
        rv = findViewById(R.id.rv);
        rv.setAdapter(new MyRecyclerView.Adapter() {

            @Override
            public View onCreateViewHolder(int position, View convertView, ViewGroup parent) {
                //创建itemView 注意上参数方法
                convertView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
                //设置数据
                TextView textView = convertView.findViewById(R.id.tv);
                textView.setText("自定义recycleView的item" + position);

//                Log.e("zdh", "----------------打印hashCode" + textView.hashCode());
                return convertView;
            }

            @Override
            public View onBinderViewHolder(int position, View convertView, ViewGroup parent) {
                //设置数据
                TextView textView = convertView.findViewById(R.id.tv);
                textView.setText("自定义recycleView的item" + position);

                Log.e("zdh", "----------------打印hashCode" + textView.hashCode());

                return convertView;
            }

            @Override
            public int getItemViewType(int itemType) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getCount() {
                return 40;
            }

            @Override
            public int getHeight(int index) {
                return 100;
            }
        });
    }


}
