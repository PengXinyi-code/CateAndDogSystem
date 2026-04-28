package com.fast.system.general.utils.file;


/**
 * 文件类型工具类
 *
 * @author fast
 */
public class FileTypeUtils
{
    /**
     * 获取文件类型
     * <p>
     * 例如: fast.txt, 返回: txt
     *
     * @param fileName 文件名
     * @return 后缀（不含".")
     */
    public static String getFileType(String fileName)
    {
        int separatorIndex = fileName.lastIndexOf(".");
        if (separatorIndex < 0)
        {
            return "";
        }
        return fileName.substring(separatorIndex + 1).toLowerCase();
    }
}
