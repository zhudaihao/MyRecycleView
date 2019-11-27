package cn.zdh.myrecycleview.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.zdh.myrecycleview.R;

/**
 * 手写recyclerView 实现核心功能
 */
public class MyRecyclerView extends ViewGroup implements NestedScrollingChild2 {
    //适配器
    private Adapter myAdapter;
    //当前显示的view的集合
    private List<View> viewList;
    //当前滑动的Y值
    private int currentY;
    //总行数（适配器的总行数）
    private int rowCount;
    //显示在屏幕上第一行 ，对应数据源是第几个
    private int firstRow;
    //Y偏移量 滑动Y的距离
    private int scrollY;
    //初始化  是否是第一屏（记录是否需要重绘）
    private boolean needRelayout;
    //当前recyclerView的宽度
    private int width;
    //当前recyclerView的高度
    private int height;
    //所有itemView的高度数组
    private int[] heights;
    //View对象 回收池
    private Recycler recycler;
    //最小滑动距离 （当你滑动距离超过这个值我才做滑动逻辑）
    private int touchSlop;

    //嵌套滑动帮助类
    private NestedScrollingChildHelper nestedScrollingChildHelper;


    public MyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * 初始化RecyclerViw
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        //获取手指最小滑动距离
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        //在我们认为用户在滚动之前，触摸可以移动 的距离
        touchSlop = viewConfiguration.getScaledTouchSlop();
        //初始化ViewList
        viewList = new ArrayList<>();
        //是否重新布局
        needRelayout = true;

        //初始化嵌套滑动帮助类
        nestedScrollingChildHelper =new NestedScrollingChildHelper(this);

    }

    /**
     * -----------------------------------itemView布局的 测量 摆放-----------------------------------
     */


    /**
     * 布局测量
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取recycleView在当前窗体的宽高
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //当前recycleView内容的高度
        int h = 0;
        //判断适配器是否为空
        if (myAdapter != null) {
            //获取当前数据的总条数
            rowCount = myAdapter.getCount();
            //根据itemView总条数 创建所有itemView的高 数组
            heights = new int[rowCount];
            //循环获取到所有itemView的高度 保存到数组
            for (int i = 0; i < heights.length; i++) {
                heights[i] = myAdapter.getHeight(i);
            }
        }

        //获取recycleView内的高度(比如获取 从itemView第一个到第四个 的高度)
        int temHeight = sumArray(heights, 0, heights.length);
        //对比recycleView内的高度和recycle的高度，哪个小我就以哪个高度绘制
        h = Math.min(temHeight, heightSize);
        //设置需要绘制的宽高
        setMeasuredDimension(widthSize, h);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    /**
     * @param heights 所有itemView高度集合
     * @param index   从哪个 item开始拿
     * @param count   一共需要拿多少个item高度
     * @return 计算出的距离--》比如 sumArray（heights，0,3） ：获取itemView从0到3的距离，包括item0的高度，不包括item3的高度
     * 如果heights[0]=10,heights[1]=20,heights[2]=30,,heights[3]=40, 最后计算出的高度为60；
     */
    private int sumArray(int[] heights, int index, int count) {
        int sun = 0;
        count = count + index;
        for (int i = index; i < count; i++) {
            sun = sun + heights[i];
        }
        return sun;
    }

    /**
     * 布局摆放
     *
     * @param changed true表示大小或者位置发生改变
     * @param l       左
     * @param t       上
     * @param r       右
     * @param b       下
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //判断是否需要布局 或者 布局发生改变时候 重新布局
        if (needRelayout || changed) {
            needRelayout = false;
            //清除显示集合
            viewList.clear();
            //不知道 是否是第一次加载 所有先清除掉所有的view
            removeAllViews();

            //如果适配器 不为空，我们就对itemView进行摆放
            if (myAdapter != null) {
                //获取到recycle宽高
                width = r - l;
                height = b - t;
                //定义布局itemView的四个变量
                int top = 0, right, bottom, left = 0;

                //变量总行数
                for (int i = 0; i < rowCount; i++) {
                    //获取绘制的宽度的最右边--》r
                    right = width;
                    bottom = top + heights[i];
                    //生成view
                    View view = makeAndStep(i, left, top, right, bottom);
                    //添加当前itemView到集合中
                    viewList.add(view);

                    //注意： 循环摆放 下一个item的top就是上一个item的bottom
                    top = bottom;
                }

            }


        }


    }

    /**
     * 创建view的方法
     *
     * @param i
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    private View makeAndStep(int i, int left, int top, int right, int bottom) {
        //生成view
        View view = obtainView(i, right - left, bottom - top);
        //摆放itemView
        view.layout(left, top, right, bottom);

        return view;
    }

    /**
     * 生成view
     *
     * @param row    适配器 item条目的position
     * @param width  父布局的宽-->recycleView的宽
     * @param height 父布局的--》recycleView的高
     * @return
     */
    private View obtainView(int row, int width, int height) {
        //先从 回收池获取
        int itemViewType = myAdapter.getItemViewType(row);
        //从栈中获取
        View view = recycler.get(itemViewType);
        //定义个adapter创建的view
        View itemView = null;
        //如果栈中view是空 就adapter创建
        if (view == null) {
            //适配创建itemView
            itemView = myAdapter.onCreateViewHolder(row, itemView, this);
            if (itemView == null) {
                //抛异常
                new RuntimeException("onCreateViewHolder  不能为空");
            }
        } else {
            //当栈的view不为空，我们就通过调用adapter的绑定方法获取itemView
            //因为栈获取的view是没有数据的itemView，
            //（所谓数据--》比如itemView里面的有imageView需要设置图片数据。。。）
            itemView = myAdapter.onBinderViewHolder(row, view, this);
        }

        //为每个itemView设置一个tag(键是tag_type_view即id，值是itemView类型)
        itemView.setTag(R.id.tag_type_view, itemViewType);
        /**
         * 测量每个itemView
         */
        itemView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
                , MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        //没生成一个itemView都添加进RecycleView
        addView(itemView);


        return itemView;
    }


    /**
     * -----------------------------------滑动处理------------------------------------------
     */

    /**
     * 判断哪些滑动需要 拦截处理
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录手机按下Y坐标
                currentY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                //获取移动后点Y坐标 和 开始按下Y坐标 之间的差值 的绝对正值
                float rawY = Math.abs(ev.getRawY() - currentY);
                //如果滑动的距离 小于最小滑动距离就不 做滑动处理
                if (rawY > touchSlop) {
                    intercept = true;
                }

                break;
        }

        return intercept;


    }


    /**
     * 滑动处理
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //获取当前移动到点Y值
                float rawY = event.getRawY();
                // 获取滑动距离=手指触摸点Y 减去 滑动后点Y
                float diffY = currentY - rawY;

                //设置移动后的点为新的的起点  不加会影响反应速度
                currentY = (int) rawY;

                //滑动方法
                scrollBy(0, (int) diffY);

                //对应状态 调用接口NestedScrollingChild2对应方法
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL,ViewCompat.TYPE_TOUCH);
                break;

            case MotionEvent.ACTION_UP:
                //对应状态 调用接口NestedScrollingChild2对应方法
                stopNestedScroll(ViewCompat.TYPE_TOUCH);
                break;
        }


        return super.onTouchEvent(event);

    }

    /**
     * 滑动逻辑处理
     *
     * @param x 滑动Y距离
     * @param y 滑动Y距离
     */
    @Override
    public void scrollBy(int x, int y) {
        scrollY += y;
        //防止数组越界 需要纠正scrollY
        scrollY = scrollBounds(scrollY);

        if (scrollY > 0) {
            //上滑 要做两件事情 1：将上面的移除掉，给下面添加一个新的进来。
            /**
             *
             * 移除上面滑出的itemView (防止延迟 使用while循环)
             *
             */
            //当滑动距离大于 第一个显示的itemView高度，就删除当前显示的第一个
            while (scrollY > heights[firstRow]) {
                removeView(viewList.remove(0));
                //改变scrollY
                scrollY = scrollY - heights[firstRow];
                //改变当前显示position
                firstRow++;
            }


            /**
             * 添加下面滑入的itemView (防止延迟 使用while循环)
             */

            //判断当前所显示的所有itemView高度，是否小于recyclerView的高度，如果小于 就添加新的itemView
            while (getFillHeight() < height) {
                //计算添加的新的itemView  在数据源中的position
                int addLast = firstRow + viewList.size();
                //获取itemView
                View view = obtainView(addLast, width, heights[addLast]);
                //把新的itemView添加进viewList里面
                viewList.add(view);
//                Log.e("zdh", "--------------getFillHeight() " + getFillHeight() + "-------------height " + height);

            }


        } else if (scrollY < 0) {
            //下滑 过程中要做两件事 1移除下面滑出的itemView 2在上面添加新的itemView

            /**
             * 添加一个新的itemView在最上面
             */
            //当recyclerView滑动到最下面时 X=0,Y=0;所有scrollY不能大于0
            while (scrollY < 0) {
                //记录当前显示itemView的第一行 的上一行
                int firstAddRow = firstRow - 1;
                //获取显示在第一行的itemView的上一行itemView
                View view = obtainView(firstAddRow, width, heights[firstAddRow]);
                //添加到集合第一个
                viewList.add(0, view);
                //更新第一行的position
                firstRow--;
                //改变scrollY值 需要添加新添加的itemView的高度
                scrollY += heights[firstAddRow];
            }

            /**
             * 移除 滑出的itemView
             */
            //判断当前显示的所有itemView 的高度 是否大于recyclerView ，如果大于就把嘴下面的itemView移除
            //计算显示的所有itemView高度
            while (sumArray(heights, firstRow, viewList.size()) - scrollY - heights[firstRow + viewList.size() - 1] >= height) {
                //移除当前显示的最后一个itemView
                removeView(viewList.remove(viewList.size() - 1));
            }

        } else {
            //等于0
        }

        /**
         * 重新摆放itemView的位置
         */
        rePositionView();
//        super.scrollBy(x, y);

    }

    /**
     * 纠正scrollY 防止数组越界
     *
     * @param scrollY
     * @return
     */
    private int scrollBounds(int scrollY) {
        if (scrollY > 0) {
            //上滑 判断上拉过程 显示的最后一个itemView未滑出距离  和scrollY大小比较，(防止scrollY大于显示的itemView未滑出距离)
            scrollY = Math.min(scrollY, sumArray(heights, firstRow, heights.length - firstRow) - height);
        } else {
            //下滑 防止滑动距离 小于第0个itemView的高度
            /**
             * 注意 比较比较都是正数 或者负数
             */
            scrollY = Math.max(scrollY, -sumArray(heights, 0, firstRow));

//            Log.e("zdh","-------------scrollY???? "+scrollY);

        }

        return scrollY;
    }

    /**
     * 上滑 或者 下滑 需要重新摆放itemView的位置
     */
    private void rePositionView() {
        //仔细观察你会发现itemView的左右是不变的，改变的是上下
        //记录 itemView的上下信息
        int top = 0, bottom;
        //top要减去滑动距离
        top = -scrollY;

//        Log.e("zdh", "-------------???top  " + top);
        //计算出当前显示第一行itemView的position的top，就可以通过循环设置下面的itemView
        for (int i1 = 0; i1 < viewList.size(); i1++) {
            //下移一个 或者上移一个 itemView
            bottom = top + heights[i1];

            //view的摆放 仔细观察你会发现itemView的左右是不变的，改变的是上下
            viewList.get(i1).layout(0, top, width, bottom);

            //改变下一个item的top，就会上一个itemView的bottom
            top = bottom;
        }

    }


    /**
     * 获取当前显示的所有itemView高度
     *
     * @return
     */
    private int getFillHeight() {
        int itemViewAllHeight = sumArray(heights, firstRow, (viewList.size())) - scrollY;
        //注意 减去滑出的偏移量
        return itemViewAllHeight;
    }

    /**
     * -------------------------------------回收--------------------------------------
     */

    /**
     * 我们删除itemView都调用了这个方法 所以直接重新这个方法 所有回收逻辑就写在这个方法里面
     *
     * @param view
     */
    @Override
    public void removeView(View view) {
        super.removeView(view);
        int key = (int) view.getTag(R.id.tag_type_view);
        recycler.put(view, key);
    }

    /**
     * -------------------------------------适配器-----------------------------------------
     *
     * @return
     */

    public Adapter getAdapter() {
        return myAdapter;
    }

    public void setAdapter(Adapter myAdapter) {
        this.myAdapter = myAdapter;

        //初始化回收池
        if (myAdapter != null) {
            recycler = new Recycler(myAdapter.getViewTypeCount());
            scrollY = 0;
            firstRow = 0;
            needRelayout = true;
            //重新测量 摆放
            requestLayout();
        }
    }


    /**
     * 适配器
     */
    public interface Adapter {
        /**
         * @param position
         * @param convertView                          ---》itemView
         * @param parent-------》父布局view---》RecycleView
         * @return
         */

        //创建ViewHolder
        View onCreateViewHolder(int position, View convertView, ViewGroup parent);


        //绑定viewHolder
        View onBinderViewHolder(int position, View convertView, ViewGroup parent);

        //获取当前position 的item的类型
        int getItemViewType(int position);

        //获取当前itemView类型的总数
        int getViewTypeCount();

        //获取当前item的总数量
        int getCount();

        //获取index item的高度
        int getHeight(int index);


    }


    /**
     * ------------------------------------------------嵌套滑动-------------------------------------
     *
     * @param i
     * @param i1
     * @return
     */
    @Override
    public boolean startNestedScroll(int i, int i1) {
        return nestedScrollingChildHelper.startNestedScroll(i,i1);
    }

    @Override
    public void stopNestedScroll(int i) {
      nestedScrollingChildHelper.stopNestedScroll(i);
    }

    @Override
    public boolean hasNestedScrollingParent(int i) {
        return false;
    }

    @Override
    public boolean dispatchNestedScroll(int i, int i1, int i2, int i3, @Nullable int[] ints, int i4) {
        return false;
    }

    @Override
    public boolean dispatchNestedPreScroll(int i, int i1, @Nullable int[] ints, @Nullable int[] ints1, int i2) {
        return false;
    }

}
