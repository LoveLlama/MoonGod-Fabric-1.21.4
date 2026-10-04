package me.moon.util

import me.moon.Moon
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import java.awt.Color

object RenderUtil : Util {
    val itemRender: ItemRenderer
        get() = mc.itemRenderer

    var camera: Frustum = Frustum(Matrix4f(), Matrix4f())

    private var depth = false
    private var texture = false
    private var clean = false
    private var bind = false
    private var override = false

    @JvmStatic
    fun drawRectangleCorrectly(x: Int, y: Int, w: Int, h: Int, color: Int) {
        drawRect(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), color)
    }

    @JvmStatic
    fun interpolateAxis(bb: Box): Box {
        val cam = mc.gameRenderer.camera.pos
        return Box(
            bb.minX - cam.x,
            bb.minY - cam.y,
            bb.minZ - cam.z,
            bb.maxX - cam.x,
            bb.maxY - cam.y,
            bb.maxZ - cam.z
        )
    }

    @JvmStatic
    fun isInViewFrustrum(entity: Entity): Boolean {
        return isInViewFrustrum(entity.boundingBox)
    }

    @JvmStatic
    fun isInViewFrustrum(bb: Box): Boolean {
        val current = mc.cameraEntity ?: return false
        camera.setPosition(current.x, current.y, current.z)
        return camera.isVisible(bb)
    }

    @JvmStatic
    fun to2D(x: Double, y: Double, z: Double): Vec3d? {
        val projection = RenderUtilAccess.projectionMatrix
        val modelview = RenderUtilAccess.modelViewMatrix
        val vec = doubleArrayOf(x, y, z, 1.0)
        var out = floatArrayOf(0f, 0f, 0f, 0f)
        out = matrixVec(modelview, vec, out)
        out = matrixVec(projection, doubleArrayOf(out[0].toDouble(), out[1].toDouble(), out[2].toDouble(), out[3].toDouble()), out)
        if (out[3] == 0f) return null
        val ndcX = out[0] / out[3]
        val ndcY = out[1] / out[3]
        val ndcZ = out[2] / out[3]
        val window = mc.window ?: return null
        val w = window.scaledWidth.toDouble()
        val h = window.scaledHeight.toDouble()
        return Vec3d((ndcX * 0.5 + 0.5) * w, (1.0 - (ndcY * 0.5 + 0.5)) * h, (ndcZ * 0.5 + 0.5))
    }

    private fun matrixVec(m: Matrix4f, v: DoubleArray, out: FloatArray): FloatArray {
        val x = v[0].toFloat(); val y = v[1].toFloat(); val z = v[2].toFloat(); val w = v[3].toFloat()
        out[0] = m.m00() * x + m.m10() * y + m.m20() * z + m.m30() * w
        out[1] = m.m01() * x + m.m11() * y + m.m21() * z + m.m31() * w
        out[2] = m.m02() * x + m.m12() * y + m.m22() * z + m.m32() * w
        out[3] = m.m03() * x + m.m13() * y + m.m23() * z + m.m33() * w
        return out
    }

    @JvmStatic
    fun drawTracerPointer(x: Float, y: Float, size: Float, widthDiv: Float, heightDiv: Float, outline: Boolean, outlineWidth: Float, color: Int) {
        val alpha = (color ushr 24 and 0xFF) / 255.0f
        RenderSystemState.enableBlend()
        RenderSystemState.setShaderColor(color)
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.TRIANGLE_FAN, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val bl = (color and 0xFF) / 255.0f
        b.vertex(x.toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, alpha)
        b.vertex((x - size / widthDiv).toDouble().toFloat(), (y + size).toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, alpha)
        b.vertex(x.toDouble().toFloat(), (y + size / heightDiv).toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, alpha)
        b.vertex((x + size / widthDiv).toDouble().toFloat(), (y + size).toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, alpha)
        b.vertex(x.toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, alpha)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        if (outline) {
            com.mojang.blaze3d.systems.RenderSystem.lineWidth(outlineWidth)
            val b2 = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINE_STRIP, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
            b2.vertex(x.toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(0f, 0f, 0f, alpha)
            b2.vertex((x - size / widthDiv).toDouble().toFloat(), (y + size).toDouble().toFloat(), 0.0.toFloat()).color(0f, 0f, 0f, alpha)
            b2.vertex(x.toDouble().toFloat(), (y + size / heightDiv).toDouble().toFloat(), 0.0.toFloat()).color(0f, 0f, 0f, alpha)
            b2.vertex((x + size / widthDiv).toDouble().toFloat(), (y + size).toDouble().toFloat(), 0.0.toFloat()).color(0f, 0f, 0f, alpha)
            b2.vertex(x.toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(0f, 0f, 0f, alpha)
            net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b2.end())
        }
        RenderSystemState.resetColor()
    }

    @JvmStatic
    fun hexColor(hexColor: Int) {
        RenderSystemState.setShaderColor(hexColor)
    }

    @JvmStatic
    fun drawTexturedRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int, zLevel: Int) {
        RenderSystemState.setTextureShader()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_TEXTURE)
        val f = 0.00390625f
        b.vertex(x.toFloat(), (y + height).toFloat(), zLevel.toFloat()).texture((textureX * f), ((textureY + height) * f))
        b.vertex((x + width).toFloat(), (y + height).toFloat(), zLevel.toFloat()).texture(((textureX + width) * f), ((textureY + height) * f))
        b.vertex((x + width).toFloat(), y.toFloat(), zLevel.toFloat()).texture(((textureX + width) * f), (textureY * f))
        b.vertex(x.toFloat(), y.toFloat(), zLevel.toFloat()).texture((textureX * f), (textureY * f))
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
    }

    @JvmStatic
    fun blockESP(b: BlockPos, c: Color, length: Double, length2: Double) {
        blockEsp(b, c, length, length2)
    }

    @JvmStatic
    fun drawBoxESP(
        pos: BlockPos, color: Color, secondC: Boolean, secondColor: Color, lineWidth: Float, outline: Boolean, box: Boolean, boxAlpha: Int, air: Boolean
    ) {
        if (box) {
            drawBox(pos, Color(color.red, color.green, color.blue, boxAlpha))
        }

        if (outline) {
            drawBlockOutline(pos, if (secondC) secondColor else color, lineWidth, air)
        }
    }

    @JvmStatic
    fun glScissor(x: Float, y: Float, x1: Float, y1: Float, sr: ScaledResolutionProxy) {
        val scale = sr.scaleFactor.toFloat()
        com.mojang.blaze3d.systems.RenderSystem.enableScissor(
            (x * scale).toInt(),
            (mc.window.scaledHeight - y1 * scale).toInt(),
            ((x1 - x) * scale).toInt(),
            ((y1 - y) * scale).toInt()
        )
    }

    @JvmStatic
    fun drawLine(x: Float, y: Float, x1: Float, y1: Float, thickness: Float, hex: Int) {
        RenderSystemState.begin2D()
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(thickness)
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.LINES, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        addLineVertices(b, x, y, x1, y1, hex)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end2D()
    }

    private fun addLineVertices(b: net.minecraft.client.render.BufferBuilder, x: Float, y: Float, x1: Float, y1: Float, hex: Int) {
        val r = (hex shr 16 and 0xFF) / 255.0f
        val g = (hex shr 8 and 0xFF) / 255.0f
        val bl = (hex and 0xFF) / 255.0f
        val a = (hex ushr 24 and 0xFF) / 255.0f
        b.vertex(x.toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, a)
        b.vertex(x1.toDouble().toFloat(), y1.toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, a)
    }

    @JvmStatic
    fun drawBox(pos: BlockPos, color: Color) {
        val cam = mc.gameRenderer.camera.pos
        val bb = Box(
            pos.x - cam.x, pos.y - cam.y, pos.z - cam.z,
            (pos.x + 1) - cam.x, (pos.y + 1) - cam.y, (pos.z + 1) - cam.z
        )
        val camE = mc.cameraEntity!!
        camera.setPosition(camE.x, camE.y, camE.z)
        if (camera.isVisible(Box(bb.minX + cam.x, bb.minY + cam.y, bb.minZ + cam.z, bb.maxX + cam.x, bb.maxY + cam.y, bb.maxZ + cam.z))) {
            RenderSystemState.begin3D()
            val t = net.minecraft.client.render.Tessellator.getInstance()
            val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
            addBoxQuads(b, bb, color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
            net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
            RenderSystemState.end3D()
        }
    }

    private fun addBoxQuads(b: net.minecraft.client.render.BufferBuilder, bb: Box, r: Float, g: Float, bl: Float, a: Float) {
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
    }

    @JvmStatic
    fun drawBlockOutline(pos: BlockPos, color: Color, linewidth: Float, air: Boolean) {
        val iblockstate = mc.world!!.getBlockState(pos)
        if ((air || !iblockstate.isAir) && mc.world!!.worldBorder.contains(pos)) {
            val interp = EntityUtil.interpolateEntity(mc.cameraEntity!!, mc.renderTickCounter.getTickDelta(true))
            drawBlockOutline(
                iblockstate.getOutlineShape(mc.world!!, pos).boundingBox.offset(-interp.x, -interp.y, -interp.z).expand(0.002),
                color, linewidth
            )
        }
    }

    @JvmStatic
    fun drawBlockOutline(bb: Box, color: Color, linewidth: Float) {
        RenderSystemState.begin3D()
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(linewidth)
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        addBoxLines(b, bb, color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end3D()
    }

    private fun addBoxLines(b: net.minecraft.client.render.BufferBuilder, bb: Box, r: Float, g: Float, bl: Float, a: Float) {
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
        b.vertex(bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()).color(r, g, bl, a)
    }

    @JvmStatic
    fun drawBoxESP(pos: BlockPos, color: Color, lineWidth: Float, outline: Boolean, box: Boolean, boxAlpha: Int) {
        val cam = mc.gameRenderer.camera.pos
        val bb = Box(
            pos.x - cam.x, pos.y - cam.y, pos.z - cam.z,
            (pos.x + 1) - cam.x, (pos.y + 1) - cam.y, (pos.z + 1) - cam.z
        )
        val camE = mc.cameraEntity!!
        camera.setPosition(camE.x, camE.y, camE.z)
        if (camera.isVisible(Box(bb.minX + cam.x, bb.minY + cam.y, bb.minZ + cam.z, bb.maxX + cam.x, bb.maxY + cam.y, bb.maxZ + cam.z))) {
            RenderSystemState.begin3D()
            com.mojang.blaze3d.systems.RenderSystem.lineWidth(lineWidth)
            if (box) {
                val t = net.minecraft.client.render.Tessellator.getInstance()
                val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
                addBoxQuads(b, bb, color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, boxAlpha / 255.0f)
                net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
            }
            if (outline) {
                val t = net.minecraft.client.render.Tessellator.getInstance()
                val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
                addBoxLines(b, bb, color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
                net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
            }
            RenderSystemState.end3D()
        }
    }

    @JvmStatic
    fun drawText(pos: BlockPos, text: String, color: Int) {
        val screen = to2D(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) ?: return
        val textManager = Moon.textManager ?: return
        val width = textManager.getStringWidth(text)
        textManager.drawStringWithShadow(text, (screen.x - width / 2.0).toFloat(), screen.y.toFloat(), color)
    }

    @JvmStatic
    fun blockEsp(blockPos: BlockPos, c: Color, length: Double, length2: Double) {
        val cam = mc.gameRenderer.camera.pos
        val x = blockPos.x - cam.x
        val y = blockPos.y - cam.y
        val z = blockPos.z - cam.z
        RenderSystemState.begin3D()
        val bb = Box(x, y, z, x + length2, y + 1.0, z + length)
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        addBoxQuads(b, bb, c.red / 255.0f, c.green / 255.0f, c.blue / 255.0f, 0.25f)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end3D()
    }

    @JvmStatic
    fun drawRect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        RenderSystemState.begin2D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val bl = (color and 0xFF) / 255.0f
        val a = (color ushr 24 and 0xFF) / 255.0f
        b.vertex(x.toDouble().toFloat(), h.toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, a)
        b.vertex(w.toDouble().toFloat(), h.toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, a)
        b.vertex(w.toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, a)
        b.vertex(x.toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(r, g, bl, a)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end2D()
    }

    @JvmStatic
    fun drawColorBox(axisalignedbb: Box, red: Float, green: Float, blue: Float, alpha: Float) {
        RenderSystemState.begin3D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        addBoxQuads(b, axisalignedbb, red, green, blue, alpha)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end3D()
    }

    @JvmStatic
    fun drawSelectionBoundingBox(boundingBox: Box) {
        RenderSystemState.begin3D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        addBoxLines(b, boundingBox, 1f, 1f, 1f, 1f)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end3D()
    }

    @JvmStatic
    fun glrendermethod() {
        RenderSystemState.begin3D()
    }

    @JvmStatic
    fun glStart(n: Float, n2: Float, n3: Float, n4: Float) {
        glrendermethod()
        RenderSystemState.setShaderColor(n, n2, n3, n4)
    }

    @JvmStatic
    fun glEnd() {
        RenderSystemState.resetColor()
        RenderSystemState.end3D()
    }

    @JvmStatic
    fun getBoundingBox(blockPos: BlockPos): Box {
        return mc.world!!.getBlockState(blockPos).getOutlineShape(mc.world!!, blockPos).boundingBox.offset(blockPos)
    }

    @JvmStatic
    fun drawOutlinedBox(axisAlignedBB: Box) {
        RenderSystemState.begin3D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        addBoxLines(b, axisAlignedBB, 1f, 1f, 1f, 1f)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end3D()
    }

    @JvmStatic
    fun drawFilledBoxESPN(pos: BlockPos, color: Color) {
        val cam = mc.gameRenderer.camera.pos
        val bb = Box(
            pos.x - cam.x, pos.y - cam.y, pos.z - cam.z,
            (pos.x + 1) - cam.x, (pos.y + 1) - cam.y, (pos.z + 1) - cam.z
        )
        val rgba = ColorUtil.toRGBA(color)
        drawFilledBox(bb, rgba)
    }

    @JvmStatic
    fun drawFilledBox(bb: Box, color: Int) {
        RenderSystemState.begin3D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        addBoxQuads(
            b, bb,
            (color shr 16 and 0xFF) / 255.0f,
            (color shr 8 and 0xFF) / 255.0f,
            (color and 0xFF) / 255.0f,
            (color ushr 24 and 0xFF) / 255.0f
        )
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end3D()
    }

    @JvmStatic
    fun drawBoundingBox(bb: Box, width: Float, color: Int) {
        RenderSystemState.begin3D()
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(width)
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        addBoxLines(
            b, bb,
            (color shr 16 and 0xFF) / 255.0f,
            (color shr 8 and 0xFF) / 255.0f,
            (color and 0xFF) / 255.0f,
            (color ushr 24 and 0xFF) / 255.0f
        )
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end3D()
    }

    @JvmStatic
    var billboardScale: Float = 1.0f
        private set

    @JvmStatic
    fun glBillboard(x: Float, y: Float, z: Float) {
        billboardScale = 0.02666667f
    }

    @JvmStatic
    fun glBillboardDistanceScaled(x: Float, y: Float, z: Float, player: PlayerEntity, scale: Float) {
        glBillboard(x, y, z)
        val dx = player.x - x
        val dy = player.y - y
        val dz = player.z - z
        val distance = Math.sqrt(dx * dx + dy * dy + dz * dz).toInt()
        var scaleDistance = distance / 2.0f / (2.0f + (2.0f - scale))
        if (scaleDistance < 1.0f) {
            scaleDistance = 1.0f
        }
        billboardScale = scaleDistance
    }

    @JvmStatic
    fun drawColoredBoundingBox(bb: Box, width: Float, red: Float, green: Float, blue: Float, alpha: Float) {
        RenderSystemState.begin3D()
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(width)
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        addBoxLines(b, bb, red, green, blue, alpha)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end3D()
    }

    @JvmStatic
    fun drawSphere(x: Double, y: Double, z: Double, size: Float, slices: Int, stacks: Int) {
        RenderSystemState.begin3D()
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(1.2f)
        val cam = mc.gameRenderer.camera.pos
        val cx = x - cam.x
        val cy = y - cam.y
        val cz = z - cam.z
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        fun point(theta: Double, lon: Double): FloatArray {
            val st = size * Math.sin(theta)
            return floatArrayOf(
                (cx + st * Math.cos(lon)).toFloat(),
                (cy + size * Math.cos(theta)).toFloat(),
                (cz + st * Math.sin(lon)).toFloat()
            )
        }
        for (lat in 1 until stacks) {
            val theta = Math.PI * lat / stacks
            for (j in 0 until slices) {
                val p1 = point(theta, 2 * Math.PI * j / slices)
                val p2 = point(theta, 2 * Math.PI * (j + 1) / slices)
                b.vertex(p1[0], p1[1], p1[2]).color(1f, 1f, 1f, 1f)
                b.vertex(p2[0], p2[1], p2[2]).color(1f, 1f, 1f, 1f)
            }
        }
        for (j in 0 until slices) {
            val lon = 2 * Math.PI * j / slices
            for (lat in 0 until stacks) {
                val p1 = point(Math.PI * lat / stacks, lon)
                val p2 = point(Math.PI * (lat + 1) / stacks, lon)
                b.vertex(p1[0], p1[1], p1[2]).color(1f, 1f, 1f, 1f)
                b.vertex(p2[0], p2[1], p2[2]).color(1f, 1f, 1f, 1f)
            }
        }
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(2.0f)
        RenderSystemState.end3D()
    }

    @JvmStatic
    fun GLPre(lineWidth: Float) {
        RenderSystemState.begin3D()
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(lineWidth)
    }

    @JvmStatic
    fun GlPost() {
        RenderSystemState.end3D()
    }

    @JvmStatic
    fun getBipedRotations(biped: Any): Array<FloatArray> {
        return arrayOf(FloatArray(3), FloatArray(3), FloatArray(3), FloatArray(3), FloatArray(3))
    }

    @JvmStatic
    fun drawArc(cx: Float, cy: Float, r: Float, start_angle: Float, end_angle: Float, num_segments: Int) {
        RenderSystemState.begin2D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.TRIANGLE_FAN, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        val a = (RenderSystemState.shaderAlpha())
        var i = (num_segments / (360.0f / start_angle)).toInt() + 1
        while (i <= num_segments / (360.0f / end_angle)) {
            val previousangle = (Math.PI * 2) * (i - 1) / num_segments
            val angle = (Math.PI * 2) * i / num_segments
            b.vertex(cx.toDouble().toFloat(), cy.toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
            b.vertex((cx + Math.cos(angle) * r).toDouble().toFloat(), (cy + Math.sin(angle) * r).toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
            b.vertex((cx + Math.cos(previousangle) * r).toDouble().toFloat(), (cy + Math.sin(previousangle) * r).toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
            i++
        }
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end2D()
    }

    @JvmStatic
    fun drawArcOutline(cx: Float, cy: Float, r: Float, start_angle: Float, end_angle: Float, num_segments: Int) {
        RenderSystemState.begin2D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINE_STRIP, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        val a = RenderSystemState.shaderAlpha()
        var i = (num_segments / (360.0f / start_angle)).toInt() + 1
        while (i <= num_segments / (360.0f / end_angle)) {
            val angle = (Math.PI * 2) * i / num_segments
            b.vertex((cx + Math.cos(angle) * r).toDouble().toFloat(), (cy + Math.sin(angle) * r).toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
            i++
        }
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end2D()
    }

    @JvmStatic
    fun drawCircleOutline(x: Float, y: Float, radius: Float) {
        drawCircleOutline(x, y, radius, 0, 360, 40)
    }

    @JvmStatic
    fun drawCircleOutline(x: Float, y: Float, radius: Float, start: Int, end: Int, segments: Int) {
        drawArcOutline(x, y, radius, start.toFloat(), end.toFloat(), segments)
    }

    @JvmStatic
    fun drawCircle(x: Float, y: Float, radius: Float) {
        drawCircle(x, y, radius, 0, 360, 64)
    }

    @JvmStatic
    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int, segments: Int) {
        drawArc(x, y, radius, start.toFloat(), end.toFloat(), segments)
    }

    @JvmStatic
    fun drawOutlinedRoundedRectangle(
        x: Int, y: Int, width: Int, height: Int, radius: Float, dR: Float, dG: Float, dB: Float, dA: Float, outlineWidth: Float
    ) {
        drawRoundedRectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius)
        RenderSystemState.setShaderColor(dR, dG, dB, dA)
        drawRoundedRectangle((x + outlineWidth).toFloat(), (y + outlineWidth).toFloat(), (width - outlineWidth * 2.0f), (height - outlineWidth * 2.0f), radius)
        RenderSystemState.resetColor()
    }

    @JvmStatic
    fun drawRectangle(x: Float, y: Float, width: Float, height: Float) {
        RenderSystemState.begin2D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        val a = RenderSystemState.shaderAlpha()
        b.vertex(width.toDouble().toFloat(), 0.0.toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        b.vertex(0.0.toFloat(), 0.0.toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        b.vertex(0.0.toFloat(), height.toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        b.vertex(width.toDouble().toFloat(), height.toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end2D()
    }

    @JvmStatic
    fun drawRectangleXY(x: Float, y: Float, width: Float, height: Float) {
        RenderSystemState.begin2D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        val a = RenderSystemState.shaderAlpha()
        b.vertex((x + width).toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        b.vertex(x.toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        b.vertex(x.toDouble().toFloat(), (y + height).toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        b.vertex((x + width).toDouble().toFloat(), (y + height).toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end2D()
    }

    @JvmStatic
    fun drawFilledRectangle(x: Float, y: Float, width: Float, height: Float) {
        RenderSystemState.begin2D()
        val t = net.minecraft.client.render.Tessellator.getInstance()
        val b = t.begin(net.minecraft.client.render.VertexFormat.DrawMode.TRIANGLE_FAN, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
        val a = RenderSystemState.shaderAlpha()
        b.vertex((x + width).toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        b.vertex(x.toDouble().toFloat(), y.toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        b.vertex(x.toDouble().toFloat(), (y + height).toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        b.vertex((x + width).toDouble().toFloat(), (y + height).toDouble().toFloat(), 0.0.toFloat()).color(1f, 1f, 1f, a)
        net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
        RenderSystemState.end2D()
    }

    @JvmStatic
    fun drawRoundedRectangle(x: Float, y: Float, width: Float, height: Float, radius: Float) {
        drawArc(x + width - radius, y + height - radius, radius, 0.0f, 90.0f, 16)
        drawArc(x + radius, y + height - radius, radius, 90.0f, 180.0f, 16)
        drawArc(x + radius, y + radius, radius, 180.0f, 270.0f, 16)
        drawArc(x + width - radius, y + radius, radius, 270.0f, 360.0f, 16)
    }

    @JvmStatic
    fun renderOne(lineWidth: Float) {
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR)
        com.mojang.blaze3d.systems.RenderSystem.enableBlend()
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc()
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest()
        com.mojang.blaze3d.systems.RenderSystem.depthMask(false)
        com.mojang.blaze3d.systems.RenderSystem.disableCull()
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(lineWidth)
        com.mojang.blaze3d.systems.RenderSystem.polygonOffset(1.0f, -2000000.0f)
    }

    @JvmStatic
    fun renderTwo() {
        com.mojang.blaze3d.systems.RenderSystem.polygonOffset(1.0f, 2000000.0f)
    }

    @JvmStatic
    fun renderThree() {
        com.mojang.blaze3d.systems.RenderSystem.polygonOffset(1.0f, -2000000.0f)
    }

    @JvmStatic
    fun renderFour(color: Color) {
        setColor(color)
        com.mojang.blaze3d.systems.RenderSystem.depthMask(false)
        com.mojang.blaze3d.systems.RenderSystem.polygonOffset(1.0f, -2000000.0f)
    }

    @JvmStatic
    fun renderFive() {
        com.mojang.blaze3d.systems.RenderSystem.polygonOffset(0.0f, 0.0f)
        com.mojang.blaze3d.systems.RenderSystem.depthMask(true)
        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest()
        com.mojang.blaze3d.systems.RenderSystem.enableCull()
        com.mojang.blaze3d.systems.RenderSystem.disableBlend()
        com.mojang.blaze3d.systems.RenderSystem.lineWidth(2.0f)
        RenderSystemState.resetColor()
    }

    @JvmStatic
    fun setColor(color: Color) {
        RenderSystemState.setShaderColor(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
    }

    @JvmStatic
    fun checkSetupFBO() {
        mc.framebuffer.beginWrite(false)
    }

    class RenderTesselator {
        companion object {
            @JvmField
            val INSTANCE = RenderTesselator()

            private var buffer: net.minecraft.client.render.BufferBuilder? = null

            @JvmStatic
            fun prepare(mode: Int) {
                prepareGL()
                begin(mode)
            }

            @JvmStatic
            fun prepareGL() {
                com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR)
                com.mojang.blaze3d.systems.RenderSystem.enableBlend()
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc()
                com.mojang.blaze3d.systems.RenderSystem.lineWidth(1.5f)
                com.mojang.blaze3d.systems.RenderSystem.depthMask(false)
                com.mojang.blaze3d.systems.RenderSystem.disableDepthTest()
                com.mojang.blaze3d.systems.RenderSystem.disableCull()
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
            }

            @JvmStatic
            fun begin(mode: Int) {
                val drawMode = if (mode == 7)
                    net.minecraft.client.render.VertexFormat.DrawMode.QUADS
                else
                    net.minecraft.client.render.VertexFormat.DrawMode.DEBUG_LINES
                buffer = net.minecraft.client.render.Tessellator.getInstance()
                    .begin(drawMode, net.minecraft.client.render.VertexFormats.POSITION_COLOR)
            }

            @JvmStatic
            fun release() {
                render()
                releaseGL()
            }

            @JvmStatic
            fun render() {
                val b = buffer ?: return
                net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(b.end())
                buffer = null
            }

            @JvmStatic
            fun releaseGL() {
                com.mojang.blaze3d.systems.RenderSystem.depthMask(true)
                com.mojang.blaze3d.systems.RenderSystem.enableDepthTest()
                com.mojang.blaze3d.systems.RenderSystem.enableCull()
                com.mojang.blaze3d.systems.RenderSystem.enableBlend()
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
            }

            @JvmStatic
            fun drawBox(blockPos: BlockPos, argb: Int, sides: Int) {
                val a = argb ushr 24 and 0xFF
                val r = argb shr 16 and 0xFF
                val g = argb shr 8 and 0xFF
                val b = argb and 0xFF
                drawBox(blockPos, r, g, b, a, sides)
            }

            @JvmStatic
            fun drawBox(x: Float, y: Float, z: Float, argb: Int, sides: Int) {
                val a = argb ushr 24 and 0xFF
                val r = argb shr 16 and 0xFF
                val g = argb shr 8 and 0xFF
                val b = argb and 0xFF
                drawBox(x, y, z, 1.0f, 1.0f, 1.0f, r, g, b, a, sides)
            }

            @JvmStatic
            fun drawBox(blockPos: BlockPos, r: Int, g: Int, b: Int, a: Int, sides: Int) {
                drawBox(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat(), 1.0f, 1.0f, 1.0f, r, g, b, a, sides)
            }

            @JvmStatic
            fun getBufferBuilder(): net.minecraft.client.render.BufferBuilder? {
                return buffer
            }

            @JvmStatic
            fun drawBox(x: Float, y: Float, z: Float, w: Float, h: Float, d: Float, r: Int, g: Int, b: Int, a: Int, sides: Int) {
                val bb = buffer ?: return
                fun v(px: Float, py: Float, pz: Float) {
                    bb.vertex(px, py, pz).color(r, g, b, a)
                }
                if ((sides and 1) != 0) {
                    v(x + w, y, z); v(x + w, y, z + d); v(x, y, z + d); v(x, y, z)
                }
                if ((sides and 2) != 0) {
                    v(x + w, y + h, z); v(x, y + h, z); v(x, y + h, z + d); v(x + w, y + h, z + d)
                }
                if ((sides and 4) != 0) {
                    v(x + w, y, z); v(x, y, z); v(x, y + h, z); v(x + w, y + h, z)
                }
                if ((sides and 8) != 0) {
                    v(x, y, z + d); v(x + w, y, z + d); v(x + w, y + h, z + d); v(x, y + h, z + d)
                }
                if ((sides and 16) != 0) {
                    v(x, y, z); v(x, y, z + d); v(x, y + h, z + d); v(x, y + h, z)
                }
                if ((sides and 32) != 0) {
                    v(x + w, y, z + d); v(x + w, y, z); v(x + w, y + h, z); v(x + w, y + h, z + d)
                }
            }

            @JvmStatic
            fun drawLines(x: Float, y: Float, z: Float, w: Float, h: Float, d: Float, r: Int, g: Int, b: Int, a: Int, sides: Int) {
                val bb = buffer ?: return
                fun v(px: Float, py: Float, pz: Float) {
                    bb.vertex(px, py, pz).color(r, g, b, a)
                }
                if ((sides and 17) != 0) { v(x, y, z); v(x, y, z + d) }
                if ((sides and 18) != 0) { v(x, y + h, z); v(x, y + h, z + d) }
                if ((sides and 33) != 0) { v(x + w, y, z); v(x + w, y, z + d) }
                if ((sides and 34) != 0) { v(x + w, y + h, z); v(x + w, y + h, z + d) }
                if ((sides and 5) != 0) { v(x, y, z); v(x + w, y, z) }
                if ((sides and 6) != 0) { v(x, y + h, z); v(x + w, y + h, z) }
                if ((sides and 9) != 0) { v(x, y, z + d); v(x + w, y, z + d) }
                if ((sides and 10) != 0) { v(x, y + h, z + d); v(x + w, y + h, z + d) }
                if ((sides and 20) != 0) { v(x, y, z); v(x, y + h, z) }
                if ((sides and 36) != 0) { v(x + w, y, z); v(x + w, y + h, z) }
                if ((sides and 24) != 0) { v(x, y, z + d); v(x, y + h, z + d) }
                if ((sides and 40) != 0) { v(x + w, y, z + d); v(x + w, y + h, z + d) }
            }

            @JvmStatic
            fun drawBoundingBox(bb: Box, width: Float, red: Float, green: Float, blue: Float, alpha: Float) {
                val packed = (alpha.toInt() and 0xFF shl 24) or (red.toInt() and 0xFF shl 16) or (green.toInt() and 0xFF shl 8) or (blue.toInt() and 0xFF)
                RenderUtil.drawBoundingBox(bb, width, packed)
            }

            @JvmStatic
            fun drawFullBox(bb: Box, blockPos: BlockPos, width: Float, argb: Int, alpha2: Int) {
                val a = argb ushr 24 and 0xFF
                val r = argb shr 16 and 0xFF
                val g = argb shr 8 and 0xFF
                val b = argb and 0xFF
                drawFullBox(bb, blockPos, width, r, g, b, a, alpha2)
            }

            @JvmStatic
            fun drawFullBox(bb: Box, blockPos: BlockPos, width: Float, red: Int, green: Int, blue: Int, alpha: Int, alpha2: Int) {
                prepare(7)
                drawBox(blockPos, red, green, blue, alpha, 63)
                release()
                drawBoundingBox(bb, width, red.toFloat(), green.toFloat(), blue.toFloat(), alpha2.toFloat())
            }

            @JvmStatic
            fun drawHalfBox(blockPos: BlockPos, argb: Int, sides: Int) {
                val a = argb ushr 24 and 0xFF
                val r = argb shr 16 and 0xFF
                val g = argb shr 8 and 0xFF
                val b = argb and 0xFF
                drawHalfBox(blockPos, r, g, b, a, sides)
            }

            @JvmStatic
            fun drawHalfBox(blockPos: BlockPos, r: Int, g: Int, b: Int, a: Int, sides: Int) {
                drawBox(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat(), 1.0f, 0.5f, 1.0f, r, g, b, a, sides)
            }
        }
    }
}

interface ScaledResolutionProxy {
    val scaleFactor: Double
}

internal object RenderUtilAccess {
    val projectionMatrix: Matrix4f
        get() = com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix()
    val modelViewMatrix: Matrix4f
        get() = com.mojang.blaze3d.systems.RenderSystem.getModelViewMatrix()
}

internal object RenderSystemState {
    fun begin2D() {
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR)
        com.mojang.blaze3d.systems.RenderSystem.enableBlend()
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc()
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    fun end2D() {
        com.mojang.blaze3d.systems.RenderSystem.disableBlend()
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    fun begin3D() {
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR)
        com.mojang.blaze3d.systems.RenderSystem.enableBlend()
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc()
        com.mojang.blaze3d.systems.RenderSystem.disableCull()
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest()
        com.mojang.blaze3d.systems.RenderSystem.depthMask(false)
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    fun end3D() {
        com.mojang.blaze3d.systems.RenderSystem.depthMask(true)
        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest()
        com.mojang.blaze3d.systems.RenderSystem.enableCull()
        com.mojang.blaze3d.systems.RenderSystem.disableBlend()
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    fun setShaderColor(r: Float, g: Float, b: Float, a: Float) {
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(r, g, b, a)
    }

    fun setShaderColor(color: Int) {
        setShaderColor(
            (color shr 16 and 0xFF) / 255.0f,
            (color shr 8 and 0xFF) / 255.0f,
            (color and 0xFF) / 255.0f,
            (color ushr 24 and 0xFF) / 255.0f
        )
    }

    fun resetColor() {
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    fun enableBlend() {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend()
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc()
    }

    fun shaderAlpha(): Float {
        return 1f
    }

    fun setTextureShader() {
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_TEX)
    }
}
