/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.lib.math;

import com.malhartech.annotation.InputPortFieldAnnotation;
import com.malhartech.annotation.OutputPortFieldAnnotation;
import com.malhartech.api.BaseOperator;
import com.malhartech.api.DefaultInputPort;
import com.malhartech.api.DefaultOutputPort;
import com.malhartech.lib.util.MutableDouble;

/**
 *
 * Takes in one stream via input port "data". At end of window sends maximum of all incoming tuples on port "max"<p>
 * <br>
 * <b>Ports</b>:
 * <b>data</b> expects V extends Number<br>
 * <b>high</b> emits V<br>
 * <br>
 * <b>Compile time checks</b>:
 * None<br>
 * <b>Run time checks</b>:
 * None<br>
 * <br>
 * <b>Benchmarks</b>: Blast as many tuples as possible in inline mode<br>
 * The operator does >500 million tuples/sec as it only emits one per end of window, and is not bounded by outbound I/O. It uses a Mutable number and thus avoids memory allocation<br>
 *<br>
 * @author amol
 */
public class MaxValue<V extends Number> extends BaseNumberOperator<V>
{
  @InputPortFieldAnnotation(name = "data")
  public final transient DefaultInputPort<V> data = new DefaultInputPort<V>(this)
  {
    @Override
    public void process(V tuple)
    {
      if (!flag) {
        high = tuple.doubleValue();
        flag = true;
      }
      else if (high < tuple.doubleValue()) {
        high = tuple.doubleValue();
      }
    }
  };

  @OutputPortFieldAnnotation(name = "max")
  public final transient DefaultOutputPort<V> max = new DefaultOutputPort<V>(this);
  double high;
  boolean flag = false;

  @Override
  public void beginWindow()
  {
    flag = false;
  }

  /**
   * Node only works in windowed mode. Emits all data upon end of window tuple
   */
  @Override
  public void endWindow()
  {
    if (flag) {
      max.emit(getValue(high));
    }
  }
}
