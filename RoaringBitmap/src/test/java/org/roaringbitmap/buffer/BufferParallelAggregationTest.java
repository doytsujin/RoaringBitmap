package org.roaringbitmap.buffer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.roaringbitmap.SeededTestData.TestDataSet.testCase;

@Execution(ExecutionMode.CONCURRENT)
public class BufferParallelAggregationTest {
  private static ForkJoinPool POOL;

  private static ForkJoinPool NO_PARALLELISM_AVAILABLE;

  @BeforeAll
  public static void init() {
    POOL = new ForkJoinPool(4);
    NO_PARALLELISM_AVAILABLE = new ForkJoinPool(1);
  }

  @AfterAll
  public static void teardown() {
    POOL.shutdownNow();
    NO_PARALLELISM_AVAILABLE.shutdownNow();
  }

  @Test
  public void singleContainerOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(0).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(0).build().toMutableRoaringBitmap();
    assertEquals(BufferFastAggregation.or(one, two, three), BufferParallelAggregation.or(one, two, three));
  }

  @Test
  public void twoContainerOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0).withArrayAt(1).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(1).build().toMutableRoaringBitmap();
    assertEquals(BufferFastAggregation.or(one, two, three), BufferParallelAggregation.or(one, two, three));
  }

  @Test
  public void disjointOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0).withArrayAt(2).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(3).build().toMutableRoaringBitmap();
    assertEquals(BufferFastAggregation.or(one, two, three), BufferParallelAggregation.or(one, two, three));
  }

  @Test
  public void disjointBigKeysOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0).withArrayAt(2).withBitmapAt((1 << 15) | 1).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1).withRunAt((1 << 15) | 2).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(3).withRunAt((1 << 15) | 3).build()
            .toMutableRoaringBitmap();
    assertEquals(BufferFastAggregation.or(one, two, three), BufferParallelAggregation.or(one, two, three));
  }

  @Test
  public void wideOr() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 20)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input), BufferParallelAggregation.or(input));
  }

  @Test
  public void hugeOr1() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 513)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input), BufferParallelAggregation.or(input));
  }


  @Test
  public void hugeOr2() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 1999)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input), BufferParallelAggregation.or(input));
  }

  @Test
  public void hugeOr3() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 4096)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input), BufferParallelAggregation.or(input));
  }

  @Test
  public void hugeOrNoParallelismAvailable1() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 513)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input),
            NO_PARALLELISM_AVAILABLE.submit(() -> BufferParallelAggregation.or(input)).join());
  }


  @Test
  public void hugeOrNoParallelismAvailable2() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 2000)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input),
            NO_PARALLELISM_AVAILABLE.submit(() -> BufferParallelAggregation.or(input)).join());
  }

  @Test
  public void hugeOrNoParallelismAvailable3() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 4096)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input),
            NO_PARALLELISM_AVAILABLE.submit(() -> BufferParallelAggregation.or(input)).join());
  }


  @Test
  public void hugeOrInFJP1() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 513)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input),
            POOL.submit(() -> BufferParallelAggregation.or(input)).join());
  }


  @Test
  public void hugeOrInFJP2() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 2000)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input),
            POOL.submit(() -> BufferParallelAggregation.or(input)).join());
  }

  @Test
  public void hugeOrInFJP3() {
    ImmutableRoaringBitmap[] input = IntStream.range(0, 4096)
            .mapToObj(i -> testCase().withBitmapAt(0).withArrayAt(1).withRunAt(2).build().toMutableRoaringBitmap())
            .toArray(ImmutableRoaringBitmap[]::new);
    assertEquals(BufferFastAggregation.or(input),
            POOL.submit(() -> BufferParallelAggregation.or(input)).join());
  }

  @Test
  public void singleContainerXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(0).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(0).build().toMutableRoaringBitmap();
    assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }


  @Test
  public void missingMiddleContainerXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0).withBitmapAt(1).withArrayAt(2).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(0).withArrayAt(2).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(0).withBitmapAt(1).withArrayAt(2).build()
            .toMutableRoaringBitmap();
    assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }

  @Test
  public void twoContainerXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0).withArrayAt(1).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(1).build().toMutableRoaringBitmap();
    assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }

  @Test
  public void disjointXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0).withArrayAt(2).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1).build().toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(3).build().toMutableRoaringBitmap();
    assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }

  @Test
  public void disjointBigKeysXOR() {
    ImmutableRoaringBitmap one = testCase().withRunAt(0).withArrayAt(2).withBitmapAt((1 << 15) | 1).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap two = testCase().withBitmapAt(1).withRunAt((1 << 15) | 2).build()
            .toMutableRoaringBitmap();
    ImmutableRoaringBitmap three = testCase().withArrayAt(3).withRunAt((1 << 15) | 3).build()
            .toMutableRoaringBitmap();
    assertEquals(BufferFastAggregation.xor(one, two, three), BufferParallelAggregation.xor(one, two, three));
  }
}



