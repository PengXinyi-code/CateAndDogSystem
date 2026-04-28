package com.fast.succour.service.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VectorUtil {
    public static float[] bytesToFloatArray(byte[] bytes) {
        if (bytes == null) return null;

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        buffer.order(ByteOrder.LITTLE_ENDIAN);

        FloatBuffer fb = buffer.asFloatBuffer();

        float[] arr = new float[fb.remaining()];
        fb.get(arr);

        return arr;
    }

    public static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}