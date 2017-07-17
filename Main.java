import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		double[] blend = new double[Function.variationsCount()];
        blend[0] = 1;
		blend[3] = 1;
        Function[] spgasket = {
        		new Function(blend, new double[]{0.5f, 0f, 0f, 0f, 0.5f, 0f}, 1),
        		new Function(blend, new double[]{0.5f, 0f, 0.5f, 0f, 0.5f, 0}, 1),
        		new Function(blend, new double[]{0.5f, 0f, 0f, 0f, 0.5f, 0.5f}, 1)
        };
        int iterations = 100000000;
        int width = 2560;
        int height = 1600;
        try {
        	double zoom = 0.22;
        	Point origin = new Point(1.5, 3);
        	int[][] plot = ChaosGame.run(spgasket, iterations, origin, zoom, width, height);
        	String imageName = String.format("shifted");
			imageName = Painter.paint(plot, imageName);
//    			Function.record(spgasket, imageName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//        Painter.colorTest();

	}
}
