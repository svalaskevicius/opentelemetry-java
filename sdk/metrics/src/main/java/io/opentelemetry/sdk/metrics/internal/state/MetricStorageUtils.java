/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import java.util.Map;
import java.util.function.BiFunction;

/** Utilities to help deal w/ {@code Map<Attributes, Accumulation>} in metric storage. */
final class MetricStorageUtils {
  /** The max number of metric accumulations for a particular {@link MetricStorage}. */
  static final int MAX_ACCUMULATIONS = 2000;

  private MetricStorageUtils() {}

  /**
   * Merges accumulations from {@code toMerge} into {@code result}. Keys from {@code result} which
   * don't appear in {@code toMerge} are removed.
   *
   * <p>Note: This mutates the result map.
   */
  static <T> void mergeInPlace(
      Map<Attributes, T> result, Map<Attributes, T> toMerge, Aggregator<T> aggregator) {
    blend(result, toMerge, aggregator::merge);
  }

  /**
   * Diffs accumulations from {@code toMerge} into {@code result}. Keys from {@code result} which
   * don't appear in {@code toMerge} are removed.
   *
   * <p>If no prior value is found, then the value from {@code toDiff} is used.
   *
   * <p>Note: This mutates the result map.
   */
  static <T> void diffInPlace(
      Map<Attributes, T> result, Map<Attributes, T> toDiff, Aggregator<T> aggregator) {
    blend(result, toDiff, aggregator::diff);
  }

  private static <T> void blend(
      Map<Attributes, T> result, Map<Attributes, T> toMerge, BiFunction<T, T, T> blendFunction) {
    result.entrySet().removeIf(entry -> !toMerge.containsKey(entry.getKey()));
    toMerge.forEach(
        (k, v) -> result.compute(k, (k2, v2) -> (v2 != null) ? blendFunction.apply(v2, v) : v));
  }
}
