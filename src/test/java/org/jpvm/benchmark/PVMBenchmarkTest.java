package org.jpvm.benchmark;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.jpvm.errors.PyException;
import org.jpvm.pvm.PVM;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 20)
@Fork(1)
public class PVMBenchmarkTest {

  @Benchmark
  public void testFib() throws PyException, IOException {
    String filename = "src/test/resources/benchmark/__pycache__/fib.cpython-38.pyc";
    PVM pvm = new PVM(filename);
    pvm.run();
  }

  @Test
  public void testMatrixMultiply() throws PyException, IOException {
    String filename = "src/test/resources/benchmark/__pycache__/matrix.cpython-38.pyc";
    PVM pvm = new PVM(filename);
    pvm.run();
  }

  @Test
  public void testMatrixMultiplyConcurrent() throws PyException, IOException {
    String filename = "src/test/resources/benchmark/__pycache__/concurrentMatrix.cpython-38.pyc";
    PVM pvm = new PVM(filename);
    pvm.run();
  }
}
