package app.roborock.patches.librrcodec

import app.morphe.patcher.patch.rawResourcePatch
import app.roborock.patches.shared.Constants

private const val LIB_PATH = "lib/arm64-v8a/librrcodec.so"

/**
 * com.roborock.smart 4.74.04 (versionCode 100932).
 *
 **/
private const val CALL_SITE_VA = 0x4914cL
private val NOP = byteArrayOf(0x1F, 0x20, 0x03, 0xD5.toByte())

@Suppress("unused")
val bypassNativeSignature = rawResourcePatch(
    name = "Bypass native signature check",
    description = "NOPs the call into librrcodec.so's APK-signature verifier, " +
        "which otherwise killProcess()es the app.",
    default = true,
) {
    compatibleWith(Constants.ROBOROCK)

    execute {
        val lib = get(LIB_PATH)
        val bytes = lib.readBytes()

        val offset = vaToFileOffset(bytes, CALL_SITE_VA)

        val opcode = bytes[offset + 3].toInt() and 0xFF
        check(opcode in 0x94..0x97) {
            "Expected a BL at VA 0x${CALL_SITE_VA.toString(16)} " +
                "(file offset 0x${offset.toString(16)}), found opcode 0x%02X. Wrong build?".format(opcode)
        }

        NOP.copyInto(bytes, offset)
        lib.writeBytes(bytes)
    }
}

/** ELF64 VA -> file offset, via the PT_LOAD program headers. */
private fun vaToFileOffset(elf: ByteArray, va: Long): Int {
    fun u16(o: Int) = (elf[o].toInt() and 0xFF) or ((elf[o + 1].toInt() and 0xFF) shl 8)
    fun u32(o: Int) = u16(o) or (u16(o + 2) shl 16)
    fun u64(o: Int) = u32(o).toLong() or (u32(o + 4).toLong() shl 32)

    require(u32(0) == 0x464C457F) { "Not an ELF file" } // 0x7F 'E' 'L' 'F'

    val phoff = u64(0x20)       // e_phoff
    val phentsize = u16(0x36)   // e_phentsize
    val phnum = u16(0x38)       // e_phnum

    for (i in 0 until phnum) {
        val base = (phoff + i.toLong() * phentsize).toInt()
        if (u32(base) != 1) continue // PT_LOAD
        val pOffset = u64(base + 0x08)
        val pVaddr = u64(base + 0x10)
        val pFilesz = u64(base + 0x20)
        if (va >= pVaddr && va < pVaddr + pFilesz) return (pOffset + (va - pVaddr)).toInt()
    }
    error("VA 0x${va.toString(16)} is not inside any PT_LOAD segment")
}
