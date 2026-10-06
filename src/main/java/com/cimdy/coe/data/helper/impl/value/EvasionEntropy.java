package com.cimdy.coe.data.helper.impl.value;

import com.cimdy.coe.registry.world.AttachmentRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public record EvasionEntropy(Optional<UUID> uuid, int entropy, int duration) {
    public static final EvasionEntropy EMPTY = new EvasionEntropy(Optional.empty(), 0, 0);

    public static final Codec<EvasionEntropy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(EvasionEntropy::uuid),
            Codec.INT.fieldOf("entropy").forGetter(EvasionEntropy::entropy),
            Codec.INT.fieldOf("duration").forGetter(EvasionEntropy::duration)
    ).apply(instance, EvasionEntropy::new));

    public static void apply(LivingEntity living, int entropy) {
        living.setData(AttachmentRegistry.EVASION_ENTROPY,
                new EvasionEntropy(Optional.of(living.getUUID()), entropy, 160));
    }

    public static boolean isSameTickHit(LivingEntity living) {
        Optional<UUID> optionalUUID = living.getData(AttachmentRegistry.EVASION_ENTROPY).uuid();
        return optionalUUID.isPresent() && living.getUUID().equals(optionalUUID.get());
    }

    public static int getEntropy(LivingEntity living) {
        return living.getData(AttachmentRegistry.EVASION_ENTROPY).entropy();
    }

    public static int getDuration(LivingEntity living) {
        return living.getData(AttachmentRegistry.EVASION_ENTROPY).duration();
    }

    public static void updata(LivingEntity living) {
        int duration = getDuration(living) - 1;
        if (duration <= 0) {
            living.removeData(AttachmentRegistry.EVASION_ENTROPY);
        } else {
            living.setData(AttachmentRegistry.EVASION_ENTROPY,
                    new EvasionEntropy(Optional.empty(), getEntropy(living), duration));
        }
    }

    public static void initialization(LivingEntity living, RandomSource random) {
        living.setData(AttachmentRegistry.EVASION_ENTROPY,
                new EvasionEntropy(Optional.of(living.getUUID()), random.nextInt(98) + 1, 160));
    }
}
