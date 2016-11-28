/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ij.measure;

// TODO: Auto-generated Javadoc
/** This class fits a spline function to a set of points.
	It is based on the InitSpline() and EvalSine() functions from 
	XY (http://www.trilon.com/xv/), an interactive image manipulation
	program for the X Window System written by John Bradley. Eric Kischell
	(keesh@ieee.org) converted these functions to Java and integrated
	them into the PolygonRoi class.
*/
public class SplineFitter {
	
	/** The y 2. */
	private double[] y2;
	
	/** The extend by. */
	private static int EXTEND_BY = 7;
	
	/** The extend by. */
	private int extendBy;
	
	/** The ypoints. */
	private float[] xpoints, ypoints;
	
	/** The npoints. */
	private int npoints;
	
	/** The iypoints. */
	private int[] ixpoints, iypoints;

	/**
	 * Instantiates a new spline fitter.
	 *
	 * @param x the x
	 * @param y the y
	 * @param n the n
	 */
	public SplineFitter(int[] x, int[] y, int n) {
		initSpline(x, y, n);
	}

	/**
	 *  For closed curves: the first and last y value should be identical;
	 * 	internally, a periodic continuation with a few will be used at both
	 * 	ends.
	 *
	 * @param x the x
	 * @param y the y
	 * @param n the n
	 * @param closed the closed
	 */
	public SplineFitter(float[] x, float[] y, int n, boolean closed) {
		initSpline(x, y, n, closed);
	}

	/**
	 * Instantiates a new spline fitter.
	 *
	 * @param x the x
	 * @param y the y
	 * @param n the n
	 */
	public SplineFitter(float[] x, float[] y, int n) {
		initSpline(x, y, n, false);
	}

	/**
	 *  Given arrays of data points x[0..n-1] and y[0..n-1], computes the
	 * 		values of the second derivative at each of the data points
	 * 		y2[0..n-1] for use in the evalSpline() function.
	 *
	 * @param x the x
	 * @param y the y
	 * @param n the n
	 */
	private void initSpline(int[] x, int[] y, int n) {
		int i,k;
		double p,qn,sig,un;
		y2 = new double[n];	 // cached
		double[] u	= new double[n];
		for (i=1; i<n-1; i++) {
			// 888 chk for div by 0?
			sig = ((double) x[i]-x[i-1]) / ((double) x[i+1] - x[i-1]);
			p = sig * y2[i-1] + 2.0;
			y2[i] = (sig-1.0) / p;
			u[i] = (((double) y[i+1]-y[i]) / (x[i+1]-x[i])) -
				   (((double) y[i]-y[i-1]) / (x[i]-x[i-1]));
			u[i] = (6.0 * u[i]/ (x[i+1]-x[i-1]) - sig*u[i-1]) / p;
		}
		qn = un = 0.0;
		y2[n-1] = (un-qn*u[n-2]) / (qn*y2[n-2]+1.0);
		for (k=n-2; k>=0; k--)
			y2[k] = y2[k]*y2[k+1]+u[k];
		ixpoints = x;
		iypoints = y;
		npoints = n;
	}

	/**
	 * Inits the spline.
	 *
	 * @param x the x
	 * @param y the y
	 * @param n the n
	 * @param closed the closed
	 */
	private void initSpline(float[] x, float[] y, int n, boolean closed) {
		if (closed) {					//add periodic continuation at both ends
			extendBy = EXTEND_BY;
			if (extendBy>=n)
				extendBy = n - 1;
			int n2 = n + 2*extendBy;
			float[] xx = new float[n2];
			float[] yy = new float[n2];
			for (int i=0; i<extendBy; i++) {
				xx[i] = x[n-(extendBy-i+1)] - x[n-1];
				yy[i] = y[n-(extendBy-i+1)];
			}
			for (int i=extendBy; i<extendBy+n; i++) {
				xx[i] = x[i-extendBy];
				yy[i] = y[i-extendBy];
			}
			for (int i=extendBy+n; i<n2; i++) {
				xx[i] = x[i+1-(extendBy+n)] - x[0] + x[n-1];
				yy[i] = y[i+1-(extendBy+n)];
			}
			n = n2;
			x = xx;
			y = yy;
		}
		int i,k;
		double p,qn,sig,un;
		y2 = new double[n];	 // cached
		double[] u	= new double[n];
		for (i=1; i<n-1; i++) {
			// 888 chk for div by 0?
			sig = ((double) x[i]-x[i-1]) / ((double) x[i+1] - x[i-1]);
			p = sig * y2[i-1] + 2.0;
			y2[i] = (sig-1.0) / p;
			u[i] = (((double) y[i+1]-y[i]) / (x[i+1]-x[i])) -
				   (((double) y[i]-y[i-1]) / (x[i]-x[i-1]));
			u[i] = (6.0 * u[i]/ (x[i+1]-x[i-1]) - sig*u[i-1]) / p;
		}
		qn = un = 0.0;
		y2[n-1] = (un-qn*u[n-2]) / (qn*y2[n-2]+1.0);
		for (k=n-2; k>=0; k--)
			y2[k] = y2[k]*y2[k+1]+u[k];
		xpoints = x;
		ypoints = y;
		npoints = n;
	}

	/**
	 *  Evalutes spline function at given point.
	 *
	 * @param xp the xp
	 * @return the double
	 */
	public double evalSpline(double xp) {
		if (xpoints!=null)
			return evalSpline(xpoints, ypoints, npoints, xp);
		else
			return evalSpline(ixpoints, iypoints, npoints, xp);
	}
	
	/**
	 * Eval spline.
	 *
	 * @param x the x
	 * @param y the y
	 * @param n the n
	 * @param xp the xp
	 * @return the double
	 */
	public double evalSpline(int x[], int y[], int n, double xp) {
		int klo,khi,k;
		double h,b,a;
		klo = 0;
		khi = n-1;
		while (khi-klo > 1) {
			k = (khi+klo) >> 1;
			if (x[k] > xp) khi = k;
			else klo = k;
		}
		h = x[khi] - x[klo];
		/* orig code */
		/* if (h==0.0) FatalError("bad xvalues in splint\n"); */
		if (h==0.0) return (0.0);  /* arbitr ret for now */
		a = (x[khi]-xp)/h;
		b = (xp-x[klo])/h;
		// should have better err checking
		if(y2==null) return (0.0);
		return (a*y[klo] + b*y[khi] + ((a*a*a-a)*y2[klo] +(b*b*b-b)*y2[khi]) * (h*h) / 6.0);
	}
	
	/**
	 * Eval spline.
	 *
	 * @param x the x
	 * @param y the y
	 * @param n the n
	 * @param xp the xp
	 * @return the double
	 */
	public double evalSpline(float x[], float y[], int n, double xp) {
		int klo,khi,k;
		double h,b,a;
		klo = 0;
		khi = n-1;
		while (khi-klo>1) {
			k = (khi+klo)>>1;
			if (x[k]>xp)
				khi = k;
			else
				klo = k;
		}
		h = x[khi] - x[klo];
		/* orig code */
		/* if (h==0.0) FatalError("bad xvalues in splint\n"); */
		if (h==0.0)
			return (0.0);  /* arbitr ret for now */
		a = (x[khi]-xp)/h;
		b = (xp-x[klo])/h;
		// should have better err checking
		if (y2==null)
			return (0.0);
		return (a*y[klo] + b*y[khi] + ((a*a*a-a)*y2[klo] +(b*b*b-b)*y2[khi]) * (h*h) / 6.0);
	}

}
