package com.example.camerapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Classe responsável por gerenciar o salvamento e compartilhamento de imagens
 * capturadas pelo aplicativo de câmera
 */
class ImageSharingManager(private val context: Context) {

    /**
     * Salva a imagem na galeria do dispositivo com máxima qualidade
     * @param bitmap Imagem a ser salva
     * @param filterName Nome do filtro aplicado (para incluir no nome do arquivo)
     * @return Uri da imagem salva ou null em caso de erro
     */
    fun saveImageToGallery(bitmap: Bitmap, filterName: String): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "RETROCAM_${filterName}_$timestamp.jpg"
        
        var fos: OutputStream? = null
        var imageUri: Uri? = null
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 e superior - usar MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RetroCam")
                    // Garantir que a imagem não seja modificada pelo sistema
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                
                val resolver = context.contentResolver
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                    // Salvar com qualidade máxima (100)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    
                    // Marcar como não mais pendente para tornar visível na galeria
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
            } else {
                // Android 9 e inferior - usar sistema de arquivos
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/RetroCam"
                val dir = File(imagesDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                
                val image = File(dir, filename)
                fos = FileOutputStream(image)
                
                // Salvar com qualidade máxima (100)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                
                // Notificar galeria sobre o novo arquivo
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                imageUri = Uri.fromFile(image)
                mediaScanIntent.data = imageUri
                context.sendBroadcast(mediaScanIntent)
            }
            
            return imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            fos?.close()
        }
    }
    
    /**
     * Compartilha a imagem com outros aplicativos
     * @param bitmap Imagem a ser compartilhada
     * @param title Título da intent de compartilhamento
     * @return true se o compartilhamento foi iniciado com sucesso, false caso contrário
     */
    fun shareImage(bitmap: Bitmap, title: String): Boolean {
        try {
            // Salvar imagem temporariamente
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val tempFile = File(cachePath, "shared_image_$timestamp.jpg")
            
            val fos = FileOutputStream(tempFile)
            // Salvar com qualidade máxima (100)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            
            // Obter URI via FileProvider para compatibilidade com Android 7+
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            
            // Criar intent de compartilhamento
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Imagem capturada com RetroCam")
                putExtra(Intent.EXTRA_TEXT, "Confira esta foto que tirei com o aplicativo RetroCam!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Iniciar atividade de compartilhamento
            context.startActivity(Intent.createChooser(intent, title))
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Compartilha a imagem diretamente para uma rede social específica
     * @param bitmap Imagem a ser compartilhada
     * @param socialNetwork Identificador da rede social (ex: "instagram", "whatsapp")
     * @return true se o compartilhamento foi iniciado com sucesso, false caso contrário
     */
    fun shareToSocialNetwork(bitmap: Bitmap, socialNetwork: String): Boolean {
        try {
            // Salvar imagem temporariamente
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val tempFile = File(cachePath, "shared_image_$timestamp.jpg")
            
            val fos = FileOutputStream(tempFile)
            // Salvar com qualidade máxima (100)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            
            // Obter URI via FileProvider
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            
            // Criar intent de compartilhamento
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Definir pacote específico com base na rede social
            val packageName = when (socialNetwork.toLowerCase()) {
                "instagram" -> "com.instagram.android"
                "whatsapp" -> "com.whatsapp"
                "facebook" -> "com.facebook.katana"
                "twitter" -> "com.twitter.android"
                else -> return false
            }
            
            // Verificar se o aplicativo está instalado
            val packageManager = context.packageManager
            try {
                packageManager.getPackageInfo(packageName, 0)
                intent.setPackage(packageName)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                // Aplicativo não instalado, voltar para compartilhamento genérico
                context.startActivity(Intent.createChooser(intent, "Compartilhar via"))
                return true
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Cria uma marca d'água discreta no canto da imagem
     * @param bitmap Imagem original
     * @param watermarkText Texto da marca d'água
     * @return Bitmap com marca d'água
     */
    fun addWatermark(bitmap: Bitmap, watermarkText: String): Bitmap {
        val result = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = Color.WHITE
            alpha = 180 // Semitransparente
            textSize = bitmap.width * 0.04f // Tamanho proporcional à imagem
            isAntiAlias = true
            setShadowLayer(1f, 0f, 1f, Color.BLACK) // Sombra para legibilidade
        }
        
        // Posicionar no canto inferior direito
        val x = bitmap.width - paint.measureText(watermarkText) - 20
        val y = bitmap.height - 20
        
        canvas.drawText(watermarkText, x, y, paint)
        
        return result
    }
}
