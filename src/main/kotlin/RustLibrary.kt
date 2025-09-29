package dev.gobley.test.jninioperfcomparison

import java.io.File
import java.nio.Buffer
import java.nio.ByteBuffer

object RustLibrary {
    // This value can be configured in the Gradle script.
    private const val RESOURCE_PREFIX = "jvm"
    private const val LIBRARY_NAME = "jni_nio_perf_comparison"

    init {
        val mappedLibraryName = System.mapLibraryName(LIBRARY_NAME)

        // Extract the library file to a temporary location as in JNA so this works even when packaged as a .jar file.
        val isWindows = System.getProperty("os.name").startsWith("Windows")
        val librarySuffix = ".dll".takeIf { isWindows }
        val libraryFile = File.createTempFile(LIBRARY_NAME, librarySuffix)

        RustLibrary::class.java.classLoader!!
            .getResourceAsStream("$RESOURCE_PREFIX/$mappedLibraryName")!!
            .use { inputStream ->
                libraryFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

        @Suppress("UnsafeDynamicallyLoadedCode")
        Runtime.getRuntime().load(libraryFile.absolutePath)
    }
    
    @JvmStatic
    external fun testUsingJni(structs: Array<TheStruct>): Double

    @JvmStatic
    fun testUsingNio(structs: Array<TheStruct>): Double {
        val buffer = ByteBuffer.allocateDirect(structs.size * 12)
        for (struct in structs) {
            buffer.putInt(struct.first)
            buffer.putDouble(struct.second)
        }
        return testUsingNio(buffer, structs.size)
    }

    @JvmStatic
    private external fun testUsingNio(structs: Buffer, numStructs: Int): Double
}