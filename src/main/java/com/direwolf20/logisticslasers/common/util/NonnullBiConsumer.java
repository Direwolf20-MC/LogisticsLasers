package com.direwolf20.logisticslasers.common.util;

import javax.annotation.Nonnull;

/**
 * Equivalent to {@link BiConsumer}, except with nonnull contract.
 *
 * @see BiConsumer
 */
@FunctionalInterface
public interface NonnullBiConsumer<T, U> {
    void accept(@Nonnull T t, @Nonnull U u);
}