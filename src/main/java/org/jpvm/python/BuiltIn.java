package org.jpvm.python;

import java.io.IOException;
import org.jpvm.errors.PyException;
import org.jpvm.errors.PyTypeError;
import org.jpvm.errors.PyTypeNotMatch;
import org.jpvm.excptions.types.*;
import org.jpvm.module.filestream.PyFileStreamObject;
import org.jpvm.module.sys.Sys;
import org.jpvm.objects.*;
import org.jpvm.objects.pyinterface.TypeDoIterate;
import org.jpvm.objects.pyinterface.TypeIterable;
import org.jpvm.objects.pyinterface.TypeRichCompare;
import org.jpvm.objects.types.PyListType;
import org.jpvm.objects.types.PySuperType;
import org.jpvm.objects.types.PyTypeType;
import org.jpvm.protocols.PyMappingMethods;
import org.jpvm.protocols.PyNumberMethods;
import org.jpvm.protocols.PySequenceMethods;
import org.jpvm.pvm.Abstract;
import org.jpvm.pvm.PVM;
import org.jpvm.pvm.ThreadState;

public class BuiltIn {

  public static PyBoolObject True = new PyBoolObject(true);
  public static PyBoolObject False = new PyBoolObject(false);
  public static PyNoneObject None = new PyNoneObject();
  public static PyNoneObject NULL = new PyNoneObject();
  public static PyObject ELLIPSIS = new PyObject();
  public static PySetObject FROZENSET = new PySetObject(true);

  /** define jpython internal exceptions */
  public static PyObject PyExcStopIteration = new PyExcStopIteration();

  public static PyObject notImplemented = new PyObject();
  public static PyDictObject dict;
  public static Class<?>[] parameterTypes =
      new Class<?>[] {PyTupleObject.class, PyDictObject.class};

  static {
    dict = new PyDictObject();
    try {
      addType("int", PyLongObject.type);
      addType("super", PySuperObject.type);
      addType("list", PyListObject.type);
      addType("set", PySetObject.type);
      addType("float", PyFloatObject.type);
      addType("complex", PyComplexObject.type);
      addType("str", PyUnicodeObject.type);
      addType("type", PyTypeType.type);
      addType("dict", PyDictObject.type);
      addType("bool", PyBoolObject.type);
      addType("tuple", PyTupleObject.type);
      addType("range", PyRangeObject.type);
      addType("True", True);
      addType("False", False);
      addType("None", None);
      addType("Ellipsis", ELLIPSIS);
      addType("stdout", Sys.stdout);
      addType("BaseException", new PyPythonBaseExceptionType());
      addType("Exception", new PyExceptionType());
      addType("AssertionError", new PyAssertionErrorType());
      addType("AttributeError", new PyAttributeErrorType());
      addType("KeyError", new PyKeyErrorType());
      addType("NameError", new PyNameErrorType());
      addType("NotImplementedError", new PyNotImplementedErrorType());
      addType("RuntimeError", new PyRuntimeErrorType());
      addType("TypeError", new PyTypeErrorType());
      addType("ValueError", new PyValueErrorType());
      addType("ZeroDivisionError", new PyZeroDivisionErrorType());
    } catch (PyException ignore) {
    }
  }

  public static void addType(String name, PyObject type) throws PyException {
    dict.put(PyUnicodeObject.getOrCreateFromInternStringPool(name, true), type);
  }

  public static void doInit() {
    try {
      // load builtin function & object into builtin dict
      registerBuiltinFunction("print");
      registerBuiltinFunction("sum");
      registerBuiltinFunction("iter");
      registerBuiltinFunction("len");
      registerBuiltinFunction("sorted");
      registerBuiltinFunction("max");
      registerBuiltinFunction("min");
      registerBuiltinFunction("sorted");
      registerBuiltinFunction("all");
      registerBuiltinFunction("any");
      registerBuiltinFunction("next");
      registerBuiltinFunction("__build_class__");
      dict.put(PyUnicodeObject.getOrCreateFromInternStringPool("object", true), PyObject.type);
    } catch (NoSuchMethodException | PyException ignore) {
    }
  }

  public static void registerBuiltinFunction(String name)
      throws NoSuchMethodException, PyException {
    Class<BuiltIn> clazz = BuiltIn.class;
    if (dict.get(PyUnicodeObject.getOrCreateFromInternStringPool(name, true)) == null) {
      PyNativeMethodObject mp =
          new PyNativeMethodObject(
              clazz.getMethod(name, PyTupleObject.class, PyDictObject.class), true);
      dict.put(PyUnicodeObject.getOrCreateFromInternStringPool(name, true), mp);
    }
  }

  public static PyObject loadFromDict(PyUnicodeObject name) {
    return dict.get(name);
  }

  public static PyObject loadFromDict(PyUnicodeObject name, PyObject defaultValue) {
    return dict.getOrDefault(name, defaultValue);
  }

  public static PyObject loadFromDict(String name) {
    return loadFromDict(new PyUnicodeObject(name));
  }

  public static PyObject print(PyTupleObject args, PyDictObject kwArgs)
      throws PyException, IOException {
    // do error checking
    PyObject std;
    PyObject end;
    if (kwArgs == null) {
      std = Sys.stdout;
      end = PyUnicodeObject.getOrCreateFromInternStringPool("\n", true);
    } else {
      std =
          kwArgs.getOrDefault(
              PyUnicodeObject.getOrCreateFromInternStringPool("std", true), Sys.stdout);
      end =
          kwArgs.getOrDefault(
              PyUnicodeObject.getOrCreateFromInternStringPool("end", true),
              PyUnicodeObject.getOrCreateFromInternStringPool("\n", true));
    }
    if (!(std instanceof PyFileStreamObject stream)) {
      throw new PyTypeNotMatch("std require stdout PyFileStreamObject");
    }
    if (!(end instanceof PyUnicodeObject uni)) {
      throw new PyTypeNotMatch("end require stdout PyUnicodeObject");
    }
    TypeDoIterate iterator = args.getIterator();
    PyObject o;
    o = iterator.next();
    while (o != BuiltIn.PyExcStopIteration) {
      stream.writeString(o.str().getData());
      if (iterator.hasNext()) stream.writeString(" ");
      o = iterator.next();
    }
    stream.writeString(uni.getData());
    return BuiltIn.None;
  }

  public static PyObject iter(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    if (args.size() == 1) {
      PyObject object = args.get(0);
      if (object instanceof TypeIterable itr) {
        return (PyObject) itr.getIterator();
      }
      throw new PyException("object " + object.repr() + " can not be an iterator");
    }
    throw new PyException("iter function require only 1 argument");
  }

  public static PyObject sum(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    if (args.size() == 1) {
      PyObject o = args.get(0);
      if (o instanceof TypeIterable itr) {
        TypeDoIterate iterator = itr.getIterator();
        PyObject result = new PyLongObject(0);
        while (iterator.hasNext()) {
          result = Abstract.add(result, iterator.next());
          if (result == BuiltIn.notImplemented)
            throw new PyTypeError("can apply sum on " + o.repr());
        }
        return result;
      }
    }
    throw new PyTypeError("sum only require one iterable argument");
  }

  public static PyObject len(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    if (args.size() == 1) {
      PyObject o = args.get(0);
      if (o instanceof TypeIterable itr) {
        TypeDoIterate iterator = itr.getIterator();
        return new PyLongObject(iterator.size());
      } else if (o instanceof PySequenceMethods seq) {
        return seq.sqLength(null);
      } else if (o instanceof PyMappingMethods map) {
        return map.mpLength(null);
      }
    }
    throw new PyTypeError("sum only require one iterable argument");
  }

  public static PyObject sorted(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    PyListObject list = PyListType.getListFromIterable(args, kwArgs);
    list.sort(args, kwArgs);
    return list;
  }

  public static PyObject max(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    PyListObject list = PyListType.getListFromIterable(args, kwArgs);
    if (list.size() == 0) return BuiltIn.None;
    PyObject res = list.get(0);
    for (int i = 0; i < list.size(); i++) {
      if (res.richCompare(list.get(i), TypeRichCompare.Operator.Py_LE).isTrue()) {
        res = list.get(i);
      }
    }
    return res;
  }

  public static PyObject min(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    PyListObject list = PyListType.getListFromIterable(args, kwArgs);
    if (list.size() == 0) return BuiltIn.None;
    PyObject res = list.get(0);
    for (int i = 0; i < list.size(); i++) {
      if (res.richCompare(list.get(i), TypeRichCompare.Operator.Py_GT).isTrue()) {
        res = list.get(i);
      }
    }
    return res;
  }

  public static PyObject all(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    //  transform an iterable object to list
    PyListObject list = PyListType.getListFromIterable(args, kwArgs);
    if (list.size() == 0) return BuiltIn.False;
    for (int i = 0; i < list.size(); i++) {
      PyObject o = list.get(i);
      if (o == BuiltIn.False) return BuiltIn.False;
      else if (o instanceof PyNumberMethods num) {
        if (((PyBoolObject) num.bool()).isFalse()) return BuiltIn.False;
      } else return BuiltIn.False;
    }
    return BuiltIn.True;
  }

  public static PyObject any(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    //  transform an iterable object to list
    PyListObject list = PyListType.getListFromIterable(args, kwArgs);
    if (list.size() == 0) return BuiltIn.False;
    for (int i = 0; i < list.size(); i++) {
      PyObject o = list.get(i);
      if (o == BuiltIn.True) return BuiltIn.True;
      else if (o instanceof PyNumberMethods num) {
        if (((PyBoolObject) num.bool()).isTrue()) return BuiltIn.True;
      }
    }
    return BuiltIn.False;
  }

  public static PyObject next(PyTupleObject args, PyDictObject kwArgs) throws PyException {
    if (args.size() == 1) {
      PyObject o = args.get(0);
      if (o instanceof TypeDoIterate iterator) {
        return iterator.next();
      }
    }
    throw new PyException("next function require only 1 argument that is an iterator");
  }

  public static PyObject __build_class__(PyTupleObject args, PyDictObject kwArgs)
      throws PyException {
    if (args.size() < 2) throw new PyException("buildClass function require at least 2 arguments");
    PyObject function = args.get(0);
    PyObject name = args.get(1);
    PyTupleObject bases = new PyTupleObject(args.size() - 2);
    for (int i = 2; i < args.size(); i++) {
      bases.set(i - 2, args.get(i));
    }
    PyDictObject locals = new PyDictObject();
    ThreadState ts = PVM.getThreadState();
    Abstract.abstractCall(function, null, null, null, ts.getCurrentFrame(), locals);
    args = new PyTupleObject(3);
    args.set(0, name);
    args.set(1, bases);
    args.set(2, locals);
    return PyTypeType.type.call(null, args, kwArgs);
  }
}
