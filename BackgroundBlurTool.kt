package com.example.camerapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * Classe responsável por implementar a ferramenta de borrar fundo
 * utilizando algoritmos de visão computacional para detecção automática
 * de primeiro plano/fundo
 */
class BackgroundBlurTool {

    init {
        // Inicializar OpenCV
        OpenCVLoader.initDebug()
    }

    companion object {
        // Constantes para os modos de seleção
        const val MODE_AUTO = 0
        const val MODE_MANUAL = 1
    }

    /**
     * Detecta automaticamente o primeiro plano (objeto principal) na imagem
     * @param bitmap Imagem original
     * @return Máscara onde branco = primeiro plano, preto = fundo
     */
    fun detectForeground(bitmap: Bitmap): Bitmap {
        // Converter bitmap para Mat (formato OpenCV)
        val inputMat = Mat()
        Utils.bitmapToMat(bitmap, inputMat)
        
        // Converter para escala de cinza
        val grayMat = Mat()
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY)
        
        // Aplicar desfoque gaussiano para reduzir ruído
        Imgproc.GaussianBlur(grayMat, grayMat, Size(5.0, 5.0), 0.0)
        
        // Detectar bordas com Canny
        val edgesMat = Mat()
        Imgproc.Canny(grayMat, edgesMat, 50.0, 150.0)
        
        // Dilatar bordas para conectar regiões
        val dilatedEdgesMat = Mat()
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        Imgproc.dilate(edgesMat, dilatedEdgesMat, kernel)
        
        // Encontrar contornos
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(dilatedEdgesMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        
        // Criar máscara vazia
        val maskMat = Mat.zeros(inputMat.size(), CvType.CV_8UC1)
        
        // Desenhar contornos na máscara
        Imgproc.drawContours(maskMat, contours, -1, Scalar(255.0, 255.0, 255.0), -1)
        
        // Suavizar máscara
        Imgproc.GaussianBlur(maskMat, maskMat, Size(11.0, 11.0), 0.0)
        
        // Converter máscara para bitmap
        val maskBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(maskMat, maskBitmap)
        
        // Liberar recursos
        inputMat.release()
        grayMat.release()
        edgesMat.release()
        dilatedEdgesMat.release()
        maskMat.release()
        hierarchy.release()
        for (contour in contours) {
            contour.release()
        }
        
        return maskBitmap
    }
    
    /**
     * Cria uma máscara manual com base nos toques do usuário
     * @param bitmap Imagem original
     * @param touchPoints Lista de pontos onde o usuário tocou
     * @param brushSize Tamanho do pincel
     * @return Máscara onde branco = área selecionada, preto = fundo
     */
    fun createManualMask(bitmap: Bitmap, touchPoints: List<Pair<Float, Float>>, brushSize: Float): Bitmap {
        val maskBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(maskBitmap)
        val paint = Paint()
        
        // Configurar pincel
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = brushSize
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        
        // Desenhar caminho com base nos pontos de toque
        if (touchPoints.size > 1) {
            val path = Path()
            path.moveTo(touchPoints[0].first, touchPoints[0].second)
            
            for (i in 1 until touchPoints.size) {
                path.lineTo(touchPoints[i].first, touchPoints[i].second)
            }
            
            canvas.drawPath(path, paint)
        } else if (touchPoints.size == 1) {
            // Se houver apenas um ponto, desenhar um círculo
            canvas.drawCircle(touchPoints[0].first, touchPoints[0].second, brushSize / 2, paint)
        }
        
        return maskBitmap
    }
    
    /**
     * Aplica o efeito de desfoque de fundo à imagem
     * @param bitmap Imagem original
     * @param mask Máscara indicando áreas a serem preservadas (branco) e desfocadas (preto)
     * @param blurIntensity Intensidade do desfoque (1-25)
     * @return Imagem com o fundo desfocado
     */
    fun applyBackgroundBlur(bitmap: Bitmap, mask: Bitmap, blurIntensity: Int): Bitmap {
        // Utilizar o FilterProcessor para aplicar o desfoque
        val filterProcessor = FilterProcessor()
        return filterProcessor.blurBackground(bitmap, mask, blurIntensity)
    }
    
    /**
     * Inverte a máscara (troca áreas brancas por pretas e vice-versa)
     * @param mask Máscara original
     * @return Máscara invertida
     */
    fun invertMask(mask: Bitmap): Bitmap {
        val width = mask.width
        val height = mask.height
        val pixels = IntArray(width * height)
        
        mask.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            // Inverter valores de cada pixel
            val alpha = pixels[i] shr 24 and 0xff
            val r = 255 - (pixels[i] shr 16 and 0xff)
            val g = 255 - (pixels[i] shr 8 and 0xff)
            val b = 255 - (pixels[i] and 0xff)
            
            pixels[i] = alpha shl 24 or (r shl 16) or (g shl 8) or b
        }
        
        val invertedMask = Bitmap.createBitmap(width, height, mask.config)
        invertedMask.setPixels(pixels, 0, width, 0, 0, width, height)
        
        return invertedMask
    }
    
    /**
     * Refina a máscara para melhorar a detecção de bordas
     * @param bitmap Imagem original
     * @param mask Máscara inicial
     * @return Máscara refinada
     */
    fun refineMask(bitmap: Bitmap, mask: Bitmap): Bitmap {
        // Converter para Mat
        val bitmapMat = Mat()
        val maskMat = Mat()
        Utils.bitmapToMat(bitmap, bitmapMat)
        Utils.bitmapToMat(mask, maskMat)
        
        // Converter para escala de cinza
        Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_BGR2GRAY)
        
        // Aplicar GrabCut para refinar a segmentação
        val bgModel = Mat()
        val fgModel = Mat()
        val resultMat = Mat()
        
        // Criar máscara inicial para GrabCut
        val grabCutMask = Mat(maskMat.size(), CvType.CV_8UC1, Scalar(Imgproc.GC_PR_BGD))
        
        // Definir áreas de primeiro plano e fundo com base na máscara
        for (y in 0 until maskMat.rows()) {
            for (x in 0 until maskMat.cols()) {
                val pixelValue = maskMat.get(y, x)[0]
                if (pixelValue > 200) {
                    grabCutMask.put(y, x, Imgproc.GC_FGD.toDouble())
                } else if (pixelValue < 50) {
                    grabCutMask.put(y, x, Imgproc.GC_BGD.toDouble())
                }
            }
        }
        
        // Executar GrabCut
        Imgproc.grabCut(bitmapMat, grabCutMask, Rect(), bgModel, fgModel, 3, Imgproc.GC_INIT_WITH_MASK)
        
        // Criar máscara final
        Core.compare(grabCutMask, Scalar(Imgproc.GC_PR_FGD), resultMat, Core.CMP_EQ)
        
        // Suavizar bordas
        Imgproc.GaussianBlur(resultMat, resultMat, Size(5.0, 5.0), 0.0)
        
        // Converter para bitmap
        val refinedMask = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resultMat, refinedMask)
        
        // Liberar recursos
        bitmapMat.release()
        maskMat.release()
        bgModel.release()
        fgModel.release()
        grabCutMask.release()
        resultMat.release()
        
        return refinedMask
    }
    
    /**
     * Aplica desfoque gradual (mais intenso nas bordas)
     * @param bitmap Imagem original
     * @param mask Máscara
     * @param maxBlurIntensity Intensidade máxima do desfoque
     * @return Imagem com desfoque gradual
     */
    fun applyGradualBlur(bitmap: Bitmap, mask: Bitmap, maxBlurIntensity: Int): Bitmap {
        // Converter para Mat
        val bitmapMat = Mat()
        val maskMat = Mat()
        Utils.bitmapToMat(bitmap, bitmapMat)
        Utils.bitmapToMat(mask, maskMat)
        
        // Converter máscara para escala de cinza
        Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_BGR2GRAY)
        
        // Calcular mapa de distância
        val distMat = Mat()
        Imgproc.distanceTransform(maskMat, distMat, Imgproc.DIST_L2, 3)
        
        // Normalizar mapa de distância
        Core.normalize(distMat, distMat, 0.0, 1.0, Core.NORM_MINMAX)
        
        // Criar máscara gradual
        val gradualMaskMat = Mat(maskMat.size(), CvType.CV_8UC1)
        distMat.convertTo(gradualMaskMat, CvType.CV_8UC1, 255.0)
        
        // Converter para bitmap
        val gradualMask = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(gradualMaskMat, gradualMask)
        
        // Aplicar desfoque com máscara gradual
        val filterProcessor = FilterProcessor()
        val result = filterProcessor.blurBackground(bitmap, gradualMask, maxBlurIntensity)
        
        // Liberar recursos
        bitmapMat.release()
        maskMat.release()
        distMat.release()
        gradualMaskMat.release()
        
        return result
    }
}
