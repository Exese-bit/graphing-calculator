package graphcalc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

//A class to handle the graph drawing within the graph box in the GUI. Does not handle the region outside the graph 
public class PixelGrid extends JPanel {
	private static ArrayList<Color[][]> pixelColors = new ArrayList<Color[][]>();
	private static ArrayList<boolean[][]> pixelVisible = new ArrayList<boolean[][]>();
	private static int functionAmount;
	private static ArrayList<String> functionCollection;

    private static ArrayList<Integer> pixelX = new ArrayList<Integer>();
    private static ArrayList<Integer> pixelY = new ArrayList<Integer>();
	
	public PixelGrid(ArrayList<String> functionCollections) {
		functionCollection = functionCollections;
		functionAmount = functionCollection.size();

        //Sets all the pixels up for graphing
		for(int functionIndex = 0; functionIndex < functionAmount; functionIndex++) {
			pixelColors.add(new Color[601][601]);
			pixelVisible.add(new boolean[601][601]);
			for(int x = 0; x < 601; x++) {
				for(int y = 0; y < 601; y++) {
					pixelColors.get(functionIndex)[x][y] = Color.white;
					pixelVisible.get(functionIndex)[x][y] = false;
				}
			}
		}
		functionAmount++;
		pixelColors.add(new Color[601][601]);
		pixelVisible.add(new boolean[601][601]);

        //Sets all the pixels for the axes lines 
		for(int x = 0; x < 601; x++) {
			for(int y = 0; y < 601; y++) {
				pixelColors.get(pixelColors.size() - 1)[x][y] = Color.white;
				pixelVisible.get(pixelColors.size() - 1)[x][y] = false;
			}
		}
		setOpaque(false);
		setPreferredSize(new Dimension(601, 601));
		setFocusable(true);
		requestFocusInWindow();
	}

    public void updateGrid(ArrayList<String> functionCollections) {
        functionCollection = functionCollections;
        functionAmount = functionCollection.size();
        pixelColors.clear();
        pixelVisible.clear();
        pixelX.clear();
        pixelY.clear();
        for(int functionIndex = 0; functionIndex < functionAmount; functionIndex++) {
			pixelColors.add(new Color[601][601]);
			pixelVisible.add(new boolean[601][601]);
			for(int x = 0; x < 601; x++) {
				for(int y = 0; y < 601; y++) {
					pixelColors.get(functionIndex)[x][y] = Color.white;
					pixelVisible.get(functionIndex)[x][y] = false;
				}
			}
		}
        functionAmount++;
        pixelColors.add(new Color[601][601]);
		pixelVisible.add(new boolean[601][601]);
        for(int x = 0; x < 601; x++) {
			for(int y = 0; y < 601; y++) {
				pixelColors.get(pixelColors.size() - 1)[x][y] = Color.white;
				pixelVisible.get(pixelColors.size() - 1)[x][y] = false;
			}
		}
    }
	
    //Graphs the point associated with the graph. Only used with graph, not axes lines 
	public void setPoint(int x, int y, Color color, boolean isVisible, int functionIndex) {
        if(functionIndex == -1) {
            for(int i = 0; i < functionAmount; i++) {
                pixelColors.get(i)[x][y] = Color.white;
                pixelVisible.get(i)[x][y] = false;
            }
        } else if(x < 601 && y < 601 && x > -1 && y > -1) {
            pixelColors.get(functionIndex)[x][y] = color;
            pixelVisible.get(functionIndex)[x][y] = isVisible;
            pixelX.add(x);
            pixelY.add(y);
		}
		repaint(x, y, 1, 1);
	}
    
    //Removes all visible pixels from the graph GUI 
    public void clearPixels() {
        int size = pixelX.size() - 1;
        while(size > -1) {
            setPoint(pixelX.get(size), pixelY.get(size), Color.white, false, -1);
            pixelX.remove(size);
            pixelY.remove(size);
            size--;
        }
    }
    
    //Returns whether a pixel for a specific function is visible at a location
    public boolean hasPixel(int x, int y, int functionIndex) {
        return pixelVisible.get(functionIndex)[x][y];
    }
    
    //Used to create axes lines and labels. Only shows the pixel if it is not below the graph.
	public void setPixel(int x, int y, Color color, boolean showPixel) {
		boolean isVisible = false;
        //Checks if the pixel is visible from the graph 
		for(int i = 0; i < functionAmount - 1; i++) {
			if(pixelVisible.get(i)[x][y]) {
				isVisible = true;
                if(color.equals(Color.black)) { //Don't antialias this pixel if it is touching the y or x axis
                    Color newColor = pixelColors.get(i)[x][y];
                    int r = newColor.getRed();
                    int g = newColor.getGreen();
                    int b = newColor.getBlue();
                    setPoint(x, y, new Color(r, g, b), true, i);
                }
			}
		}
		if(!isVisible) {
			if(showPixel) { //To put pixel on GUI
				pixelColors.get(pixelColors.size() - 1)[x][y] = color;
				pixelVisible.get(pixelColors.size() - 1)[x][y] = true;
			} else { //To remove pixel from GUI 
				pixelColors.get(pixelColors.size() - 1)[x][y] = Color.white;
				pixelVisible.get(pixelColors.size() - 1)[x][y] = false;
			}
			repaint(x, y, 1, 1);
		} else { //Don't draw pixel because it is under the graph 
			pixelColors.get(pixelColors.size() - 1)[x][y] = Color.white;
			pixelVisible.get(pixelColors.size() - 1)[x][y] = false;
			repaint(x, y, 1, 1);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(int functionIndex = 0; functionIndex < functionAmount; functionIndex++) {
			for(int x = 0; x < 601; x++) {
				for(int y = 0; y < 601; y++) {
					if(pixelVisible.get(functionIndex)[x][y]) {
						g.setColor(pixelColors.get(functionIndex)[x][y]);
						g.fillRect(x, y, 1, 1);
					}
				}
			}
		}
	}
}
