package org.jpvm.stl.time;

import java.util.concurrent.TimeUnit;
import org.jpvm.errors.PyException;
import org.jpvm.excptions.PyErrorUtils;
import org.jpvm.objects.*;
import org.jpvm.objects.annotation.PyClassMethod;
import org.jpvm.pvm.InterpreterState;
import org.jpvm.pvm.PVM;
import org.jpvm.python.BuiltIn;

public class PyModuleMain extends PyModuleObject {
  public PyModuleMain(PyUnicodeObject moduleName) {
    super(moduleName);
  }

  @PyClassMethod
  public PyObject sleep(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    if (args.size() == 1) {
      PyObject object = args.get(0);
      if (object instanceof PyLongObject o) {
        try {
          InterpreterState is = PVM.getThreadState().getIs();
          is.dropGIL();
          TimeUnit.SECONDS.sleep(o.getData());
          return BuiltIn.None;
        } catch (InterruptedException ignore) {
        } finally {
          InterpreterState is = PVM.getThreadState().getIs();
          is.takeGIL();
        }
      } else if (object instanceof PyFloatObject o) {
        InterpreterState is = PVM.getThreadState().getIs();
        try {
          is.dropGIL();
          long l = (long) (o.getData() * 1000);
          TimeUnit.MILLISECONDS.sleep(l);
        } catch (InterruptedException ignore) {
        }finally{
          is.takeGIL();
        }
        return BuiltIn.None;
      }
    }
    return PyErrorUtils.pyErrorFormat(
        PyErrorUtils.TypeError, "time.sleep only require 1 int argument");
  }

  @PyClassMethod
  public PyObject time(PyTupleObject args, PyDictObject kwArgs) {
    long l = System.currentTimeMillis();
    return new PyFloatObject(l / 1000.0);
  }
}
