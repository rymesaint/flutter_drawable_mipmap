package com.rymesaint.flutter_drawable_mipmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import java.io.ByteArrayOutputStream;

/** FlutterDrawableMipmapPlugin */
public class FlutterDrawableMipmapPlugin implements FlutterPlugin, MethodCallHandler {
    private MethodChannel channel;
    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_drawable_mipmap");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (context == null) {
            return;
        }
        if (call.method.equals("drawableMipmap")) {
            String name = call.argument("name");
            Boolean isDrawable = call.argument("is_drawable");
            if (isDrawable == null) {
                isDrawable = false;
            }

            if (name == null || name.isEmpty()) {
                result.error("INVALID_NAME", "Resource name cannot be null or empty", null);
                return;
            }

            int id = context.getResources().getIdentifier(
                name,
                isDrawable ? "drawable" : "mipmap",
                context.getPackageName()
            );

            if (id == 0) {
                result.error("RESOURCE_NOT_FOUND", "Resource '" + name + "' not found", null);
                return;
            }

            Drawable drawable = ContextCompat.getDrawable(context, id);
            byte[] byteArray = drawableToByteArray(drawable);
            result.success(byteArray);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        if (channel != null) {
            channel.setMethodCallHandler(null);
            channel = null;
        }
        context = null;
    }

    private byte[] drawableToByteArray(Drawable drawable) {
        if (drawable == null) {
            Log.e("FlutterDrawableMipmap", "Drawable is null");
            return new byte[0];
        }
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            if (width <= 0) width = 1;
            if (height <= 0) height = 1;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
