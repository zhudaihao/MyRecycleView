package cn.zdh.myrecycleview.view;

import android.view.View;

import java.util.Stack;

/**
 * 回收池
 * 使用栈存储
 */
public class Recycler {
    //回收池 的容器 存储所有回收了的itemView；
    private Stack<View>[] views;


    public Recycler(int itemViewTypeCount) {
        //根据itemView类型数量 创建数组
        views = new Stack[itemViewTypeCount];
        //初始化数组总的每一个stack
        for (int i = 0; i < itemViewTypeCount; i++) {
            views[i] = new Stack<>();
        }
    }

    /**
     * 存
     */
    public void put(View itemView, int viewType) {
        views[viewType].push(itemView);
    }


    /**
     * 读
     */
    public View get(int viewType) {
        try {
            return views[viewType].pop();
        } catch (Exception e) {
            return null;
        }

    }


}
