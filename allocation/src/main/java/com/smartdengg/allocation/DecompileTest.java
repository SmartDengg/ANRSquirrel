package com.smartdengg.allocation;

/**
 * 创建时间:  2016/11/21 11:19 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public class DecompileTest {

  {
    String s = "";
  }

  {
    String s = "";
  }

  public void main() {

    Son[] sons = new Son[2];
    sons[0] = new Son("1");
    sons[1] = new Son("1");

    String s1 = "name0 = " + sons[0].name + "\n" + "name1 = " + sons[1].name;
    String s2 = "deng" + "wei" + "haha";

    String deng = "deng";
    String wei = "wei";
    String haha = "haha";
    String s3 = deng + wei + haha;

    System.out.printf(s1.toString());
    System.out.printf(s2.toString());
    System.out.printf(s3.toString());

    //Parent parent = new Parent(sons);
    //parent.go();
    //
    //String mode = "";
    //switch (mode) {
    //  case "MODE1":
    //    System.out.println("MODE1");
    //    break;
    //  case "MODE2":
    //    System.out.println("MODE2");
    //    break;
    //  case "MODE3":
    //    System.out.println("MODE3");
    //}
    //
    //List<String> list = new ArrayList<>();
    //for (String string : list) {
    //  /*empty*/
    //}
    //
    //for (String string : list) {
    //  System.err.println("for-each mode" + string);
    //}
    //
    //for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
    //  System.out.printf("iterator mode" + iterator.next());
    //}

    Inter inter = new Inter() {

      @Override public void print() {
        DecompileTest.this.print();
      }
    };

    Inter inter1 = new Inter() {
      @Override public void print() {

      }
    };

    InterClass interClass = new InterClass();
    String string = interClass.mString;

    //StaticInterClass staticInterClass = new StaticInterClass();

    StaticInner si = new StaticInner();
    si.innerMethod();

    Integer i = 1;

    Integer integer = new Integer(10);
    int i1 = integer;
  }

  private void print() {
  }

  ;
  private static int shared = 100;

  public static class StaticInner {
    public void innerMethod() {
      System.out.println("inner " + shared);
    }
  }

  private class Parent {

    private Son[] sons;

    private Parent(Son[] sons) {
      this.sons = sons;
    }

    private void go() {

      String result = "Names: ";
      for (int i = 0, n = sons.length; i < n; i++) {
        result += sons[i].name;
      }
      System.out.printf(result);
    }
  }

  private class Son {

    private String name;

    private Son(String name) {
      this.name = name;
    }
  }

  interface Inter {

    void print();
  }

  class InterClass {

    private String mString;
  }

  static class StaticInterClass {

  }
}
