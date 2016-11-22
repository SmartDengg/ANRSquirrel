package com.smartdengg.allocation;

/**
 * 创建时间:  2016/11/21 11:19 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public class DecompileTest {

  public void main() {

    //Son[] sons = new Son[2];
    //sons[0] = new Son("1");
    //sons[1] = new Son("1");
    //
    //String s1 = "name0 = " + sons[0].name + "\n" + "name1 = " + sons[1].name;
    //String s2 = "deng" + "wei" + "haha";
    //
    //String deng = "deng";
    //String wei = "wei";
    //String haha = "haha";
    //String s3 = deng + wei + haha;
    //
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
    };

    Inter inter1 = new Inter() {
    };

    InterClass interClass = new InterClass();
    String string = interClass.mString;

    StaticInterClass staticInterClass = new StaticInterClass();
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

  }

  class InterClass {

    private String mString;
  }

  static class StaticInterClass {

  }
}
