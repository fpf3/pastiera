package it.palsoftware.pastiera.inputmethod

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat

/**
 * Helper per gestire le notifiche dell'app.
 */
object NotificationHelper {
    private const val CHANNEL_ID = "pastiera_nav_mode_channel"
    private const val CHANNEL_NAME = "Pastiera Nav Mode"
    private const val NOTIFICATION_ID = 1
    
    /**
     * Verifica se il permesso per le notifiche è concesso.
     * Su Android 13+ (API 33+) è necessario il permesso POST_NOTIFICATIONS.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ richiede il permesso POST_NOTIFICATIONS
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 e precedenti non richiedono permessi espliciti per le notifiche
            true
        }
    }
    
    /**
     * Crea il canale di notifica (richiesto per Android 8.0+).
     * Usa IMPORTANCE_HIGH per cercare di posizionare la notifica più in alto nella barra di stato.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Priorità alta per visibilità migliore
            ).apply {
                description = "Notifiche per il nav mode di Pastiera"
                setShowBadge(false)
                // Su Android 8.0+, le notifiche con importanza alta possono apparire più in alto
                enableLights(true)
                enableVibration(false)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Crea un'icona bitmap con la lettera "N" per la notifica del nav mode.
     * @param size Dimensione dell'icona in pixel
     * @param backgroundColor Colore di sfondo (default trasparente)
     * @param textColor Colore del testo (default bianco)
     */
    private fun createNavModeIcon(
        size: Int,
        backgroundColor: Int = Color.TRANSPARENT,
        textColor: Int = Color.WHITE
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Disegna lo sfondo
        if (backgroundColor != Color.TRANSPARENT) {
            canvas.drawColor(backgroundColor)
        } else {
            canvas.drawColor(Color.TRANSPARENT)
        }
        
        // Disegna la lettera "N"
        val paint = Paint().apply {
            color = textColor
            textSize = size * 0.7f // 70% della dimensione per avere margini
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        // Calcola la posizione verticale per centrare il testo
        val textY = (canvas.height / 2) - ((paint.descent() + paint.ascent()) / 2)
        
        // Disegna la "N"
        canvas.drawText("N", canvas.width / 2f, textY, paint)
        
        return bitmap
    }
    
    /**
     * Mostra una notifica per l'attivazione del nav mode.
     * Verifica prima se il permesso è concesso.
     */
    fun showNavModeActivatedNotification(context: Context) {
        // Verifica se il permesso è concesso
        if (!hasNotificationPermission(context)) {
            android.util.Log.w("NotificationHelper", "Permesso per le notifiche non concesso")
            return
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Crea il canale se non esiste (per Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
        }
        
        // Crea l'icona personalizzata con "N" per la small icon (barra di stato)
        // Dimensioni standard per small icon: 24dp convertiti in pixel
        val smallIconSize = (24 * context.resources.displayMetrics.density).toInt().coerceAtLeast(24)
        val smallIconBitmap = createNavModeIcon(smallIconSize, Color.TRANSPARENT, Color.WHITE)
        val smallIcon = IconCompat.createWithBitmap(smallIconBitmap)
        
        // Crea l'icona grande per la notifica espansa
        val largeIconSize = (64 * context.resources.displayMetrics.density).toInt().coerceAtLeast(64)
        val largeIconBitmap = createNavModeIcon(largeIconSize, Color.TRANSPARENT, Color.WHITE)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Nav Mode Activated")
            .setContentText("Nav mode activated")
            .setSmallIcon(smallIcon) // Icona personalizzata con "N" per la barra di stato
            .setLargeIcon(largeIconBitmap) // Icona grande con "N" per la notifica espansa
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Priorità alta per visibilità migliore
            .setAutoCancel(true) // Si chiude automaticamente quando viene toccata
            .setOngoing(true) // Persistente per rimanere visibile
            .setCategory(NotificationCompat.CATEGORY_STATUS) // Categoria status per barra di stato
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visibile anche su schermo bloccato
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Cancella la notifica del nav mode.
     */
    fun cancelNavModeNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}

