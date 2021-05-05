package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class Test {

	public static class Matrix2d implements java.io.Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 20210505L;
		private int matrix[][];
		private Integer m[][];
		// create instance of Random class
		Random rand = new Random();
		private static final int bound = 1000;

		public Matrix2d(int dim) {
			matrix = new int[dim][dim];
			for (int row = 0; row < dim; ++row) {
				for (int col = 0; col < dim; ++col) {
					matrix[row][col] = rand.nextInt(bound);
				}
			}

			m = new Integer[dim][dim];
			for (int row = 0; row < dim; ++row) {
				for (int col = 0; col < dim; ++col) {
					m[row][col] = rand.nextInt(bound);
				}
			}
		}

		public void printOut() {
			System.out.println("-- matrix --");
			for (int row = 0; row < matrix.length; ++row) {
				for (int col = 0; col < matrix[row].length; ++col) {
					System.out.print(matrix[row][col] + "\t");
				}
				System.out.println();
			}

			System.out.println("-- m --");
			for (int row = 0; row < m.length; ++row) {
				for (int col = 0; col < m[row].length; ++col) {
					System.out.print(m[row][col] + "\t");
				}
				System.out.println();
			}
		}
	}

	public static void main(String[] args) {
		Matrix2d matrix2d = new Matrix2d(3);
		matrix2d.printOut();
		String filename = "file.ser";

		// Serialization
		try {
			// Saving of object in a file
			FileOutputStream file = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(file);

			// Method for serialization of object
			out.writeObject(matrix2d);

			out.close();
			file.close();

			System.out.println("Object has been serialized");

		}
		catch (IOException ex) {
			System.out.println("IOException is caught");
		}

		Matrix2d object1 = null;

		// Deserialization
		try {
			// Reading the object from a file
			FileInputStream file = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(file);

			// Method for deserialization of object
			object1 = (Matrix2d) in.readObject();

			in.close();
			file.close();

			object1.printOut();
		}

		catch (IOException ex) {
			System.out.println("IOException is caught");
		}

		catch (ClassNotFoundException ex) {
			System.out.println("ClassNotFoundException is caught");
		}
		
		
	}
}
