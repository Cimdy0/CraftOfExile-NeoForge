package com.cimdy.coe.data.codec.entity.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

import java.util.Optional;

/**
 * 内置道具渲染层实现 —— 覆盖既有全部硬编码表现（残片 GLOW 环、危害物烟雾、战旗旗杆、
 * 奉献尖刺）并额外提供真实模型层。
 *
 * <p>各层半径/高度均为<b>实体半径的相对倍率</b>（{@code 1.0 = 实体触发半径}），
 * 使同一份定义在任意半径下外观一致、并随「范围扩大」类词缀自动缩放。
 */
public final class EntityRenderLayers {

    private EntityRenderLayers() {
    }

    /**
     * 水平粒子环 —— 贴地一圈采样点，用于表达「拾取 / 触发范围」。
     * 残片原本硬编码的 GLOW 环即本层（samples=12、y_offset=0.15）。
     */
    public record Ring(ParticleOptions particle, int samples, double radiusScale, double yOffset,
                       int interval, double speed) implements EntityRenderLayer {
        public static final Codec<Ring> CODEC = RecordCodecBuilder.create(i -> i.group(
                ParticleTypes.CODEC.fieldOf("particle").forGetter(Ring::particle),
                Codec.INT.optionalFieldOf("samples", 12).forGetter(Ring::samples),
                Codec.DOUBLE.optionalFieldOf("radius_scale", 1.0).forGetter(Ring::radiusScale),
                Codec.DOUBLE.optionalFieldOf("y_offset", 0.15).forGetter(Ring::yOffset),
                Codec.INT.optionalFieldOf("interval", 6).forGetter(Ring::interval),
                Codec.DOUBLE.optionalFieldOf("speed", 0.02).forGetter(Ring::speed)
        ).apply(i, Ring::new));

        @Override
        public EntityRenderLayer.Type<?> getType() {
            return EntityRenderTypes.RING;
        }

        @Override
        public int interval() {
            return interval;
        }

        @Override
        public void emit(EmitContext context, double radius) {
            double r = radius * radiusScale;
            for (int i = 0; i < samples; i++) {
                double angle = 2 * Math.PI * i / samples;
                double px = context.x() + Math.cos(angle) * r;
                double pz = context.z() + Math.sin(angle) * r;
                context.level().sendParticles(particle, px, context.y() + yOffset, pz,
                        1, 0.0, speed, 0.0, 0.0);
            }
        }
    }

    /**
     * 竖直粒子柱 —— 从底面向上升起，用于表达「驻场存在感」（战旗旗杆、图腾、奉献尖刺）。
     */
    public record Pillar(ParticleOptions particle, double heightScale, int density,
                         double radiusScale, int interval, double speed) implements EntityRenderLayer {
        public static final Codec<Pillar> CODEC = RecordCodecBuilder.create(i -> i.group(
                ParticleTypes.CODEC.fieldOf("particle").forGetter(Pillar::particle),
                Codec.DOUBLE.optionalFieldOf("height_scale", 1.5).forGetter(Pillar::heightScale),
                Codec.INT.optionalFieldOf("density", 6).forGetter(Pillar::density),
                Codec.DOUBLE.optionalFieldOf("radius_scale", 1.0).forGetter(Pillar::radiusScale),
                Codec.INT.optionalFieldOf("interval", 6).forGetter(Pillar::interval),
                Codec.DOUBLE.optionalFieldOf("speed", 0.02).forGetter(Pillar::speed)
        ).apply(i, Pillar::new));

        @Override
        public EntityRenderLayer.Type<?> getType() {
            return EntityRenderTypes.PILLAR;
        }

        @Override
        public int interval() {
            return interval;
        }

        @Override
        public void emit(EmitContext context, double radius) {
            double h = radius * heightScale;
            double r = radius * radiusScale;
            RandomSource random = context.random();
            for (int i = 0; i < density; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double dist = random.nextDouble() * r;
                double px = context.x() + Math.cos(angle) * dist;
                double pz = context.z() + Math.sin(angle) * dist;
                double py = context.y() + random.nextDouble() * h;
                context.level().sendParticles(particle, px, py, pz, 1, 0.0, speed, 0.0, 0.0);
            }
        }
    }

    /**
     * 粒子球壳 —— 包裹触发体积，用于表达「危害区域 / 光环边界」。
     */
    public record Sphere(ParticleOptions particle, int samples, double radiusScale, double yOffset,
                         int interval, double speed) implements EntityRenderLayer {
        public static final Codec<Sphere> CODEC = RecordCodecBuilder.create(i -> i.group(
                ParticleTypes.CODEC.fieldOf("particle").forGetter(Sphere::particle),
                Codec.INT.optionalFieldOf("samples", 24).forGetter(Sphere::samples),
                Codec.DOUBLE.optionalFieldOf("radius_scale", 1.0).forGetter(Sphere::radiusScale),
                Codec.DOUBLE.optionalFieldOf("y_offset", 0.5).forGetter(Sphere::yOffset),
                Codec.INT.optionalFieldOf("interval", 6).forGetter(Sphere::interval),
                Codec.DOUBLE.optionalFieldOf("speed", 0.0).forGetter(Sphere::speed)
        ).apply(i, Sphere::new));

        @Override
        public EntityRenderLayer.Type<?> getType() {
            return EntityRenderTypes.SPHERE;
        }

        @Override
        public int interval() {
            return interval;
        }

        @Override
        public void emit(EmitContext context, double radius) {
            double r = radius * radiusScale;
            // 斐波那契球面采样：比经纬网格更均匀，少量点即可成壳
            double golden = Math.PI * (3.0 - Math.sqrt(5.0));
            for (int i = 0; i < samples; i++) {
                double y = 1.0 - (i / (double) (samples - 1)) * 2.0;
                double rr = Math.sqrt(Math.max(0.0, 1.0 - y * y));
                double theta = golden * i;
                double px = context.x() + Math.cos(theta) * rr * r;
                double pz = context.z() + Math.sin(theta) * rr * r;
                double py = context.y() + yOffset + y * r;
                context.level().sendParticles(particle, px, py, pz, 1, 0.0, speed, 0.0, 0.0);
            }
        }
    }

    /**
     * 单点粒子簇 —— 在实体中心小范围散开，用于表达「火花 / 余烬 / 飘散物」。
     */
    public record Point(ParticleOptions particle, int count, double spread, double yOffset,
                        int interval, double speed) implements EntityRenderLayer {
        public static final Codec<Point> CODEC = RecordCodecBuilder.create(i -> i.group(
                ParticleTypes.CODEC.fieldOf("particle").forGetter(Point::particle),
                Codec.INT.optionalFieldOf("count", 2).forGetter(Point::count),
                Codec.DOUBLE.optionalFieldOf("spread", 0.2).forGetter(Point::spread),
                Codec.DOUBLE.optionalFieldOf("y_offset", 0.3).forGetter(Point::yOffset),
                Codec.INT.optionalFieldOf("interval", 4).forGetter(Point::interval),
                Codec.DOUBLE.optionalFieldOf("speed", 0.01).forGetter(Point::speed)
        ).apply(i, Point::new));

        @Override
        public EntityRenderLayer.Type<?> getType() {
            return EntityRenderTypes.POINT;
        }

        @Override
        public int interval() {
            return interval;
        }

        @Override
        public void emit(EmitContext context, double radius) {
            double s = Math.max(spread, radius * spread);
            context.level().sendParticles(particle, context.x(), context.y() + yOffset, context.z(),
                    count, s, s * 0.5, s, speed);
        }
    }

    /**
     * 模型层 —— 客户端绘制贴图立牌（{@link ModelDef}）。
     * <b>服务端为空实现</b>：模型不走 {@code sendParticles}，由客户端渲染器按 {@link #model()} 取用。
     */
    public record Model(ModelDef def) implements EntityRenderLayer {
        // 组件名取 def 而非 model：接口的 model() 返回 Optional<ModelDef>（供客户端统一取用），
        // 若组件同名会让 record 的存取方法与其返回类型冲突（编译期「存取方法无效」）。
        // 派发键已是 "model"，故内容体直接就是 ModelDef 自身字段（不再多嵌一层 "model"）。
        public static final Codec<Model> CODEC = ModelDef.CODEC.xmap(Model::new, Model::def);

        @Override
        public EntityRenderLayer.Type<?> getType() {
            return EntityRenderTypes.MODEL;
        }

        @Override
        public int interval() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void emit(EmitContext context, double radius) {
            // 模型层无服务端发射：表现由客户端渲染器承担
        }

        @Override
        public Optional<ModelDef> model() {
            return Optional.of(def);
        }
    }
}
