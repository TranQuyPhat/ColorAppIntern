package com.example.myapplication.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import android.animation.Animator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.media.Image
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.RenderMode
import com.caverock.androidsvg.SVG
import com.example.myapplication.CustomView.Tracing.OnDrawingCompleteListener
import com.example.myapplication.CustomView.Tracing.TracingView2
import com.example.myapplication.MainActivity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ImageFragment : Fragment() {
    private var tracingView: TracingView2? = null
    private var selectedPenResId: Int = R.drawable.pen1 // Mặc định là bút vẽ đầu tiên
    companion object {
        private const val ARG_FILE_NAME = "file_name"
        private const val ARG_LEVEL = "level"
        fun newInstance(fileName: String, level: Int): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle().apply {
                putString(ARG_FILE_NAME, fileName)
                putInt(ARG_LEVEL, level)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {   
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileName = arguments?.getString(ARG_FILE_NAME)
        val containerView = view.findViewById<FrameLayout>(R.id.tracingContainer)
        val originalImage = view.findViewById<ImageView>(R.id.originalImage)
        val undoButton = view.findViewById<ImageButton>(R.id.btn_undo)
        val level = arguments?.getInt(ARG_LEVEL) ?: 1
        val gridButton = view.findViewById<ImageButton>(R.id.btngrid)
        val paintButton=view.findViewById<ImageButton>(R.id.btn_pen)
        val doneAnimation = view.findViewById<LottieAnimationView>(R.id.doneAnimation).apply {
            setRenderMode(RenderMode.HARDWARE) //
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            setAnimation("success.json") // hoặc setAnimation(R.raw.done)
            buildDrawingCache()
            pauseAnimation()
            visibility = View.GONE
        }
        gridButton.setOnClickListener {
            findNavController().navigate(R.id.toGridFragment)
        }
        paintButton.setOnClickListener {
            findNavController().navigate(R.id.toPencilFragment)
        }
        parentFragmentManager.setFragmentResultListener(
            "PEN_SELECTION_REQUEST",
            this
        ) { _, bundle ->
            bundle.getInt("SELECTED_PEN_RES_ID").let { resId ->
                selectedPenResId = resId
                tracingView?.setPenNibResource(resId)
            }
        }
        // 1. Set text cho TextView trong header_bar (include @id/headerBar)
        // tìm headerBar rồi lấy tv_level
        val headerBar = view.findViewById<View>(R.id.headerBar)
        val tvLevel = headerBar.findViewById<TextView>(R.id.tv_level)
        tvLevel.text = "LEVEL $level"
        undoButton.setOnClickListener {
            val success = tracingView?.undo() ?: false
            if (!success) {
                Toast.makeText(
                    requireContext(),
                    "Không có thao tác nào để hoàn tác",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (fileName != null) {
            val colorPicker = view.findViewById<LinearLayout>(R.id.colorPicker)
            val colors = extractColorsFromSVG(requireContext(), fileName)
            for (color in colors) {
                val colorView = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                        setMargins(16, 0, 16, 0)
                    }
                    setBackgroundColor(color)
                    setOnClickListener {
                        tracingView?.setFillColor(color)
                    }
                }
                colorPicker.addView(colorView)
            }
            val tracingInputStream = requireContext().assets.open(fileName)
            tracingView = TracingView2(requireContext()).apply {
                setPenNibResource(selectedPenResId)
                loadSVGFromAsset(tracingInputStream)
            }
            tracingInputStream.close()

            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            containerView.addView(tracingView, params)
            tracingView?.setOnDrawingCompleteListener(  object : OnDrawingCompleteListener {
                override fun onDrawingComplete() {
                    doneAnimation.visibility = View.VISIBLE
                    doneAnimation.playAnimation()
                    doneAnimation.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) {
                            doneAnimation.removeAllAnimatorListeners()
                            doneAnimation.visibility = View.GONE

                            // Delay 1 giây rồi load level kế
                            view?.postDelayed({
                                (activity as? MainActivity)?.loadNextLevel()
                            }, 1000)
                        }
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                }
            })
            try {
                val imageInputStream = requireContext().assets.open(fileName)
                val svg = SVG.getFromInputStream(imageInputStream)
                val drawable = PictureDrawable(svg.renderToPicture())
                originalImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                originalImage.setImageDrawable(drawable)
                imageInputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


    }


    fun extractColorsFromSVG(context: Context, fileName: String): Set<Int> {
        val colors = mutableSetOf<Int>()
        context.assets.open(fileName).use { inputStream ->
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    // 1) Lấy fill & stroke trực tiếp
                    val fill = parser.getAttributeValue(null, "fill")
                    val stroke = parser.getAttributeValue(null, "stroke")
                    listOf(fill, stroke).forEach { colorStr ->
                        if (!colorStr.isNullOrBlank() && colorStr != "none") {
                            try {
                                colors.add(Color.parseColor(colorStr))
                            } catch (_: Exception) {
                            }
                        }
                    }

                    // 2) Nếu SVG dùng style="fill:#f00;stroke:#00f;"
                    val style = parser.getAttributeValue(null, "style")
                    if (!style.isNullOrBlank()) {
                        style.split(';').forEach { segment ->
                            val parts = segment.split(':')
                            if (parts.size == 2) {
                                val key = parts[0].trim()
                                val value = parts[1].trim()
                                if ((key == "fill" || key == "stroke") && value != "none") {
                                    try {
                                        colors.add(Color.parseColor(value))
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        }
        return colors
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        tracingView?.onDetachedFromWindow()
//        tracingView = null
//    }


}