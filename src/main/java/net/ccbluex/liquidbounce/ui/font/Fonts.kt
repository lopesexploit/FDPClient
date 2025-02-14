/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FDPClient.CLIENT_CLOUD
import net.ccbluex.liquidbounce.file.FileManager.fontsDir
import net.ccbluex.liquidbounce.ui.font.fontmanager.impl.SimpleFontRenderer
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer as CustomFontRenderer
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.URLRegistryUtils.FONTS
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.io.HttpUtils.Downloader
import net.ccbluex.liquidbounce.utils.io.extractZipTo
import net.ccbluex.liquidbounce.utils.io.jsonArray
import net.ccbluex.liquidbounce.utils.io.readJson
import net.ccbluex.liquidbounce.utils.io.writeJson
import net.minecraft.client.gui.FontRenderer
import java.awt.Font
import java.io.File
import kotlin.system.measureTimeMillis

data class FontInfo(val name: String, val size: Int = -1, val isCustom: Boolean = false)

object Fonts : MinecraftInstance {

    private val CUSTOM_FONT_REGISTRY = LinkedHashMap<FontInfo, CustomFontRenderer>()

    private val FONT_REGISTRY = LinkedHashMap<FontInfo, FontRenderer>()

    val minecraftFont: FontRenderer by lazy {
        mc.fontRendererObj
    }
    
    lateinit var font20: GameFontRenderer

    lateinit var fontSmall: GameFontRenderer

    lateinit var font35: GameFontRenderer

    lateinit var font40: GameFontRenderer

    lateinit var font72: GameFontRenderer

    lateinit var fontBold180: GameFontRenderer

    lateinit var fontSFUI35: GameFontRenderer

    lateinit var fontSFUI40: GameFontRenderer

    lateinit var fontIconXD85: GameFontRenderer

    lateinit var fontNovoAngularIcon85: GameFontRenderer

    lateinit var ICONFONT_20: SimpleFontRenderer
    lateinit var CheckFont_20: SimpleFontRenderer

    // NURSULTAN
    lateinit var Nursultan15: SimpleFontRenderer
    lateinit var Nursultan16: SimpleFontRenderer
    lateinit var Nursultan18: SimpleFontRenderer
    lateinit var Nursultan20: SimpleFontRenderer
    lateinit var Nursultan30: SimpleFontRenderer

    //INTER
    lateinit var InterMedium_14: SimpleFontRenderer
    lateinit var InterMedium_15: SimpleFontRenderer
    lateinit var InterMedium_16: SimpleFontRenderer
    lateinit var InterMedium_18: SimpleFontRenderer
    lateinit var InterMedium_20: SimpleFontRenderer

    lateinit var InterBold_15: SimpleFontRenderer
    lateinit var InterBold_18: SimpleFontRenderer
    lateinit var InterBold_20: SimpleFontRenderer
    lateinit var InterBold_26: SimpleFontRenderer
    lateinit var InterBold_30: SimpleFontRenderer

    lateinit var InterRegular_15: SimpleFontRenderer
    lateinit var InterRegular_35: SimpleFontRenderer
    lateinit var InterRegular_40: SimpleFontRenderer

    lateinit var fontTahomaSmall: GameFontRenderer

    private fun <T : FontRenderer> register(fontInfo: FontInfo, fontRenderer: T): T {
        FONT_REGISTRY[fontInfo] = fontRenderer
        return fontRenderer
    }

    private fun <T : CustomFontRenderer> registerCustomFont(fontInfo: FontInfo, fontRenderer: T): T {
        CUSTOM_FONT_REGISTRY[fontInfo] = fontRenderer
        return fontRenderer
    }

    fun loadFonts() {
        LOGGER.info("Start to load fonts.")

        val time = measureTimeMillis {
            downloadFonts()
            register(FontInfo(name = "Minecraft Font"), minecraftFont)
            
            font20 = register(FontInfo(name = "Roboto Medium", size = 20),
                getFontFromFile("Roboto-Medium.ttf", 20).asGameFontRenderer())
            fontSmall = register(FontInfo(name = "Roboto Medium", size = 30),
                getFontFromFile("Roboto-Medium.ttf", 30).asGameFontRenderer())
            font35 = register(FontInfo(name = "Roboto Medium", size = 35),
                getFontFromFile("Roboto-Medium.ttf", 35).asGameFontRenderer())
            font40 = register(FontInfo(name = "Roboto Medium", size = 40),
                getFontFromFile("Roboto-Medium.ttf", 40).asGameFontRenderer())
            font72 = register(FontInfo(name = "Roboto Medium", size = 72),
                getFontFromFile("Roboto-Medium.ttf", 72).asGameFontRenderer())
            fontBold180 = register(FontInfo(name = "Roboto Bold", size = 180),
                getFontFromFile("Roboto-Bold.ttf", 180).asGameFontRenderer())

            // SFUI
            fontSFUI35 = register(FontInfo(name = "sfui", size = 35),
                getFontFromFile("sfui.ttf", 35).asGameFontRenderer())
            fontSFUI40 = register(FontInfo(name = "sfui", size = 40),
                getFontFromFile("sfui.ttf", 40).asGameFontRenderer())
            // icons
            fontIconXD85 = register(FontInfo(name = "iconxd", size = 85),
                getFontFromFile("iconxd.ttf", 85).asGameFontRenderer())
            fontNovoAngularIcon85 = register(FontInfo(name = "novoangular", size = 85),
                getFontFromFile("novoangular.ttf", 85).asGameFontRenderer())

            ICONFONT_20 = registerCustomFont(FontInfo(name = "ICONFONT", size = 20),
                getFontFromFile("stylesicons.ttf", 20).asSimpleFontRenderer())

            CheckFont_20 = registerCustomFont(FontInfo(name = "Check Font", size = 20),
                getFontFromFile("check.ttf", 20).asSimpleFontRenderer())

            Nursultan15 = registerCustomFont(FontInfo(name = "Nursultan", size = 15),
                getFontFromFile("Nursultan.ttf", 15).asSimpleFontRenderer())
            Nursultan16 = registerCustomFont(FontInfo(name = "Nursultan", size = 16),
                getFontFromFile("Nursultan.ttf", 16).asSimpleFontRenderer())
            Nursultan18 = registerCustomFont(FontInfo(name = "Nursultan", size = 18),
                getFontFromFile("Nursultan.ttf", 18).asSimpleFontRenderer())
            Nursultan20 = registerCustomFont(FontInfo(name = "Nursultan", size = 20),
                getFontFromFile("Nursultan.ttf", 20).asSimpleFontRenderer())
            Nursultan30 = registerCustomFont(FontInfo(name = "Nursultan", size = 30),
                getFontFromFile("Nursultan.ttf", 30).asSimpleFontRenderer())

            InterMedium_14 = registerCustomFont(FontInfo(name = "InterMedium", size = 14),
                getFontFromFile("Inter_Medium.ttf", 14).asSimpleFontRenderer())
            InterMedium_15 = registerCustomFont(FontInfo(name = "InterMedium", size = 15),
                getFontFromFile("Inter_Medium.ttf", 15).asSimpleFontRenderer())
            InterMedium_16 = registerCustomFont(FontInfo(name = "InterMedium", size = 16),
                getFontFromFile("Inter_Medium.ttf", 16).asSimpleFontRenderer())
            InterMedium_18 = registerCustomFont(FontInfo(name = "InterMedium", size = 18),
                getFontFromFile("Inter_Medium.ttf", 18).asSimpleFontRenderer())
            InterMedium_20 = registerCustomFont(FontInfo(name = "InterMedium", size = 20),
                getFontFromFile("Inter_Medium.ttf", 20).asSimpleFontRenderer())

            InterBold_15 = registerCustomFont(FontInfo(name = "InterBold", size = 15),
                getFontFromFile("Inter_Bold.ttf", 15).asSimpleFontRenderer())
            InterBold_18 = registerCustomFont(FontInfo(name = "InterBold", size = 18),
                getFontFromFile("Inter_Bold.ttf", 18).asSimpleFontRenderer())
            InterBold_20 = registerCustomFont(FontInfo(name = "InterBold", size = 20),
                getFontFromFile("Inter_Bold.ttf", 20).asSimpleFontRenderer())
            InterBold_26 = registerCustomFont(FontInfo(name = "InterBold", size = 26),
                getFontFromFile("Inter_Bold.ttf", 26).asSimpleFontRenderer())
            InterBold_30 = registerCustomFont(FontInfo(name = "InterBold", size = 30),
                getFontFromFile("Inter_Bold.ttf", 30).asSimpleFontRenderer())

            InterRegular_15 = registerCustomFont(FontInfo(name = "InterRegular", size = 15),
                getFontFromFile("Inter_Regular.ttf", 15).asSimpleFontRenderer())
            InterRegular_35 = registerCustomFont(FontInfo(name = "InterRegular", size = 35),
                getFontFromFile("Inter_Regular.ttf", 35).asSimpleFontRenderer())
            InterRegular_40 = registerCustomFont(FontInfo(name = "InterRegular", size = 40),
                getFontFromFile("Inter_Regular.ttf", 40).asSimpleFontRenderer())

            fontTahomaSmall = register(FontInfo(name = "Tahoma", size = 18),
                getFontFromFile("Tahoma.ttf", 18).asGameFontRenderer())


            loadCustomFonts()
        }
        LOGGER.info("Loaded ${FONT_REGISTRY.size} fonts in ${time}ms")
    }

    private fun loadCustomFonts() {
        FONT_REGISTRY.keys.removeIf { it.isCustom }

        File(fontsDir, "fonts.json").apply {
            if (exists()) {
                val jsonElement = readJson()

                if (jsonElement !is JsonArray) return@apply

                for (element in jsonElement) {
                    if (element !is JsonObject) return@apply

                    val font = getFontFromFile(element["fontFile"].asString, element["fontSize"].asInt)

                    FONT_REGISTRY[FontInfo(font.name, font.size, isCustom = true)] = GameFontRenderer(font)
                }
            } else {
                createNewFile()
                writeJson(jsonArray())
            }
        }
    }

    fun downloadFonts() {
        val robotoZipFile = File(fontsDir, "roboto.zip")
        if (!robotoZipFile.exists()) {
            LOGGER.info("Downloading roboto fonts...")
            Downloader.downloadWholeFile("$CLIENT_CLOUD/fonts/Roboto.zip", robotoZipFile)
            LOGGER.info("Extract roboto fonts...")
            robotoZipFile.extractZipTo(fontsDir)
        }

        val fontZipFile = File(fontsDir, "font.zip")
        if (!fontZipFile.exists()) {
            LOGGER.info("Downloading additional fonts...")
            Downloader.downloadWholeFile("${FONTS}/Font.zip", fontZipFile)
        }

        if(fontZipFile.exists()){
            LOGGER.info("Font zip file exists, trying to extract...")
            if(!fontsDir.exists()){
                LOGGER.info("Fonts directory does not exist, trying to create...")
                fontsDir.mkdirs()
            }
            try{
                fontZipFile.extractZipTo(fontsDir){file ->
                    LOGGER.info("Extracted: ${file.absolutePath}")
                }
                val extractedFiles = fontsDir.listFiles { file -> file.isFile && file.name.endsWith(".ttf") }
                if (extractedFiles != null && extractedFiles.isNotEmpty()) {
                    LOGGER.info("Fonts extracted successfully:")
                    extractedFiles.forEach{file ->
                        LOGGER.info(" - ${file.absolutePath}")
                    }
                }else {
                    LOGGER.warn("No .ttf files extracted")
                }
            }catch (e:Exception){
                LOGGER.error("Error during extraction", e)
            }

        }else{
            LOGGER.warn("font not found")
        }

    }

    fun getFontRenderer(name: String, size: Int): FontRenderer {
        return FONT_REGISTRY.entries.firstOrNull { (fontInfo, _) ->
            fontInfo.size == size && fontInfo.name.equals(name, true)
        }?.value ?: minecraftFont
    }

    fun getFontDetails(fontRenderer: FontRenderer): FontInfo? {
        return FONT_REGISTRY.keys.firstOrNull { FONT_REGISTRY[it] == fontRenderer }
    }

    val fonts: List<FontRenderer>
        get() = FONT_REGISTRY.values.toList()

    private fun getFontFromFile(fontName: String, size: Int): Font = try {
        File(fontsDir, fontName).inputStream().use { inputStream ->
            Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(Font.PLAIN, size.toFloat())
        }
    } catch (e: Exception) {
        LOGGER.warn("Exception during loading font[name=${fontName}, size=${size}]", e)
        Font("default", Font.PLAIN, size)
    }

    private fun Font.asGameFontRenderer(): GameFontRenderer {
        return GameFontRenderer(this@asGameFontRenderer)
    }

    private fun Font.asSimpleFontRenderer(): SimpleFontRenderer {
        return SimpleFontRenderer.create(this) as SimpleFontRenderer
    }
}