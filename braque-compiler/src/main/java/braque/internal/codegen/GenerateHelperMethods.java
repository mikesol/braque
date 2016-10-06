package braque.internal.codegen;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Created by mikesolomon on 21/09/16.
 */
public class GenerateHelperMethods {
    static void makeHelperMethods(ProcessingEnvironment processingEnvironment) {
        CodeBuilder builder = new CodeBuilder();
        builder.pkg(Utils.STATIC_METHOD_CLASSES_PACKAGE).klass().a("HelperMethods").sbeg()
                .a("  static class ScoredReturnType<T extends ").processing().a("BraqueObject> {").n()
                .a("    final T mBraqueObject;").n()
                .a("    final int mScore;").n()
                .a("    ScoredReturnType(T braqueObject, int score) {").n()
                .a("      mBraqueObject = braqueObject;").n()
                .a("      mScore = score;").n()
                .a("    }").n()
                .a("  }").n()
                .a("  static String addUIDs(String s, java.util.List<String> previousUIDs) {").n()
                .a("    int i = 0;").n()
                .a("    while (s.indexOf(\"*\") >= 0) {").n()
                .a("      s = s.substring(0,s.indexOf(\"*\"))+previousUIDs.get(i++)+s.substring(s.indexOf(\"*\")+1);").n()
                .a("    }").n()
                .a("    return s;").n()
                .a("  }").n()
                .a("  static java.util.List<String> append(java.util.List<String> source, String s){").n()
                .a("    java.util.List<String> out = new java.util.ArrayList<>(source);").n()
                .a("    out.add(s);").n()
                .a("    return out;").n()
                .a("  }").n()
                .a("  static java.util.List<String> append(java.util.List<String> source, int i){").n()
                .a("    return append(source, \"\"+i);").n()
                .a("  }").n()
                .a("  static private final java.util.Comparator<String> maybeNumberComparator = new java.util.Comparator<String>() {").n()
                .a("      @Override").n()
                .a("      public int compare(String s1, String s2) {").n()
                .a("          Integer i1, i2;").n()
                .a("          if (s1 == null || s2 == null);").n()
                .a("          String[] split1 = s1.split(\"/\");").n()
                .a("          String[] split2 = s2.split(\"/\");").n()
                .a("          if (split1.length == 0 || split2.length == 0) {").n()
                .a("            return -1;").n()
                .a("          }").n()
                .a("          try {").n()
                .a("            i1 = Integer.valueOf(split1[split1.length - 1]);").n()
                .a("            i2 = Integer.valueOf(split2[split2.length - 1]);").n()
                .a("            return i1 - i2;").n()
                .a("          } catch (NumberFormatException e) {").n()
                .a("              return -1;").n()
                .a("          }").n()
                .a("      }").n()
                .a("  };").n()
                .a("  static private final java.util.Comparator<").a("ScoredReturnType<? extends ").processing().a("BraqueObject>")
                .a("> scoredReturnTypeComparator = new java.util.Comparator<").a("ScoredReturnType<? extends ").processing()
                .a("BraqueObject>").a(">() {").n()
                .a("      @Override").n()
                .a("      public int compare(ScoredReturnType<? extends ").processing().a("BraqueObject").a("> s1, ScoredReturnType<? extends ").processing().a("BraqueObject").a("> s2) {").n()
                .a("          return s1.mScore - s2.mScore;").n()
                .a("      }").n()
                .a("  };").n()
                .n()
                .a("  static void sortIfIntegersElseLeaveInRandomOrder(java.util.List<String> stringList) {").n()
                .a("      java.util.Collections.sort(stringList, maybeNumberComparator);").n()
                .a("  }").n()
                .a("  static <T extends ").processing().a("BraqueObject").a("> void sortScoredReturnTypes(java.util.List<ScoredReturnType<T>> rtList) {").n()
                .a("      java.util.Collections.sort(rtList, scoredReturnTypeComparator);").n()
                .a("  }").n();;
        makeSafeCastFunction(builder, "String");
        makeSafeCastFunction(builder, "Float");
        makeSafeCastFunction(builder, "Double");
        makeSafeCastFunction(builder, "Integer");
        makeSafeCastFunction(builder, "Long");
        makeSafeCastFunction(builder, "Boolean");
        builder.spend();
        Utils.write(Utils.STATIC_METHOD_CLASSES_PACKAGE + Utils.DOT + "HelperMethods", builder, processingEnvironment);
    }

    static private void makeSafeCastFunction(CodeBuilder builder, String objType) {
        builder.n()
                .a("  static " + objType + " safeCast" + objType + "(Object o) {").n()
                .a("    if (o == null) {").n()
                .a("      return null;").n()
                .a("    }").n()
                .a("    if (o instanceof " + objType + ") {").n()
                .a("      return (" + objType + ")o;").n()
                .a("    }").n()
                .a("    return null;").n()
                .a("  }").n();
    }
}
