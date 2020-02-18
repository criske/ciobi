package com.crskdev.ciobi.ui.common

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Looper
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.core.animation.addListener
import androidx.core.content.getSystemService
import androidx.core.view.postOnAnimationDelayed
import com.crskdev.ciobi.R

/**
 * Created by Cristian Pela on 10.02.2020.
 */
class WindowOverlay<T>(private val context: Context,
                       @StyleRes private val style: Int = R.style.AppTheme,
                       private val position: DpPoint = DpPoint.ORIGIN,
                       private val size: DpSize = DpSize.WRAP_CONTENT,
                       private val alignment: Alignment = Alignment.Center,
                       private val onClose: (Any?) -> Unit = {},
                       private val content: WindowOverlayScope<T>.() -> View) {


    private var windowManager: WindowManager? = null
    private var windowOverlayScope: WindowOverlayScopeImpl<T>? = null
    private var contentView: ViewGroup? = null

    var isClosing = false
        private set
    var isCreating = false
        private set

    private val animationDuration = 350L

    fun create() {
        if (isCreating) return

        isCreating = true

        val windowContext = ContextThemeWrapper(context.applicationContext, style)
        windowManager = windowContext.getSystemService()
        val scope = WindowOverlayScopeImpl(this, windowContext)
        windowOverlayScope = scope

        val container = FrameLayout(windowContext).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            alpha = 0f
        }
        val createdContent = scope.run(content)
        contentView = container.apply { container.addView(createdContent) }
        windowManager?.addView(contentView, layoutParams())
        ObjectAnimator.ofFloat(container, "alpha", 1f).apply {
            duration = animationDuration
            addListener(
                onEnd = { scope.onCreateBlock.invoke(createdContent) })
            startDelay = 1000
            start()
        }
    }

    fun update(data: T) {
        val contentChild = contentView?.getChildAt(0)
        if (contentChild != null) {
            fun doUpdate() {
                contentChild.also { v -> windowOverlayScope?.run { v.onUpdateBlock(data) } }
            }
            //always update on the main thread - just to be safe
            if (Looper.getMainLooper() == Looper.myLooper()) {
                doUpdate()
            } else {
                contentChild.post { doUpdate() }
            }
        }
    }

    fun destroy(closingData: Any? = null) {
        if (isClosing) return
        contentView?.let { content ->
            ObjectAnimator.ofFloat(content, "alpha", 0f).apply {
                duration = animationDuration
                addListener(
                    onStart = { isClosing = true },
                    onEnd = {
                        windowOverlayScope?.run {
                            onDestroyBlock()
                            onCreateBlock = {}
                            onDestroyBlock = {}
                            onUpdateBlock = {}
                        }
                        onClose(closingData)
                        content.postOnAnimationDelayed(100) {
                            windowManager?.removeView(content)
                            contentView = null
                            isClosing = false
                            isCreating = false
                        }
                    })
                start()
            }
        }
    }

    private fun layoutParams(): WindowManager.LayoutParams = withDensity {
        val width = when (size.width) {
            Dp.Hairline -> WindowManager.LayoutParams.WRAP_CONTENT
            Dp.Infinity -> WindowManager.LayoutParams.MATCH_PARENT
            else -> size.width.toIntPx().value
        }
        val height = when (size.height) {
            Dp.Hairline -> WindowManager.LayoutParams.WRAP_CONTENT
            Dp.Infinity -> WindowManager.LayoutParams.MATCH_PARENT
            else -> size.height.toIntPx().value
        }
        val x = position.x.toIntPx().value
        val y = position.y.toIntPx().value
        WindowManager.LayoutParams(
            width,
            height,
            x,
            y,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                .or(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL),
            PixelFormat.TRANSLUCENT
        ).apply { gravity = alignment.value }
    }


    sealed class Alignment(val value: Int) {
        object Top : Alignment(Gravity.TOP)
        object Start : Alignment(Gravity.START)
        object End : Alignment(Gravity.END)
        object Bottom : Alignment(Gravity.BOTTOM)
        object Center : Alignment(Gravity.CENTER)
        class Combined internal constructor(value: Int) : Alignment(value)

        operator fun plus(other: Alignment): Combined {
            if (this is Combined || other is Combined) {
                throw IllegalStateException("You add combined alignments")
            }
            return Combined(this.value.or(other.value))
        }
    }


    private class WindowOverlayScopeImpl<T>(
        val windowOverlay: WindowOverlay<T>,
        override val context: ContextThemeWrapper) : WindowOverlayScope<T> {

        private val inflater = LayoutInflater.from(context)

        var onCreateBlock: View.() -> Unit = {}

        var onDestroyBlock: () -> Unit = {}

        var onUpdateBlock: View.(T) -> Unit = {}

        override fun inflate(@LayoutRes layout: Int): View = inflater.inflate(layout, null)

        override fun onCreate(block: View.() -> Unit) {
            onCreateBlock = block
        }

        override fun onDestroy(block: () -> Unit) {
            onDestroyBlock = block
        }

        override fun onUpdate(block: View.(T) -> Unit) {
            onUpdateBlock = block
        }

        override fun close(closingData: Any?) {
            windowOverlay.destroy(closingData)
        }

    }

}


interface WindowOverlayScope<T> {
    val context: Context
    fun inflate(@LayoutRes layout: Int): View
    fun onCreate(block: View.() -> Unit)
    fun onDestroy(block: () -> Unit)
    fun onUpdate(block: View.(T) -> Unit)
    fun close(closingData: Any? = null)
}




