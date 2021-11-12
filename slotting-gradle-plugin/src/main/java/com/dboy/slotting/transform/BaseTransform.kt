package com.dboy.slotting.transform

import com.android.build.api.transform.*
import com.android.build.api.transform.Status.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.dboy.slotting.concurrent.Schedulers
import com.dboy.slotting.concurrent.Worker
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * - 文件描述: 重新封装的Transform
 * @author DBoy
 * @since 2021/11/11 16:01
 */
abstract class BaseTransform : Transform() {

    /**
     * 异步处理任务
     */
    private val worker: Worker = Schedulers.IO()

    override fun getName(): String = this.javaClass.simpleName

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        TransformManager.SCOPE_FULL_PROJECT

    override fun isIncremental(): Boolean = true

    override fun transform(transformInvocation: TransformInvocation?) {
        //通获取所有输入，是一个集合，TransformInput代表一个输入
        val transformInputs: Collection<TransformInput> = transformInvocation?.inputs ?: return
        //获取输出的提供者，通过TransformOutputProvider可以创建Transform的输出
        val outputProvider: TransformOutputProvider = transformInvocation.outputProvider ?: return
        //判断本次Transform任务是否是增量，如果Transform的[isIncremental()]方法返回false，TransformInvocation的isIncremental方法永远返回false
        val isIncremental: Boolean = transformInvocation.isIncremental

        println("开始transform isIncremental = $isIncremental")

        //如果不是增量，就删除之前所有产生的输出，重头来过
        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        //记录开始时间
        val startTime = System.currentTimeMillis()

        //遍历所有输入
        transformInputs.forEach { transformInput ->

            //获取这个输入中的所有jar文件输入，JarInput代表一个jar文件输入
            transformInput.jarInputs.forEach { jarInput ->
                //获取输入的文件
                val srcJarFile = jarInput.file
                //使用TransformOutputProvider的getContentLocation方法根据输入构造输出位置
                val destJarFile = outputProvider.getContentLocation(
                    jarInput.name,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                if (isIncremental) {
                    //增量处理Jar文件
                    val status = jarInput.status
                    worker.execute {
                        handleJarIncremental(srcJarFile, destJarFile, status)
                    }
                } else {
                    //非增量处理Jar文件
                    worker.execute {
                        handleJar(srcJarFile, destJarFile)
                    }
                }
            }

            transformInput.directoryInputs.forEach { directoryInput ->
                //输入的原文件夹
                val srcDirectory = directoryInput.file
                //根据input信息创建相同的output文件夹
                val destDirectory = outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                FileUtils.forceMkdir(destDirectory)
                if (isIncremental) {
                    //通过DirectoryInput的getChangedFiles方法获取改变过的文件集合，每一个文件对应一个Status
                    val changeFiles = directoryInput.changedFiles
                    worker.execute {
                        //增量处理目录文件
                        handleDirectoryIncremental(srcDirectory, destDirectory, changeFiles)
                    }
                } else {
                    worker.execute {
                        //非增量处理目录文件
                        handleDirectory(srcDirectory, destDirectory)
                    }
                }
            }
        }
        worker.await()

        println("${name}——结束,耗时:${System.currentTimeMillis() - startTime}")
    }

    /**
     * 处理directory目录中的class文件，产生新的输出
     */
    private fun handleDirectory(
        srcDirectory: File,
        destDirectory: File
    ) {
        //递归地遍历srcDirectory的所有文件, 在遍历的过程中把srcDirectory中的文件逐个地复制到destDirectory，
        //如果发现这个文件是class文件，就把它通过asm修改后再复制到destDirectory中
        foreachDirectoryFile(srcDirectory) { srcFile ->
            val destFile = getDestFile(srcFile, srcDirectory, destDirectory)
            val srcPath = srcDirectory.absolutePath
            transformSingleFile(srcFile, destFile, srcPath)
        }
    }

    /**
     * 增量处理directory目录中的class文件，可能产生新的输出
     */
    private fun handleDirectoryIncremental(
        srcDirectory: File,
        destDirectory: File,
        changeFiles: Map<File, Status>
    ) {
        changeFiles.forEach { (inputFile, status) ->
            //根据文件的Status做出不同的操作
            val destFile = getDestFile(inputFile, srcDirectory, destDirectory)
            when {
                status == REMOVED -> {
                    //状态是移除，直接删了
                    FileUtils.forceDelete(destFile)
                }
                checkHandleIncrementalForStatus(status) -> {
                    val srcPath = srcDirectory.absolutePath
                    //根据状态检查判断是否需要处理这个文件
                    transformSingleFile(
                        inputFile,
                        destFile,
                        srcPath
                    )
                }
            }
        }
    }

    /**
     * (srcFile -> destFile)
     * 把srcFile文件复制到destFile中，如果srcFile是class文件，则把它经过asm修改后再复制到destFile中
     */
    private fun transformSingleFile(srcFile: File, destFile: File, srcPath: String) {
        if (srcFile.isFile) {
            var rootPath = srcPath
            if (!rootPath.endsWith(File.separator)) rootPath += File.separator
            try {
                //创建输出文件用于写入信息
                FileUtils.touch(destFile)
            } catch (e: IOException) {
                //也许 mkdirs 因某些奇怪的原因失败，请再试一次。
                FileUtils.forceMkdirParent(destFile)
            }
            //是否对这个是Class的文件进行代码织入
            val isWeave = srcFile.name.endsWith(".class") && isWeaveClass(
                srcFile.absolutePath.replace(rootPath, "").replace(File.separator, ".")
            )
            if (isWeave) {
                FileInputStream(srcFile).use { srcFileIs ->
                    FileOutputStream(destFile).use { destFileOs ->
                        //进行代码织入，并将织入后的代码转成bytes
                        //将转换后的字节写入输出文件中
                        destFileOs.write(weaveCode(srcFileIs))
                    }
                }
            } else {
                //不织入代码将文件直接拷贝过去
                FileUtils.copyFile(srcFile, destFile)
            }
        }
    }

    /**
     * 构造srcFile在destDirectory中对应的destFile
     */
    private fun getDestFile(srcFile: File, srcDirectory: File, destDirectory: File): File {
        val srcDirPath = srcDirectory.absolutePath
        val destDirPath = destDirectory.absolutePath
        //找到源输入文件对应的输出文件位置
        val destFilePath = srcFile.absolutePath.replace(srcDirPath, destDirPath)
        //构造源输入文件对应的输出文件
        return File(destFilePath)
    }

    /**
     * (srcJar -> destJar）:
     * 遍历srcJar的所有内容，把srcJar中的内容条目一条一条地复制到destJar中,
     * 如果发现这个内容条目是class文件，就把它通过asm修改后再复制到destJar中
     */
    private fun handleJar(srcJar: File?, destJar: File?) {
        srcJar ?: return
        destJar ?: return
        //创建Jar文件
        val srcJarFile = JarFile(srcJar)
        //获取Jar中所有文件
        val srcJarFilesEnum = srcJarFile.entries()
        //获取Jar文件输出流
        JarOutputStream(FileOutputStream(destJar)).use { destJarFileOs ->
            //遍历Jar中的文件
            while (srcJarFilesEnum.hasMoreElements()) {
                //获取Jar中的一个文件
                val srcEntry = srcJarFilesEnum.nextElement()
                //通过Jar获取Jar内的指定文件的输入流
                srcJarFile.getInputStream(srcEntry).use { entryIs ->
                    val bytes: ByteArray =
                        if (srcEntry.name.endsWith(".class") && isWeaveClass(
                                srcEntry.name.replace(
                                    '/',
                                    '.'
                                )
                            )
                        ) {
                            //如果是class文件,判断是否需要对这个class进行织入
                            //然后把修改后的class转成Bytes复制到destJar中
                            weaveCode(entryIs)
                        } else {
                            //如果不是class文件或者不是想要织入的class
                            //原封不动地复制到destJar中
                            IOUtils.toByteArray(entryIs)
                        }
                    //创建输出文件
                    val destEntry = JarEntry(srcEntry.name)
                    //放入输出口
                    destJarFileOs.putNextEntry(destEntry)
                    //写入到Jar中
                    destJarFileOs.write(bytes)
                    //关闭输出口
                    destJarFileOs.closeEntry()
                }
            }
        }
    }


    /**
     * 增量处理jar文件中的class文件，可能产生新的输出
     * @param srcJarFile 原始Jar文件
     * @param desJarFile 保存Jar文件
     * @param status [srcJarFile]的状态
     */
    private fun handleJarIncremental(
        srcJarFile: File,
        desJarFile: File,
        status: Status
    ) {
        when {
            status == REMOVED -> {
                //如果文件状态是移除，直接删除所有文件
                FileUtils.forceDelete(desJarFile)
            }
            checkHandleIncrementalForStatus(status) -> {
                //如果经过状态判断需要对jar处理
                //遍历srcJar的所有内容, 在遍历的过程中把srcJar中的内容一条一条地复制到destJar
                //如果发现这个内容条目是class文件，就把它通过asm修改后再复制到destJar中
                handleJar(srcJarFile, desJarFile)
            }
        }
    }


    /**
     * 检查是否对这个状态的文件进行处理
     * - [fileState] 当状态为[Status.REMOVED]时，是不会调用此方法。因为这个状态下会直接删除文件。
     */
    protected open fun checkHandleIncrementalForStatus(fileState: Status): Boolean {
        return when (fileState) {
            ADDED,
            CHANGED -> {
                true
            }
            REMOVED -> {
                false
            }
            NOTCHANGED -> {
                false
            }
        }
    }

    /**
     * 递归遍历文件夹
     * @param file 文件夹/文件
     * @param action 扫描到得文件会通过此闭包函数回调
     */
    private fun foreachDirectoryFile(file: File?, action: (file: File) -> Unit) {
        if (file?.isFile == true) {
            action(file)
            return
        } else if (file?.isDirectory == true) {
            file.listFiles()?.map {
                if (it?.isFile == true) {
                    action(it)
                } else if (it?.isDirectory == true) {
                    foreachDirectoryFile(it, action)
                }
            }
        }
    }

    /**
     * 织入代码
     *
     * @param inputStream 源文件输入
     */
    protected abstract fun weaveCode(inputStream: InputStream): ByteArray

    /**
     * 是否要对这个class进行织入
     *
     * @param name class name = {com.simple.XXX.class}
     */
    protected abstract fun isWeaveClass(name: String): Boolean
}