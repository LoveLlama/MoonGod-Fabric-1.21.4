package me.moon.features.gui.font

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.Identifier
import java.awt.Font
import java.util.ArrayList
import kotlin.math.max

class CustomFont(font: Font, antiAlias: Boolean, fractionalMetrics: Boolean) : CFont(font, antiAlias, fractionalMetrics) {
    private val colorCode = IntArray(32)
    protected var boldChars = arrayOfNulls<CharData>(256)
    protected var italicChars = arrayOfNulls<CharData>(256)
    protected var boldItalicChars = arrayOfNulls<CharData>(256)
    protected var texBold: Identifier? = null
    protected var texItalic: Identifier? = null
    protected var texItalicBold: Identifier? = null

    init {
        setupMinecraftColorcodes()
        setupBoldItalicIDs()
    }

    fun drawStringWithShadow(text: String, x: Double, y: Double, color: Int): Float {
        val shadowWidth = drawString(text, x + 1.0, y + 1.0, color, true)
        return max(shadowWidth, drawString(text, x, y, color, false))
    }

    fun drawString(text: String, x: Float, y: Float, color: Int): Float {
        return drawString(text, x.toDouble(), y.toDouble(), color, false)
    }

    fun drawCenteredStringWithShadow(text: String, x: Float, y: Float, color: Int): Float {
        return drawStringWithShadow(text, x - getStringWidth(text) / 2.0, y.toDouble(), color)
    }

    fun drawCenteredString(text: String, x: Float, y: Float, color: Int): Float {
        return drawString(text, x - getStringWidth(text) / 2.0f, y, color)
    }

    fun drawString(text: String, x: Double, y: Double, color: Int, shadow: Boolean): Float {
        var x = x
        var y = y
        x -= 1.0
        y -= 2.0
        if (text == null) {
            return 0.0f
        } else {
            var color = color
            if (color == 553648127) {
                color = 16777215
            }
            if (color and -67108864 == 0) {
                color = color or -16777216
            }
            if (shadow) {
                color = color and 16579836 shr 2 or (color and -16777216)
            }
            var currentData = this.charData
            val alpha = (color shr 24 and 0xFF) / 255.0f
            var bold = false
            var italic = false
            var strikethrough = false
            var underline = false
            val size = text.length
            this.colorR = (color shr 16 and 0xFF) / 255.0f
            this.colorG = (color shr 8 and 0xFF) / 255.0f
            this.colorB = (color and 0xFF) / 255.0f
            this.colorA = alpha
            this.activeTex = this.textureId

            var i = 0
            while (i < size) {
                val character = text[i]
                if (character.code == 167 && i < size) {
                    var colorIndex = 21
                    try {
                        colorIndex = "0123456789abcdefklmnor".indexOf(text[i + 1])
                    } catch (_: Exception) {
                    }
                    if (colorIndex >= 16) {
                        if (colorIndex != 16) {
                            if (colorIndex == 17) {
                                bold = true
                                if (italic) {
                                    this.activeTex = this.texItalicBold
                                    currentData = this.boldItalicChars
                                } else {
                                    this.activeTex = this.texBold
                                    currentData = this.boldChars
                                }
                            } else if (colorIndex == 18) {
                                strikethrough = true
                            } else if (colorIndex == 19) {
                                underline = true
                            } else if (colorIndex == 20) {
                                italic = true
                                if (bold) {
                                    this.activeTex = this.texItalicBold
                                    currentData = this.boldItalicChars
                                } else {
                                    this.activeTex = this.texItalic
                                    currentData = this.italicChars
                                }
                            } else if (colorIndex == 21) {
                                bold = false
                                italic = false
                                underline = false
                                strikethrough = false
                                this.colorR = (color shr 16 and 0xFF) / 255.0f
                                this.colorG = (color shr 8 and 0xFF) / 255.0f
                                this.colorB = (color and 0xFF) / 255.0f
                                this.colorA = alpha
                                this.activeTex = this.textureId
                                currentData = this.charData
                            }
                        }
                    } else {
                        bold = false
                        italic = false
                        underline = false
                        strikethrough = false
                        this.activeTex = this.textureId
                        currentData = this.charData
                        if (colorIndex < 0 || colorIndex > 15) {
                            colorIndex = 15
                        }
                        if (shadow) {
                            colorIndex += 16
                        }
                        val colorcode = this.colorCode[colorIndex]
                        this.colorR = (colorcode shr 16 and 0xFF) / 255.0f
                        this.colorG = (colorcode shr 8 and 0xFF) / 255.0f
                        this.colorB = (colorcode and 0xFF) / 255.0f
                        this.colorA = alpha
                    }
                    ++i
                } else if (character.code < currentData.size && character.code >= 0) {
                    drawChar(currentData, character, x.toFloat(), y.toFloat())
                    val cd = currentData[character.code]!!
                    if (strikethrough) {
                        drawLine(x, y + cd.height / 4.0, x + (cd.width - 8.0) / 2.0, y + cd.height / 4.0, 1.0f)
                    }
                    if (underline) {
                        drawLine(x, y + cd.height / 2.0 - 1.0, x + (cd.width - 8.0) / 2.0, y + cd.height / 2.0 - 1.0, 1.0f)
                    }
                    x += (cd.width - 8 + this.charOffset) / 2.0
                }
                i++
            }
            return x.toFloat()
        }
    }

    override fun getStringWidth(text: String): Int {
        if (text == null) {
            return 0
        } else {
            var width = 0
            var currentData = this.charData
            val size = text.length
            var i = 0
            while (i < size) {
                val character = text[i]
                if (character.code == 167 && i < size) {
                    val colorIndex = "0123456789abcdefklmnor".indexOf(character)
                    if (colorIndex < 16) {
                    } else if (colorIndex == 17) {
                        currentData = this.boldChars
                    } else if (colorIndex == 20) {
                        currentData = this.italicChars
                    } else if (colorIndex == 21) {
                        currentData = this.charData
                    }
                    ++i
                } else if (character.code < currentData.size && character.code >= 0) {
                    width += currentData[character.code]!!.width - 8 + this.charOffset
                }
                i++
            }
            return width / 2
        }
    }

    override fun setFont(font: Font) {
        super.setFont(font)
        setupBoldItalicIDs()
    }

    override fun setAntiAlias(antiAlias: Boolean) {
        super.setAntiAlias(antiAlias)
        setupBoldItalicIDs()
    }

    override fun setFractionalMetrics(fractionalMetrics: Boolean) {
        super.setFractionalMetrics(fractionalMetrics)
        setupBoldItalicIDs()
    }

    private fun setupBoldItalicIDs() {
        this.texBold = setupTexture(this.font.deriveFont(1), this.antiAlias, this.fractionalMetrics, this.boldChars)
        this.texItalic = setupTexture(this.font.deriveFont(2), this.antiAlias, this.fractionalMetrics, this.italicChars)
        this.texItalicBold = setupTexture(this.font.deriveFont(3), this.antiAlias, this.fractionalMetrics, this.boldItalicChars)
    }

    private fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR)
        RenderSystem.setShaderColor(this.colorR, this.colorG, this.colorB, this.colorA)
        RenderSystem.lineWidth(width)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        val b = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR)
        b.vertex(x.toFloat(), y.toFloat(), 0.0f).color(this.colorR, this.colorG, this.colorB, this.colorA)
        b.vertex(x1.toFloat(), y1.toFloat(), 0.0f).color(this.colorR, this.colorG, this.colorB, this.colorA)
        BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystem.disableBlend()
    }

    fun wrapWords(text: String, width: Double): List<String> {
        val finalWords: MutableList<String> = ArrayList()
        if (getStringWidth(text).toDouble() > width) {
            val words = text.split(" ").toTypedArray()
            var currentWord = ""
            var lastColorCode = '￿'
            for (word in words) {
                for (i in word.toCharArray().indices) {
                    val c = word[i]
                    if (c.code == 167 && i < word.toCharArray().size - 1) {
                        lastColorCode = word[i + 1]
                    }
                }
                if (getStringWidth(currentWord + word + " ").toDouble() < width) {
                    currentWord = currentWord + word + " "
                } else {
                    finalWords.add(currentWord)
                    currentWord = "§" + lastColorCode + word + " "
                }
            }
            if (currentWord.length > 0) {
                if (getStringWidth(currentWord).toDouble() < width) {
                    finalWords.add("§" + lastColorCode + currentWord + " ")
                    currentWord = ""
                } else {
                    for (s in formatString(currentWord, width)) {
                        finalWords.add(s)
                    }
                }
            }
        } else {
            finalWords.add(text)
        }
        return finalWords
    }

    fun formatString(string: String, width: Double): List<String> {
        val finalWords: MutableList<String> = ArrayList()
        var currentWord = ""
        var lastColorCode = '￿'
        val chars = string.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            if (c.code == 167 && i < chars.size - 1) {
                lastColorCode = chars[i + 1]
            }
            if (getStringWidth(currentWord + c).toDouble() < width) {
                currentWord += c
            } else {
                finalWords.add(currentWord)
                currentWord = "§" + lastColorCode + c
            }
        }
        if (currentWord.length > 0) {
            finalWords.add(currentWord)
        }
        return finalWords
    }

    private fun setupMinecraftColorcodes() {
        for (index in 0 until 32) {
            val noClue = (index shr 3 and 1) * 85
            var red = (index shr 2 and 1) * 170 + noClue
            var green = (index shr 1 and 1) * 170 + noClue
            var blue = (index shr 0 and 1) * 170 + noClue
            if (index == 6) {
                red += 85
            }
            if (index >= 16) {
                red /= 4
                green /= 4
                blue /= 4
            }
            this.colorCode[index] = (red and 0xFF shl 16) or (green and 0xFF shl 8) or (blue and 0xFF)
        }
    }
}
