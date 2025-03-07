package com.github.ol_loginov.heaplibweb.hprof

import java.io.*
import java.nio.file.Path
import java.util.*
import kotlin.io.path.inputStream
import kotlin.math.min

private const val MAX_BUFFER_SIZE = Int.MAX_VALUE - 8
private const val DEFAULT_BUFFER_SIZE = 8192

interface HprofFileSource : DataInput, AutoCloseable {
    fun available(): Int
    fun readNBytes(len: Int): ByteArray
    fun skipNBytes(n: Long)
}

class RAFFileSource(file: Path) : RandomAccessFile(file.toFile(), "r"), HprofFileSource {
    private val raf = RandomAccessFile(file.toFile(), "r")

    override fun available(): Int = (raf.length() - raf.filePointer).toInt()
    override fun readNBytes(len: Int): ByteArray {
        require(len >= 0) { "len < 0" }

        var buffers: MutableList<ByteArray>? = null
        var result: ByteArray? = null
        var total = 0
        var remaining: Int = len
        var n: Int
        do {
            var buf = ByteArray(min(remaining, DEFAULT_BUFFER_SIZE))
            var nread = 0

            // read to EOF which may read more or less than buffer size
            while (raf.read(buf, nread, min(buf.size - nread, remaining)).also { n = it } > 0) {
                nread += n
                remaining -= n
            }
            if (nread > 0) {
                if (MAX_BUFFER_SIZE - total < nread) {
                    throw OutOfMemoryError("Required array size too large")
                }
                if (nread < buf.size) {
                    buf = buf.copyOfRange(0, nread)
                }
                total += nread
                if (result == null) {
                    result = buf
                } else {
                    if (buffers == null) {
                        buffers = ArrayList()
                        buffers.add(result)
                    }
                    buffers.add(buf)
                }
            }
            // if the last call to read returned -1 or the number of bytes
            // requested have been read then break
        } while (n >= 0 && remaining > 0)

        if (buffers == null) {
            if (result == null) {
                return ByteArray(0)
            }
            return if (result.size == total) result else Arrays.copyOf(result, total)
        }

        result = ByteArray(total)
        var offset = 0
        remaining = total
        for (b in buffers) {
            val count = min(b.size, remaining)
            System.arraycopy(b, 0, result, offset, count)
            offset += count
            remaining -= count
        }

        return result
    }

    override fun skipNBytes(n: Long) {
        var rest = n
        while (rest > 0) {
            if (available() <= 0) {
                throw EOFException()
            }
            rest -= skipBytes(rest.toInt())
        }
    }
}

class DISFileSource(file: Path, bufferSize: Int) : DataInputStream(BufferedInputStream(file.inputStream(), bufferSize)), HprofFileSource
