package org.jpvm;

import org.junit.Test;

public class SyntaxTest {

  @Test
  public void testLoop() {
    CONTINUELIABLE:
    for (int i = 0; i < 3; i++) {
      System.out.printf("start outer for loop index %d%n", i);
      for (int k = 0; k < 3; k++) {
        if (k == 1) break CONTINUELIABLE;
        System.out.printf("inner loop index %d%n", k);
      }
      System.out.printf("end outer for loop index %d%n", i);
    }
  }

  @Test
  public void testVarArgs() {
    numbers(1, 2, 3, 4, 5);
  }

  public void numbers(int... numbers) {
    printNumbers(numbers);
  }

  public void printNumbers(int... numbers) {
    for (int number : numbers) {
      System.out.println(number);
    }
  }
}
