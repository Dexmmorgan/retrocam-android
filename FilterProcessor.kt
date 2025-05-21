package com.example.camerapp

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Classe responsável por aplicar filtros retrô e populares às imagens capturadas
 */
class FilterProcessor {

    companion object {
        // Nomes dos filtros disponíveis
        val FILTER_NAMES = listOf(
            "Original", 
            "CPM35", 
            "Classic U", 
            "NT16", 
            "GRD", 
            "S 67", 
            "Inst SQC", 
            "D Classic",
            "VHS",
            "8mm",
            "Slide"
        )
    }

    /**
     * Aplica o filtro selecionado à imagem original
     * @param source Bitmap original de alta qualidade
     * @param filterType Tipo de filtro a ser aplicado
     * @return Bitmap com o filtro aplicado, mantendo a qualidade original
     */
    fun applyFilter(source: Bitmap, filterType: Int): Bitmap {
        // Cria uma cópia do bitmap original para preservar a imagem original
        val resultBitmap = source.copy(source.config, true)
        
        when (filterType) {
            0 -> return resultBitmap // Original - sem filtro
            1 -> applyCPM35Filter(resultBitmap)
            2 -> applyClassicUFilter(resultBitmap)
            3 -> applyNT16Filter(resultBitmap)
            4 -> applyGRDFilter(resultBitmap)
            5 -> applyS67Filter(resultBitmap)
            6 -> applyInstSQCFilter(resultBitmap)
            7 -> applyDClassicFilter(resultBitmap)
            8 -> applyVHSFilter(resultBitmap)
            9 -> apply8mmFilter(resultBitmap)
            10 -> applySlideFilter(resultBitmap)
        }
        
        return resultBitmap
    }
    
    /**
     * Filtro CPM35 - Vintage com grão suave, cores levemente desaturadas
     */
    private fun applyCPM35Filter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        // Ajuste de cores para o estilo vintage
        val colorMatrix = ColorMatrix()
        
        // Reduzir saturação levemente
        colorMatrix.setSaturation(0.8f)
        
        // Ajustar tons para dar aparência de filme
        val overlayMatrix = ColorMatrix(floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f, 10f,
            0.0f, 1.0f, 0.0f, 0.0f, 0f,
            0.0f, 0.0f, 0.9f, 0.0f, 10f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        colorMatrix.postConcat(overlayMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        // Aplicar o filtro mantendo a qualidade original
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Adicionar grão sutil para efeito de filme
        addFilmGrain(bitmap, 5)
    }
    
    /**
     * Filtro Classic U - Tons vintage equilibrados
     */
    private fun applyClassicUFilter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Ajustar contraste
        colorMatrix.set(floatArrayOf(
            1.1f, 0.0f, 0.0f, 0.0f, 10f,
            0.0f, 1.0f, 0.0f, 0.0f, 10f,
            0.0f, 0.0f, 0.9f, 0.0f, 20f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Adicionar grão médio
        addFilmGrain(bitmap, 8)
    }
    
    /**
     * Filtro NT16 - Ideal para retratos, com tons suaves e naturais
     */
    private fun applyNT16Filter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Ajustar para tons mais suaves, ideais para pele
        colorMatrix.set(floatArrayOf(
            1.1f, 0.0f, 0.0f, 0.0f, 5f,
            0.0f, 1.05f, 0.0f, 0.0f, 0f,
            0.0f, 0.0f, 1.0f, 0.0f, 5f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Grão muito sutil para retratos
        addFilmGrain(bitmap, 3)
    }
    
    /**
     * Filtro GRD - Contraste baixo para fotos com clima mais sombrio
     */
    private fun applyGRDFilter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Reduzir contraste e saturação para clima sombrio
        colorMatrix.setSaturation(0.7f)
        
        val contrastMatrix = ColorMatrix(floatArrayOf(
            0.9f, 0.0f, 0.0f, 0.0f, 10f,
            0.0f, 0.9f, 0.0f, 0.0f, 10f,
            0.0f, 0.0f, 0.9f, 0.0f, 10f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        colorMatrix.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Grão médio para efeito dramático
        addFilmGrain(bitmap, 10)
    }
    
    /**
     * Filtro S 67 - Alta saturação, ideal para fotos de comida
     */
    private fun applyS67Filter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Aumentar saturação para realçar cores de alimentos
        colorMatrix.setSaturation(1.4f)
        
        // Ajustar tons para realçar vermelhos e amarelos (comuns em comida)
        val foodMatrix = ColorMatrix(floatArrayOf(
            1.2f, 0.0f, 0.0f, 0.0f, 10f,
            0.0f, 1.1f, 0.0f, 0.0f, 10f,
            0.0f, 0.0f, 0.9f, 0.0f, 0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        colorMatrix.postConcat(foodMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Grão sutil
        addFilmGrain(bitmap, 5)
    }
    
    /**
     * Filtro Inst SQC - Estilo polaroid com bordas brancas
     */
    private fun applyInstSQCFilter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Ajustar contraste e brilho para estilo polaroid
        colorMatrix.set(floatArrayOf(
            1.2f, 0.0f, 0.0f, 0.0f, 10f,
            0.0f, 1.1f, 0.0f, 0.0f, 10f,
            0.0f, 0.0f, 1.0f, 0.0f, 10f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Adicionar vinheta para efeito polaroid
        addVignette(bitmap)
        
        // Grão médio
        addFilmGrain(bitmap, 8)
    }
    
    /**
     * Filtro D Classic - Efeito de câmera descartável dos anos 90
     */
    private fun applyDClassicFilter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Ajustar para aparência de câmera descartável
        colorMatrix.setSaturation(1.2f)
        
        val disposableMatrix = ColorMatrix(floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f, 15f,
            0.0f, 0.9f, 0.0f, 0.0f, 10f,
            0.0f, 0.0f, 0.8f, 0.0f, 20f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        colorMatrix.postConcat(disposableMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Grão pronunciado típico de câmeras descartáveis
        addFilmGrain(bitmap, 15)
        
        // Adicionar vazamento de luz sutil
        addLightLeak(bitmap)
    }
    
    /**
     * Filtro VHS - Efeito de fita VHS com distorções características
     */
    private fun applyVHSFilter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Reduzir saturação e ajustar cores para aparência de VHS
        colorMatrix.setSaturation(0.8f)
        
        val vhsMatrix = ColorMatrix(floatArrayOf(
            1.0f, 0.2f, 0.2f, 0.0f, 0f,
            0.2f, 1.0f, 0.2f, 0.0f, 0f,
            0.2f, 0.2f, 1.0f, 0.0f, 0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        colorMatrix.postConcat(vhsMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Adicionar ruído VHS
        addVHSNoise(bitmap)
    }
    
    /**
     * Filtro 8mm - Simulação de filme 8mm com grão pronunciado
     */
    private fun apply8mmFilter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Ajustar para aparência de filme 8mm
        colorMatrix.setSaturation(0.7f)
        
        val eightMmMatrix = ColorMatrix(floatArrayOf(
            1.2f, 0.0f, 0.0f, 0.0f, 10f,
            0.0f, 1.0f, 0.0f, 0.0f, 0f,
            0.0f, 0.0f, 0.8f, 0.0f, 0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        colorMatrix.postConcat(eightMmMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Grão muito pronunciado
        addFilmGrain(bitmap, 20)
        
        // Adicionar arranhões de filme
        addFilmScratches(bitmap)
    }
    
    /**
     * Filtro Slide - Efeito de slide com cores vibrantes
     */
    private fun applySlideFilter(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Aumentar saturação e contraste para efeito de slide
        colorMatrix.setSaturation(1.3f)
        
        val slideMatrix = ColorMatrix(floatArrayOf(
            1.2f, 0.0f, 0.0f, 0.0f, 0f,
            0.0f, 1.2f, 0.0f, 0.0f, 0f,
            0.0f, 0.0f, 1.2f, 0.0f, 0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        ))
        
        colorMatrix.postConcat(slideMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        // Grão sutil
        addFilmGrain(bitmap, 5)
    }
    
    /**
     * Adiciona grão de filme à imagem
     * @param bitmap Imagem a ser processada
     * @param intensity Intensidade do grão (1-20)
     */
    private fun addFilmGrain(bitmap: Bitmap, intensity: Int) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val random = java.util.Random()
        val grainFactor = intensity / 100f
        
        for (i in pixels.indices) {
            val noise = (random.nextFloat() - 0.5f) * grainFactor * 255
            
            val pixel = pixels[i]
            val r = Math.min(255, Math.max(0, ((pixel shr 16) and 0xff) + noise.toInt()))
            val g = Math.min(255, Math.max(0, ((pixel shr 8) and 0xff) + noise.toInt()))
            val b = Math.min(255, Math.max(0, (pixel and 0xff) + noise.toInt()))
            
            pixels[i] = (0xff shl 24) or (r shl 16) or (g shl 8) or b
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
    
    /**
     * Adiciona efeito de vinheta à imagem
     */
    private fun addVignette(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(centerX, centerY) * 0.7f
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                
                val distance = Math.sqrt(Math.pow((x - centerX).toDouble(), 2.0) + 
                                        Math.pow((y - centerY).toDouble(), 2.0))
                
                val factor = Math.max(0.0, 1.0 - distance / radius)
                
                val pixel = pixels[index]
                val r = ((pixel shr 16) and 0xff) * factor
                val g = ((pixel shr 8) and 0xff) * factor
                val b = (pixel and 0xff) * factor
                
                pixels[index] = (0xff shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
            }
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
    
    /**
     * Adiciona efeito de vazamento de luz à imagem
     */
    private fun addLightLeak(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // Escolher um canto para o vazamento de luz
        val cornerX = if (Math.random() > 0.5) 0 else width
        val cornerY = if (Math.random() > 0.5) 0 else height
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                
                val distance = Math.sqrt(Math.pow((x - cornerX).toDouble(), 2.0) + 
                                        Math.pow((y - cornerY).toDouble(), 2.0))
                
                val maxDistance = Math.sqrt(Math.pow(width.toDouble(), 2.0) + 
                                          Math.pow(height.toDouble(), 2.0))
                
                val factor = Math.max(0.0, 1.0 - distance / (maxDistance * 0.5))
                
                if (factor > 0.1) {
                    val pixel = pixels[index]
                    val r = Math.min(255, ((pixel shr 16) and 0xff) + (factor * 100).toInt())
                    val g = Math.min(255, ((pixel shr 8) and 0xff) + (factor * 50).toInt())
                    val b = Math.min(255, (pixel and 0xff) + (factor * 30).toInt())
                    
                    pixels[index] = (0xff shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
    
    /**
     * Adiciona ruído VHS à imagem
     */
    private fun addVHSNoise(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val random = java.util.Random()
        
        // Adicionar linhas horizontais aleatórias
        for (i in 0 until 10) {
            val y = random.nextInt(height)
            val lineHeight = 1 + random.nextInt(3)
            
            for (h in 0 until lineHeight) {
                if (y + h < height) {
                    for (x in 0 until width) {
                        val index = (y + h) * width + x
                        
                        // Distorção de cor aleatória
                        val r = Math.min(255, ((pixels[index] shr 16) and 0xff) + random.nextInt(50))
                        val g = Math.min(255, ((pixels[index] shr 8) and 0xff) + random.nextInt(50))
                        val b = Math.min(255, (pixels[index] and 0xff) + random.nextInt(50))
                        
                        pixels[index] = (0xff shl 24) or (r shl 16) or (g shl 8) or b
                    }
                }
            }
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
    
    /**
     * Adiciona arranhões de filme à imagem
     */
    private fun addFilmScratches(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val random = java.util.Random()
        
        // Adicionar arranhões verticais aleatórios
        for (i in 0 until 5) {
            val x = random.nextInt(width)
            val scratchWidth = 1 + random.nextInt(2)
            
            for (w in 0 until scratchWidth) {
                if (x + w < width) {
                    for (y in 0 until height) {
                        val index = y * width + (x + w)
                        
                        // 50% de chance de o arranhão ser branco
                        if (random.nextFloat() > 0.5f) {
                            pixels[index] = 0xffffffff.toInt()
                        }
                    }
                }
            }
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
    
    /**
     * Implementação da ferramenta de borrar fundo
     * @param bitmap Imagem original
     * @param maskBitmap Máscara indicando áreas a serem desfocadas (preto = desfoque, branco = nítido)
     * @param intensity Intensidade do desfoque (1-25)
     * @return Bitmap com o fundo desfocado
     */
    fun blurBackground(bitmap: Bitmap, maskBitmap: Bitmap, intensity: Int): Bitmap {
        // Criar cópia do bitmap original para preservar a qualidade
        val resultBitmap = bitmap.copy(bitmap.config, true)
        
        // Aplicar desfoque gaussiano à imagem inteira
        val blurredBitmap = applyGaussianBlur(resultBitmap, intensity)
        
        // Combinar a imagem original com a desfocada usando a máscara
        return blendWithMask(bitmap, blurredBitmap, maskBitmap)
    }
    
    /**
     * Aplica desfoque gaussiano à imagem
     */
    private fun applyGaussianBlur(bitmap: Bitmap, radius: Int): Bitmap {
        val blurred = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(blurred)
        val paint = Paint()
        
        // Aplicar filtro de desfoque
        paint.maskFilter = BlurMaskFilter(radius.toFloat(), BlurMaskFilter.Blur.NORMAL)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return blurred
    }
    
    /**
     * Combina duas imagens usando uma máscara
     */
    private fun blendWithMask(original: Bitmap, blurred: Bitmap, mask: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        
        val result = original.copy(original.config, true)
        val originalPixels = IntArray(width * height)
        val blurredPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)
        
        original.getPixels(originalPixels, 0, width, 0, 0, width, height)
        blurred.getPixels(blurredPixels, 0, width, 0, 0, width, height)
        
        // Redimensionar máscara se necessário
        val scaledMask = if (mask.width != width || mask.height != height) {
            Bitmap.createScaledBitmap(mask, width, height, true)
        } else {
            mask
        }
        
        scaledMask.getPixels(maskPixels, 0, width, 0, 0, width, height)
        
        for (i in 0 until width * height) {
            // Extrair valor de alfa da máscara (0 = desfoque total, 255 = sem desfoque)
            val maskValue = (maskPixels[i] and 0xff) / 255f
            
            // Misturar pixels com base no valor da máscara
            val r = ((originalPixels[i] shr 16) and 0xff) * maskValue + 
                   ((blurredPixels[i] shr 16) and 0xff) * (1 - maskValue)
            
            val g = ((originalPixels[i] shr 8) and 0xff) * maskValue + 
                   ((blurredPixels[i] shr 8) and 0xff) * (1 - maskValue)
            
            val b = (originalPixels[i] and 0xff) * maskValue + 
                   (blurredPixels[i] and 0xff) * (1 - maskValue)
            
            resultPixels[i] = (0xff shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
        }
        
        result.setPixels(resultPixels, 0, width, 0, 0, width, height)
        
        return result
    }
}
