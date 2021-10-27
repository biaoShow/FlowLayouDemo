package com.biao.myflowlayout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

class FlowLayout : ViewGroup {

    private val TAG = FlowLayout::class.java.simpleName

    private val spacing = 50 //每个子view间隔
    private val rowSpacing = 30 //行距

    //记录所有子view
    private var views: ArrayList<ArrayList<View>> = ArrayList<ArrayList<View>>()

    //记录每一行的子view
    private var lineViews: ArrayList<View> = ArrayList<View>()

    //记录每一行的高度
    private val lineHeights = ArrayList<Int>()

    constructor(context: Context) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount
        val mPaddingLeft = paddingLeft
        val mPaddingTop = paddingTop
        val mPaddingRight = paddingRight
        val mPaddingBottom = paddingBottom
        var sumHeight = 0 //累加ViewGroup高度
        var maxLineHeight = 0 //每行最大的高度
        var lineWidth = 0 //记录每一行子view累加多宽
        var maxWidth = 0 //全部行中最宽一行的宽度

        views.clear()
        lineHeights.clear()
        lineViews.clear()

        //当前ViewGroup宽高
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)

        //遍历每个子view
        for (i in 0 until count) {

            //获取到子view
            val viewChild = getChildAt(i)

            if (viewChild.visibility != View.GONE) {

                //子view参数(xml里面的参数)
                val childParams = viewChild.layoutParams

                //结合父布局要求(包括测量模式和大小)，计算出当前子view大小（即xml dp 转成 MeasureSpec 的过程）
                //MeasureSpec可供下一代子view使用，还不是确切值，有可能需要下一代子view先确认大小
                val childWidth = getChildMeasureSpec(
                    widthMeasureSpec, mPaddingLeft + mPaddingRight, childParams.width
                )
                val childHeight = getChildMeasureSpec(
                    heightMeasureSpec, mPaddingTop + mPaddingBottom, childParams.height
                )

                //传入当前子view理论（参考）大小，确认当前子view和下一代子view大小并保存
                //确认下一代的view大小后，当前的子view大小才确切
                viewChild.measure(childWidth, childHeight)

                //获取子view测量后确切宽和高
                val width = viewChild.measuredWidth
                val height = viewChild.measuredHeight

                //已使用宽度+当前子view宽度 大于 父ViewGroup宽度则需要换行
                if ((lineWidth + width + spacing) > parentWidth) {
                    maxWidth = max(maxWidth, lineWidth)//保存最大的宽度，作为ViewGroup宽度
                    sumHeight += (maxLineHeight + rowSpacing) //累加目前全部布局的总高度
                    views.add(lineViews)//整行添加到列表记录
                    lineHeights.add(maxLineHeight)//添加一行中最高高度，作为后面布局时候计算view纵坐标位置

                    //数据clear
                    lineWidth = 0
                    maxLineHeight = 0
                    lineViews = ArrayList<View>()
                }

                //累加每行已使用宽度
                lineWidth += width + spacing
                //记录每行最大高度
                maxLineHeight = max(maxLineHeight, height)
                //记录每一行View
                lineViews.add(viewChild)

                //最后一行不一定满足宽度大于父布局宽度，所以特殊处理
                if (i == (count - 1)) {
                    sumHeight += (maxLineHeight + rowSpacing) //累加目前全部布局的总高度
                    views.add(lineViews)//整行添加到列表记录
                    lineHeights.add(maxLineHeight)//添加一行中最高高度，作为后面布局时候计算view纵坐标位置
                }

            }

        }

        Log.i(TAG, "maxWidth = $maxWidth, sumHeight = $sumHeight")
        //设置当前ViewGroup宽和高
        setMeasuredDimension(maxWidth, sumHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        var left = paddingLeft
        var top = paddingTop

        for (i in 0 until views.size) {
            //获取每一行所有view集合
            val lineViews = views[i]
            for (j in 0 until lineViews.size) {
                //获取每一个view
                val view = lineViews[j]

                //计算每个view的右下角坐标
                val right = left + view.measuredWidth
                val bottom = top + view.measuredHeight
                Log.i(
                    TAG,
                    "i = $i, width = ${view.measuredWidth}, left = $left, top = $top, right = $right, bottom = $bottom"
                )
                //回调每一个view的onLayout方法，传入具体坐标
                view.layout(left, top, right, bottom)

                left = right + spacing //计算每一个view的左上角横坐标
            }

            //每一行布局完成后计算下一行布局左上角的坐标位置，下一行布局时候使用
            left = paddingLeft
            top += lineHeights[i] + rowSpacing
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }
}