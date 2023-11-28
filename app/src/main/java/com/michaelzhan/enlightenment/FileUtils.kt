package com.michaelzhan.enlightenment

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object FileUtils {
    private const val tag = "FileUtils"

    /**
     * 是否有拷贝出错的文件
     */
    var hasCopyFailedFile = false
    /**
     * 拷贝assets目录中的文件到sd卡
     * @param context 上下文
     * @param assetsPath assets 文件夹中的目录
     * @param dstPath sd卡磁盘目录
     */
    fun copyAssetsToSD(assetsPath: String, dstPath: String, blockPath: ArrayList<String>? = null): Boolean {
        hasCopyFailedFile = false
        copyAssetsToDst(EnlightenmentApplication.context, assetsPath, dstPath, blockPath)
        return !hasCopyFailedFile
    }

    /**
     * 递归拷贝Assets文件文件到SD卡
     * @param context 上下文
     * @param assetsPath assets 文件夹中的目录
     * @param dstPath sd卡磁盘目录
     */
    private fun copyAssetsToDst(context: Context, assetsPath: String, dstPath: String, blockPath: ArrayList<String>? = null) {
        try {
            Log.d(tag, "$assetsPath $dstPath")
            val assetsFileNames = context.assets.list(assetsPath)
            if (assetsFileNames != null) {
                for (i in assetsFileNames) {
                    Log.d("adsf", i.toString())
                }
            }
            val dstFile = File(dstPath)
            if (!assetsFileNames.isNullOrEmpty()) {
                if (!dstFile.exists()) {
                    dstFile.mkdirs()
                }

                for (assetsFileName in assetsFileNames) {
                    Log.d(tag, assetsFileName)
                    if (blockPath?.contains(assetsFileName) != true) {
                        if (assetsPath != "") {
                            // assets 文件夹下的目录
                            copyAssetsToDst(context, assetsPath + File.separator + assetsFileName, dstPath + File.separator + assetsFileName)
                        } else {
                            // assets 文件夹
                            copyAssetsToDst(context, assetsFileName, dstPath + File.separator + assetsFileName)
                        }
                    }
                }
            } else {
                if (!dstFile.exists()) {
                    //当文件不存在的时候copy
                    val inputStream = context.assets.open(assetsPath)
                    val fileOutputStream = FileOutputStream(dstFile)
                    val buffer = ByteArray(1024)
                    var byteCount: Int
                    while (inputStream.read(buffer).also { byteCount = it } != -1) {
                        fileOutputStream.write(buffer, 0, byteCount)
                    }
                    fileOutputStream.flush()
                    inputStream.close()
                    fileOutputStream.close()
                } else {
                    Log.d("copyAssetsToDst", "文件已经存在:${dstFile.path}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            hasCopyFailedFile = true
        }
    }


    /**
     * 创建文件夹
     */
    private fun copyFolderToSD(folderFileName: String, sdUrl: String) {
        val file = File(sdUrl, folderFileName)
        if (!file.exists()) {
            file.mkdir()
        }
    }


    /**
     * 删除工作
     * @param url  需要删除内容的路径。。。
     */
    private fun delFile(url: String) {
        println("111")
        val file = File(url)
        if (!file.exists()) {
            return
        }
        val delFileList = file.list() as Array<String>
        for (i in delFileList.indices) {
            val childFileUrl = url + "/" + delFileList[i]
            val childFile = File(childFileUrl)
            if (childFile.isFile) { //判断是否是文件
                childFile.delete()
            } else {
                //不是文件
                delFile(childFileUrl)
            }
            //删除当前子文件夹
            childFile.delete()
        }
    }

    fun isTwoFileSame(path1: String, path2: String) = file2MD5(File(path1)) == file2MD5(File(path2))

    fun file2MD5(file: File?): String {
        try {
            val hash: ByteArray
            val buffer = ByteArray(8192)
            val md: MessageDigest = MessageDigest.getInstance("MD5")
            val fis = FileInputStream(file)
            var len: Int
            while (fis.read(buffer).also { len = it } != -1) {
                md.update(buffer, 0, len)
            }
            hash = md.digest()

            //对生成的16字节数组进行补零操作
            val hex = StringBuilder(hash.size * 2)
            for (b in hash) {
                if (b.toInt() and 0xFF < 0x10) {
                    hex.append("0")
                }
                hex.append(Integer.toHexString(b.toInt() and 0xFF))
            }
            return hex.toString()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("NoSuchAlgorithmException", e)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("UnsupportedEncodingException", e)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun delete(fileName: String): Boolean {
        val file = File(fileName)
        return if (!file.exists()) {
            println("删除文件失败:" + fileName + "不存在！")
            false
        } else {
            if (file.isFile) deleteFile(fileName) else deleteDirectory(fileName)
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName
     * 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private fun deleteFile(fileName: String): Boolean {
        val file = File(fileName)
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        return if (file.exists() && file.isFile) {
            if (file.delete()) {
                println("删除单个文件" + fileName + "成功！")
                true
            } else {
                println("删除单个文件" + fileName + "失败！")
                false
            }
        } else {
            println("删除单个文件失败：" + fileName + "不存在！")
            false
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir
     * 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private fun deleteDirectory(dir: String): Boolean {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        var dir = dir
        if (!dir.endsWith(File.separator)) dir += File.separator
        val dirFile = File(dir)
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory) {
            println("删除目录失败：" + dir + "不存在！")
            return false
        }
        var flag = true
        // 删除文件夹中的所有文件包括子目录
        val files = dirFile.listFiles()
        for (i in files.indices) {
            // 删除子文件
            if (files[i].isFile) {
                flag = deleteFile(files[i].absolutePath)
                if (!flag) break
            } else if (files[i].isDirectory) {
                flag = deleteDirectory(files[i].absolutePath)
                if (!flag) break
            }
        }
        if (!flag) {
            println("删除目录失败！")
            return false
        }
        // 删除当前目录
        return if (dirFile.delete()) {
            println("删除目录" + dir + "成功！")
            true
        } else {
            false
        }
    }

    fun saveBitmap(bitmap: Bitmap, path: String) {
        val file = File(path)
        try {
            val out = FileOutputStream(file)
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush()
                out.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun writeInternal(filepath: String, content: String) {
        //获取文件在内存卡中files目录下的路径

        try {
            //打开文件输出流
            val outputStream = FileOutputStream(filepath)

            //写数据到文件中
            outputStream.write(content.toByteArray())
            outputStream.close()
        }catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readInternal(filepath: String): String {
        val sb = StringBuilder("")

        //获取文件在内存卡中files目录下的路径

        try {
            //打开文件输入流
            val inputStream = FileInputStream(filepath)
            val buffer = ByteArray(1024)
            var len = inputStream.read(buffer)
            //读取文件内容
            while (len > 0) {
                sb.append(String(buffer, 0, len))

                //继续将数据放到buffer中
                len = inputStream.read(buffer)
            }
            //关闭输入流
            inputStream.close()
        }catch (e: IOException) {
            e.printStackTrace()
        }
        return sb.toString()
    }
}