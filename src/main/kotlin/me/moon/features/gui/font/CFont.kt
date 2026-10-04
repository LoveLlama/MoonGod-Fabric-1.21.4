package me.moon.features.gui.font

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

open class CFont(font: Font, antiAlias: Boolean, fractionalMetrics: Boolean) {
    protected val imgSize = 512.0f
    protected var charData = arrayOfNulls<CharData>(256)
    @JvmField
    protected var font: Font = font

    @JvmField
    protected var antiAlias: Boolean = antiAlias

    @JvmField
    protected var fractionalMetrics: Boolean = fractionalMetrics
    protected var fontHeight = -1
    protected var charOffset = 0
    protected var textureId: Identifier? = null
    protected var activeTex: Identifier? = null
    protected var colorR = 1.0f
    protected var colorG = 1.0f
    protected var colorB = 1.0f
    protected var colorA = 1.0f

    init {
        this.textureId = setupTexture(font, antiAlias, fractionalMetrics, this.charData)
        this.activeTex = this.textureId
    }

    protected fun setupTexture(font: Font, antiAlias: Boolean, fractionalMetrics: Boolean, chars: Array<CharData?>): Identifier? {
        val img = generateFontImage(font, antiAlias, fractionalMetrics, chars)
        return try {
            val native = NativeImage(NativeImage.Format.RGBA, 512, 512, false)
            for (yy in 0 until 512) {
                for (xx in 0 until 512) {
                    native.setColorArgb(xx, yy, img.getRGB(xx, yy))
                }
            }
            val tex = NativeImageBackedTexture(native)
            val id = Identifier.of("me", "dynamic/font_" + System.identityHashCode(chars))
            MinecraftClient.getInstance().textureManager.registerTexture(id, tex)
            id
        } catch (e: Exception) {
            null
        }
    }

    protected fun generateFontImage(font: Font, antiAlias: Boolean, fractionalMetrics: Boolean, chars: Array<CharData?>): BufferedImage {
        val imgSize = 512
        val bufferedImage = BufferedImage(imgSize, imgSize, 2)
        val g = bufferedImage.graphics as Graphics2D
        g.font = font
        g.color = Color(255, 255, 255, 0)
        g.fillRect(0, 0, imgSize, imgSize)
        g.color = Color.WHITE
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, if (fractionalMetrics) RenderingHints.VALUE_FRACTIONALMETRICS_ON else RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, if (antiAlias) RenderingHints.VALUE_TEXT_ANTIALIAS_ON else RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, if (antiAlias) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF)
        val fontMetrics = g.fontMetrics
        var charHeight = 0
        var positionX = 0
        var positionY = 1
        var i = 0
        while (i < chars.size) {
            val ch = (i).toChar()
            val cd = CharData()
            val dimensions: Rectangle2D = fontMetrics.getStringBounds(ch.toString(), g)
            cd.width = dimensions.bounds.width + 8
            cd.height = dimensions.bounds.height
            if (positionX + cd.width >= imgSize) {
                positionX = 0
                positionY += charHeight
                charHeight = 0
            }
            if (cd.height > charHeight) {
                charHeight = cd.height
            }
            cd.storedX = positionX
            cd.storedY = positionY
            if (cd.height > this.fontHeight) {
                this.fontHeight = cd.height
            }
            chars[i] = cd
            g.drawString(ch.toString(), positionX + 2, positionY + fontMetrics.ascent)
            positionX += cd.width
            i++
        }
        return bufferedImage
    }

    open fun drawChar(chars: Array<CharData?>, c: Char, x: Float, y: Float) {
        try {
            val cd = chars[c.code] ?: return
            drawQuad(x, y, cd.width / 2.0f, cd.height / 2.0f, cd.storedX.toFloat(), cd.storedY.toFloat(), cd.width.toFloat(), cd.height.toFloat())
        } catch (_: Exception) {
        }
    }

    protected fun drawQuad(x: Float, y: Float, width: Float, height: Float, srcX: Float, srcY: Float, srcWidth: Float, srcHeight: Float) {
        val ru = srcX / 512.0f
        val rv = srcY / 512.0f
        val rw = srcWidth / 512.0f
        val rh = srcHeight / 512.0f
        val tex = this.activeTex ?: return
        RenderSystemState.enable()
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX)
        RenderSystem.setShaderTexture(0, tex)
        RenderSystem.setShaderColor(this.colorR, this.colorG, this.colorB, this.colorA)
        val b = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE)
        b.vertex(x + width, y, 0.0f).texture(ru + rw, rv)
        b.vertex(x, y, 0.0f).texture(ru, rv)
        b.vertex(x, y + height, 0.0f).texture(ru, rv + rh)
        b.vertex(x, y + height, 0.0f).texture(ru, rv + rh)
        b.vertex(x + width, y + height, 0.0f).texture(ru + rw, rv + rh)
        b.vertex(x + width, y, 0.0f).texture(ru + rw, rv)
        BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.disable()
    }

    object RenderSystemState {
        fun enable() {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
        }
        fun disable() {
            RenderSystem.disableBlend()
        }
    }

    fun getStringHeight(text: String): Int {
        return getHeight()
    }

    fun getHeight(): Int {
        return (this.fontHeight - 8) / 2
    }

    open fun getStringWidth(text: String): Int {
        var width = 0
        for (c in text.toCharArray()) {
            if (c.code < this.charData.size && c.code >= 0) {
                width += this.charData[c.code]!!.width - 8 + this.charOffset
            }
        }
        return width / 2
    }

    fun isAntiAlias(): Boolean {
        return this.antiAlias
    }

    open fun setAntiAlias(antiAlias: Boolean) {
        if (this.antiAlias != antiAlias) {
            this.antiAlias = antiAlias
            this.textureId = setupTexture(this.font, antiAlias, this.fractionalMetrics, this.charData)
            this.activeTex = this.textureId
        }
    }

    fun isFractionalMetrics(): Boolean {
        return this.fractionalMetrics
    }

    open fun setFractionalMetrics(fractionalMetrics: Boolean) {
        if (this.fractionalMetrics != fractionalMetrics) {
            this.fractionalMetrics = fractionalMetrics
            this.textureId = setupTexture(this.font, this.antiAlias, fractionalMetrics, this.charData)
            this.activeTex = this.textureId
        }
    }

    fun getFont(): Font {
        return this.font
    }

    open fun setFont(font: Font) {
        this.font = font
        this.textureId = setupTexture(font, this.antiAlias, this.fractionalMetrics, this.charData)
        this.activeTex = this.textureId
    }

    class CharData {
        var width = 0
        var height = 0
        var storedX = 0
        var storedY = 0
    }
}
