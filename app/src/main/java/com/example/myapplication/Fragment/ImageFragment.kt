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
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.PictureDrawable
import android.media.Image
import android.provider.MediaStore
import android.view.ViewOutlineProvider
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.RenderMode
import com.caverock.androidsvg.SVG
import com.example.myapplication.CustomView.Tracing.OnDrawingCompleteListener
//import com.example.myapplication.CustomView.Tracing.TracingView2
import com.example.myapplication.CustomView.Tracing.TracingView3
import com.example.myapplication.MainActivity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import yuku.ambilwarna.AmbilWarnaDialog

class ImageFragment : Fragment() {
    private var tracingView: TracingView3? = null
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
        val successOverlay = view.findViewById<View>(R.id.chooseView)

        val originalImage = view.findViewById<ImageView>(R.id.originalImage)
        val testimageori = view.findViewById<ImageView>(R.id.testimageori)
        val undoButton = view.findViewById<ImageButton>(R.id.btn_undo)
        val level = arguments?.getInt(ARG_LEVEL) ?: 0
        val isFreeDrawMode = level == 0

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
        val btnSave = successOverlay.findViewById<ImageButton>(R.id.btnsave)

        btnSave.setOnClickListener {
            val filename = "drawing_${System.currentTimeMillis()}.png"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ColorPage") // Tự tạo thư mục "ColorPage" trong thư viện ảnh
            }

            val contentResolver = requireContext().contentResolver
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                contentResolver.openOutputStream(uri).use { outputStream ->
                    val bitmap = tracingView?.getDrawingCacheBitmap()
                    if (bitmap != null && outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        Toast.makeText(requireContext(), "Ảnh đã lưu vào thư viện!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Không thể lưu ảnh", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show()
            }
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
        tvLevel.text = if (isFreeDrawMode) "Free Draw Mode" else "LEVEL $level"

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
            var selectedColorView: View? = null

            val colorPicker = view.findViewById<LinearLayout>(R.id.colorPicker)
            val colors = extractColorsFromSVG(requireContext(), fileName)

// Thêm các màu lấy từ SVG
            for (color in colors) {
                val colorView = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                        setMargins(16, 0, 16, 0)
                    }

                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(color)
                        setStroke(4, Color.RED)
                    }

                    setOnClickListener {
                        tracingView?.setFillColor(color)

                        (selectedColorView?.background as? GradientDrawable)?.setStroke(4, Color.WHITE)
                        (this.background as? GradientDrawable)?.setStroke(6, Color.BLACK)

                        selectedColorView = this
                    }
                }
                colorPicker.addView(colorView)
            }

// Thêm màu tùy chỉnh cuối cùng (ô tròn trắng mở dialog màu)
            val customColorView = object : View(requireContext()) {
                override fun onDraw(canvas: android.graphics.Canvas) {
                    val radius = width / 2f
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = SweepGradient(
                            radius, radius,
                            intArrayOf(
                                Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN,
                                Color.GREEN, Color.YELLOW, Color.RED
                            ),
                            null
                        )
                    }
                    canvas.drawCircle(radius, radius, radius, paint)
                }
            }.apply {
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    setMargins(16, 0, 16, 0)
                }

                setOnClickListener {
                    val colorPickerDialog = AmbilWarnaDialog(requireContext(), Color.WHITE,
                        object : AmbilWarnaDialog.OnAmbilWarnaListener {
                            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                                tracingView?.setFillColor(color)
                                selectedColorView = this@apply
                            }

                            override fun onCancel(dialog: AmbilWarnaDialog?) {}
                        })
                    colorPickerDialog.show()
                }
            }
            colorPicker.addView(customColorView)



            val tracingInputStream = requireContext().assets.open(fileName)
            tracingView = TracingView3(requireContext()).apply {
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
//                                (activity as? MainActivity)?.loadNextLevel()
                                val anim = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                                successOverlay.visibility = View.VISIBLE
                                successOverlay.startAnimation(anim)

                                successOverlay.visibility = View.VISIBLE

                                val resultBitmap = tracingView?.getDrawingCacheBitmap()
                                if (resultBitmap != null) {
                                    testimageori.setImageBitmap(resultBitmap)
                                }

                                val btnNext = successOverlay.findViewById<ImageButton>(R.id.btnNext)
                                val btnReplay = successOverlay.findViewById<ImageButton>(R.id.btnReplay)

                                if (isFreeDrawMode) {
                                    btnNext.visibility = View.GONE
                                    btnReplay.visibility = View.VISIBLE
                                }
                                btnNext.apply {
                                    visibility = if (isFreeDrawMode) View.GONE else View.VISIBLE
                                    setOnClickListener {
                                        parentFragmentManager.setFragmentResult("LOAD_NEXT_LEVEL", Bundle())
                                        findNavController().popBackStack()
                                    }
                                }

                                btnReplay.setOnClickListener {
                                    successOverlay.visibility = View.GONE
                                    findNavController().run {
                                        popBackStack()
                                        navigate(R.id.imageFragment, Bundle().apply{
                                            putString("file_name",fileName)
                                            putInt("level",level)
                                        })
                                    }
                                }
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

}