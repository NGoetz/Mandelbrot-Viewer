package fractal;

public class ComplexNumber {
double re;
double im;
	public ComplexNumber(double r, double i) {
		re=r;
		im=i;
	}
public ComplexNumber add(ComplexNumber c){
	return new ComplexNumber(re+c.re,im+c.im);
}
public ComplexNumber multiply(ComplexNumber c){
	return new ComplexNumber(re*c.re-im*c.im, re*c.im+im*c.re);
}
public double norm(){
	return Math.sqrt(re*re+im*im);
}
public String text(){
	return re+" + "+im+"i ";
}
}
