package com.fast.succour.service.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VectorUtil {
    public static float[] bytesToFloatArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;

        // 确保字节数是 4 的倍数（每个 float 占 4 字节）
        if (bytes.length % 4 != 0) {
            System.err.println("警告：特征向量字节数 " + bytes.length + " 不是 4 的倍数，可能数据已损坏");
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int floatCount = bytes.length / 4;
        float[] arr = new float[floatCount];

        for (int i = 0; i < floatCount; i++) {
            arr[i] = buffer.getFloat();
        }

        return arr;
    }

    public static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) {
            return 0;
        }

        double dot = 0, normA = 0, normB = 0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
