public class t1 implements i1{
    public void a(){
        i1.super.a();
        System.out.println(2);
    }
    public static void main(String[] args) {
        new t1().a();
    }
}
