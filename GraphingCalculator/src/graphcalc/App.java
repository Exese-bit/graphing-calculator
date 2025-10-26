package graphcalc;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashSet;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.border.LineBorder;
import javax.swing.*;

/*
 * A graphing calculator built completely from scratch with a custom parser, math engine, GUI, and graphing algorithm. Utilizes the Java Math class for basic operations and trigonometry. Built with Swing.
 *
 * @author Jonah Zilberter
 * @since 2025-06-07
 */
public class App extends JPanel {
    
    //Variables for parsing + keep track of graph positions
	private static ArrayList<Integer> parseIndex = new ArrayList<Integer>();
	private static ArrayList<Double> initialX = new ArrayList<Double>();
	private static ArrayList<Double> finalX = new ArrayList<Double>();
	private static ArrayList<double[]> yValues = new ArrayList<double[]>();
	private static ArrayList<String[]> yValuePositions = new ArrayList<String[]>();
	private static ArrayList<int[]> yPointPositions = new ArrayList<int[]>();
	private static ArrayList<String> functionCollection;
	private static Color[] functionColor = {new Color(56, 125, 34), //green
						new Color(15, 61, 148), //blue
						new Color(148, 15, 15), //red
						new Color(222, 211, 38), //yellow
						new Color(201, 109, 22), //orange
						new Color(212, 48, 197), //pink
						new Color(120, 48, 212)};  // purple
    private static ArrayList<ArrayList<Integer[]>> yPairs = new ArrayList<ArrayList<Integer[]>>();
	private static ArrayList<ArrayList<Integer[]>> xPairs = new ArrayList<ArrayList<Integer[]>>();
    private static ArrayList<ArrayList<Integer>> allYPoints = new ArrayList<ArrayList<Integer>>();
	private static ArrayList<ArrayList<Integer>> allXPoints = new ArrayList<ArrayList<Integer>>();
    private static ArrayList<HashSet<ArrayList<Integer>>> allCoordinates = new ArrayList<HashSet<ArrayList<Integer>>>();

    //range variables
	private static double minimumX;
	private static double maximumX;
	private static double minimumY;
	private static double maximumY;

    //variables to handle pan + zoom
	private static Integer prevX;
	private static Integer prevY;
	private static Double shiftX;
	private static Double shiftY;
	private static boolean isShowingPoint;
    private static int selectedFunction;

    //variables to handle axis lines
	private static double[] xLines;
	private static double[] yLines;
	private static int[] xLinePositions;
	private static int[] yLinePositions;
    
    //variables for graphics 
    private static PixelGrid grid;
    private static JLayeredPane drawerPanel;
    private static JFrame frame;
	private static JLabel[] xLabels;
	private static JLabel[] yLabels;
    private static JLabel[] pointVisualizerLabels;

    //variables for outside of graph GUI 
    private static JScrollPane scrollPane;
    private static JPanel listPanel;
    private static ArrayList<JPanel> rowPanels;
    private static ArrayList<JButton> rowButtons;
    private static ArrayList<JTextField> textFields;
    private static ArrayList<JPanel> selectPanels;
    private static ArrayList<JPanel> removePanels;
    private static ArrayList<Index> indexes;
    private static JLabel consoleText;
    
	public static void main(String[] args) {

		Scanner userinput = new Scanner(System.in);

		System.out.println("____________________________________________________\n");
		System.out.println("███████╗██╗██╗      █████╗   █████╗██╗      █████╗  ");
		System.out.println("╚════██║██║██║     ██╔══██╗██╔══██╗██║     ██╔══██╗ ");
		System.out.println("  ███╔═╝██║██║     ██║  ╚═╝███████║██║     ██║  ╚═╝ ");
		System.out.println("██╔══╝  ██║██║     ██║  ██╗██╔══██║██║     ██║  ██╗ ");
		System.out.println("███████╗██║███████╗╚█████╔╝██║  ██║███████╗╚█████╔╝ ");
		System.out.println("╚══════╝╚═╝╚══════╝ ╚════╝ ╚═╝  ╚═╝╚══════╝ ╚════╝  ");
		System.out.println("____________________________________________________\n");
		functionCollection = new ArrayList<String>();

        functionCollection.add("");
        drawGUI();
		setUpGraph(false);
		for(int functionIndex = 0; functionIndex < functionCollection.size(); functionIndex++) {
			graph(functionIndex);
		}
		updateLines(0);
		createLines();
		createLabels();
		
		drawerPanel.setVisible(true);
        movePointVisualizer(false, 10, 10, 0);

		grid.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
                if(!isShowingPoint) {
                    if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                        addRow(rowPanels.size());
                    } 
                    if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                        deleteRow(rowPanels.size() - 1);
                    }
                    if(e.getKeyChar() == 'i' | e.getKeyChar() == 'o') { //if zooming
                        double lineDiff = 0;
                        
                        //find the distance between visible axis lines
                        for(int i = 0; i < 23; i++) {
                            if(xLinePositions[i] != -1 && xLinePositions[i] != 601) {
                                lineDiff = xLinePositions[i + 1] - xLinePositions[i];
                                i = 23;
                            }
                        }
                        if(e.getKeyChar() == 'o') { //zoom out
                            deleteLines();
                            zoom(-2);
                            for(int functionIndex = 0; functionIndex < functionCollection.size(); functionIndex++) {
                                graph(functionIndex);
                            }
                            createLines();
                            if(lineDiff < 60) { //If axis lines are too close, update their spacing
                                deleteLines();
                                updateLines(1);
                                createLines();
                            }
                        }
                        if(e.getKeyChar() == 'i') { //zoom in
                            deleteLines();
                            zoom(+2);
                            for(int functionIndex = 0; functionIndex < functionCollection.size(); functionIndex++) {
                                graph(functionIndex);
                            }
                            createLines();
                            if(lineDiff > 100) { //If axis lines are too far apart, update their spacing
                                deleteLines();
                                updateLines(-1);
                                createLines();
                            }
                        }
                        deleteLines();
                        fixGraph();
                        createLines();
                        createLabels();
                    }
                }
			}
		});
		grid.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
                if(!isShowingPoint) { //If panning
                    if(e.getX() - prevX != 0) { //If panning sideways
                        int diff = e.getX() - prevX;
                        deleteLines();
                        shiftYValues(diff);
                        setUpGraph(false);
                        for(int i = 0; i < functionCollection.size(); i++) {
                            graph(i);
                        }
                        prevX = e.getX();
                        createLines();
                        double lineDiff = Math.abs(xLines[13] - xLines[12]); //Distance between x lines
                        if(Math.abs(shiftX) > lineDiff) { //If the pan is too far sideways, update line positions
                            while(Math.abs(shiftX) > lineDiff) {
                                deleteLines();
                                if(shiftX > 0) {
                                    for(int i = 0; i < 23; i++) {
                                        xLines[i] -= lineDiff;
                                    }
                                    shiftX -= lineDiff;
                                } else if(shiftX < 0) {
                                    for(int i = 0; i < 23; i++) {
                                        xLines[i] += lineDiff;
                                    }
                                    shiftX += lineDiff;
                                }
                                createLines();
                                createLabels();
                            }
                        }
                    }
                    if(e.getY() - prevY != 0) { //If panning up/down
                        int diff = e.getY() - prevY;
                        deleteLines();
                        displaceYValues(diff);
                        setUpGraph(false);
                        for(int i = 0; i < functionCollection.size(); i++) {
                            graph(i);
                        }
                        prevY = e.getY();
                        createLines();
                        double lineDiff = Math.abs(xLines[13] - xLines[12]); //Distance between y lines
                        if(Math.abs(shiftY) > lineDiff) { //If panning too far up/down, update y line positions
                            while(Math.abs(shiftY) > lineDiff) {
                                deleteLines();
                                if(shiftY > 0) {
                                    for(int i = 0; i < 23; i++) {
                                        yLines[i] -= lineDiff;
                                    }
                                    shiftY -= lineDiff;
                                } else if(shiftY < 0) {
                                    for(int i = 0; i < 23; i++) {
                                        yLines[i] += lineDiff;
                                    }
                                    shiftY += lineDiff;
                                }
                                createLines();
                            } 
                        }
                    }
                    createLabels();
                } else { //If showing the point associated with x value, move the point visualizer with the mouse drag
                    if(e.getX() > -1 && e.getX() < 601 && e.getY() > -1 && e.getY() < 601) {
                        int index = allXPoints.get(selectedFunction).indexOf(e.getX());
                        if(index != -1) {
                            movePointVisualizer(true, e.getX(), 601 - allYPoints.get(selectedFunction).get(index), selectedFunction);
                        }
                    }
                }
			}
		});
		grid.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				prevX = e.getX();
				prevY = e.getY(); 
                int touching = -1;

                //Check if the mouse clicked on the graph
                for(int i = 0; i < functionCollection.size(); i++) {
                    ArrayList<Integer> test = new ArrayList<Integer>();
                    test.add(e.getX() - 1);
                    test.add(603 - e.getY());
                    for(int k = 0; k < 3; k++) {
                        for(int j = 0; j < 7; j++) {
                            try {
                                if(allCoordinates.get(i).contains(test)) {
                                    touching = i;
                                }
                                test.set(1, test.get(1) - 1);
                            } catch (IndexOutOfBoundsException s) {

                            }
                        }
                        test.set(0, test.get(0) + 1);
                        test.set(1, 603 - e.getY());
                    }
                }
                if(touching == -1) { //If mouse pressed outside of graph
                    prevX = e.getX();
				    prevY = e.getY();
                    grid.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    movePointVisualizer(false, 1, 1, 0);
                    isShowingPoint = false;
                } else { //If mouse pressed on graph, show the point of the graph associated with mouse location
                    isShowingPoint = true;
                    selectedFunction = touching;
                    int index = allXPoints.get(touching).indexOf(prevX);
                    if(index > -1) {
                        movePointVisualizer(true, e.getX(), 601 - allYPoints.get(touching).get(index), touching);
                    }
                }
			}
			public void mouseReleased(MouseEvent e) {
                if(!isShowingPoint) { //If the mouse was not pressed on the graph
				    grid.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				    prevX = null;
				    prevY = null;
                } else { //Hide the point visualizer, no longer showing point
                    movePointVisualizer(false, 1, 1, 0);
                    isShowingPoint = false;
                    selectedFunction = -1;
                }
			}
		});
		
		grid.addMouseWheelListener(new MouseAdapter() {
			public void mouseWheelMoved(MouseWheelEvent e) {
                if(!isShowingPoint) { //If zooming and not showing point
                    double lineDiff = 0;

                    //Finds distance between visible axis lines
                    for(int i = 0; i < 23; i++) {
                        if(xLinePositions[i] != -1 && xLinePositions[i] != 601) {
                            lineDiff = xLinePositions[i + 1] - xLinePositions[i];
                            i = 23;
                        }
                    }
                    if(e.getWheelRotation() > 0) { //zoom out
                        deleteLines();
                        zoom(-3);
                        for(int i = 0; i < functionCollection.size(); i++) {
                            graph(i);
                        }
                        createLines();
                        if(lineDiff < 60) { //If lines are too close, update positions
                            deleteLines();
                            updateLines(1);
                            createLines();
                        }
                    }
                    if(e.getWheelRotation() < 0) { //zoom in
                        deleteLines();
                        zoom(+3);
                        for(int i = 0; i < functionCollection.size(); i++) {
                            graph(i);
                        }
                        createLines();
                        if(lineDiff > 100) { //If lines are too far apart, update positions
                            deleteLines();
                            updateLines(-1);
                            createLines();
                        }
                    }
                    deleteLines();
                    fixGraph();
                    createLines();
                    createLabels();
                }
			}
		});
		userinput.close();
	}
    
    //create GUI window
    public static void drawGUI() {
        
        minimumX = -10;
		maximumX = 10;
		minimumY = -10;
		maximumY = 10;

        xLines = new double[24];
		yLines = new double[24];
		xLinePositions = new int[24];
		yLinePositions = new int[24];
		xLines[23] = 0;
		yLines[23] = 0;
		
		shiftX = 0.0;
		shiftY = 0.0;
		
		prevX = null;
		prevY = null;
	    
        pointVisualizerLabels = new JLabel[7];

        isShowingPoint = false;

        frame = new JFrame("Pixel Grid");
        frame.setSize(901, 631);
		drawerPanel = new JLayeredPane();
		drawerPanel.setPreferredSize(new Dimension(901, 631));
		
		xLabels = new JLabel[23];
		yLabels = new JLabel[23];
		for(int i = 0; i < 23; i++) {
			xLabels[i] = new JLabel();
			xLabels[i].setVisible(true);
			xLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
			xLabels[i].setVerticalAlignment(SwingConstants.CENTER);
			xLabels[i].setOpaque(false);
			drawerPanel.add(xLabels[i], Integer.valueOf(1));
			yLabels[i] = new JLabel();
			yLabels[i].setVisible(true);
			yLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);
			yLabels[i].setVerticalAlignment(SwingConstants.BOTTOM);
			yLabels[i].setOpaque(false);
			drawerPanel.add(yLabels[i], Integer.valueOf(1));
		}
        
        for(int i = 0; i < 7; i++) {
            pointVisualizerLabels[i] = new JLabel();
            pointVisualizerLabels[i].setOpaque(true);
            pointVisualizerLabels[i].setVisible(false);
            pointVisualizerLabels[i].setBackground(Color.black);
            if(i != 6) {
                drawerPanel.add(pointVisualizerLabels[i], Integer.valueOf(3));
            } else {
                drawerPanel.add(pointVisualizerLabels[i], Integer.valueOf(4));
                pointVisualizerLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            }
        }
        pointVisualizerLabels[4].setBackground(Color.white);
        pointVisualizerLabels[6].setBackground(Color.white);
        
		grid = new PixelGrid(functionCollection);
		grid.setBounds(0, 0, 601, 601);
		drawerPanel.add(grid, Integer.valueOf(1));
		grid.setVisible(true);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(drawerPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setResizable(false);
        
        indexes = new ArrayList<Index>();

        rowPanels = new ArrayList<JPanel>();
        rowButtons = new ArrayList<JButton>();
        textFields = new ArrayList<JTextField>();
        selectPanels = new ArrayList<JPanel>();
        removePanels = new ArrayList<JPanel>();
        consoleText = new JLabel();
        
        JLabel flatBorder = new JLabel();
        flatBorder.setBounds(0, 602, 601, 1);
        flatBorder.setBackground(Color.gray);
        flatBorder.setOpaque(true);
        drawerPanel.add(flatBorder, Integer.valueOf(4));
        JLabel verticalBorder = new JLabel();
        verticalBorder.setBounds(600, 598, 1, 33);
        verticalBorder.setBackground(Color.gray);
        verticalBorder.setOpaque(true);
        drawerPanel.add(verticalBorder, Integer.valueOf(4));

        consoleText.setBounds(5, 602, 595, 30);
        consoleText.setOpaque(true);
        drawerPanel.add(consoleText, Integer.valueOf(1));
        consoleText.setText(">>>");
    
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setBounds(600, 0, 300, 604);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(10, Integer.MAX_VALUE));
        verticalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Color.GRAY;      // the draggable part
                this.trackColor = Color.LIGHT_GRAY; // background track
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton(); // remove bottom/right button
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton(); // remove top/left button
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0)); // no size
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
        drawerPanel.add(scrollPane, Integer.valueOf(3));

        addRow(0);
    }
    
    //adds a row to the scrollPane with a select function button, a remove function button, and a text field to enter a function + decoration
    public static void addRow(int index) {
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        Dimension rowSize = new Dimension(285, 61);
        if(index != 0) {
            functionCollection.add("");
        }

        Index thisIndex = new Index(index);
        indexes.add(thisIndex);

        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setPreferredSize(rowSize);
        rowPanel.setMinimumSize(rowSize);
        rowPanel.setMaximumSize(rowSize);
        rowPanels.add(rowPanel);
        
        JLabel separator = new JLabel();
        separator.setOpaque(true);              
        separator.setBackground(Color.GRAY);    
        separator.setPreferredSize(new Dimension(285, 1)); 
        rowPanel.add(separator, BorderLayout.SOUTH);
        
        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new BorderLayout());
        selectPanel.setPreferredSize(new Dimension(25, 60));
        selectPanel.setBackground(Color.white);
        selectPanels.add(selectPanel);
        
        JButton selectFunction = new JButton("");
        rowButtons.add(selectFunction);
        selectFunction.setBorder(BorderFactory.createEmptyBorder());
        selectFunction.setBackground(Color.LIGHT_GRAY);
        selectFunction.setPreferredSize(new Dimension(20, 60));
        selectFunction.addActionListener(e -> {
            System.out.println(thisIndex.getIndex());
            selectFunction.transferFocus();
            grid.requestFocusInWindow();
        });
        selectPanel.add(selectFunction, BorderLayout.WEST);

        JPanel removePanel = new JPanel();
        removePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));
        removePanel.setPreferredSize(new Dimension(20, 60));
        removePanel.setBackground(Color.white);
        removePanels.add(removePanel);
        
        JButton removeFunction = new JButton("X");
        removeFunction.setBorder(new LineBorder(Color.gray, 1, false));
        removeFunction.setBackground(Color.white);
        removeFunction.setForeground(Color.gray);
        removeFunction.setPreferredSize(new Dimension(15, 15));
        removeFunction.addActionListener(e -> {
            if(functionCollection.size() > 1) {
                deleteRow(thisIndex.getIndex());
                removeFunction.transferFocus();
                grid.requestFocusInWindow();
            }
        });

        removePanel.add(removeFunction);

        JTextField textField = new JTextField(30);
        textField.setBorder(null);
        textFields.add(textField);
        textField.addActionListener(e -> {
            String input = textField.getText();
            String func;
            if(!input.equals("")) {
                if(input.indexOf("=") == 1) {
                    input = input.substring(input.indexOf("=") + 1);
                    func = "(0+" + input + ")";
                } else {
                    func = "y";
                }
            } else {
                func = "";
            }
			functionCollection.set(thisIndex.getIndex(), func);
            
            grid.clearPixels();
            grid.updateGrid(functionCollection);
            setUpGraph(true);
            displayErrors();

            textField.transferFocus();
            textField.setCaretPosition(0);
            grid.requestFocusInWindow();
        });

        rowPanel.add(textField, BorderLayout.CENTER);
        rowPanel.add(selectPanel, BorderLayout.WEST);
        rowPanel.add(removePanel, BorderLayout.EAST);

        listPanel.add(rowPanel);
    }
    
    //deletes a row from the scrollPane 
    public static void deleteRow(int index) {
        if(functionCollection.size() > 1) {
            functionCollection.remove(index);
            updateIndexes(index);
            listPanel.remove(rowPanels.get(index));
            listPanel.revalidate();
            listPanel.repaint();

            grid.clearPixels();
            grid.updateGrid(functionCollection);
            setUpGraph(true);
            displayErrors();

            rowPanels.remove(index);
            rowButtons.remove(index);
            textFields.remove(index);
        }
    }
    
    //updates the indexes for each row in the scrollPane when a row is deleted
    public static void updateIndexes(int index) {
         for(int i = index + 1; i < indexes.size(); i++) {
            indexes.get(i).updateIndex();
        }
    }

    //move the pointVisualizer (the circle and lable to show the coordinates of a point)
    public static void movePointVisualizer(boolean isVisible, int x, int y, int functionIndex) {
        for(int i = 0; i < 7; i++) {
             pointVisualizerLabels[i].setVisible(isVisible);
        }
        
        double increment = (maximumX - minimumX)/600;

        if(isVisible) {
            String xPrint = formatNumber(minimumX + increment * x);
            String yPrint = formatNumber(yValues.get(functionIndex)[x]);

            //Prevent showing 1e-14 instead of 0
            if(Math.abs(Double.parseDouble(yPrint)) < 0.0000000001) {
                yPrint = "0";
            }
            if(Math.abs(Double.parseDouble(xPrint)) < 0.0000000001) {
                xPrint = "0";
            }
            //Format value for x and y coordinates so they fit within the label 
            if((xPrint).indexOf("E") != -1) {
                double exponent = Double.parseDouble(xPrint.substring(xPrint.indexOf("E") + 1));
                if(exponent > -3 && exponent < 4) {
                    xPrint = round((minimumX + increment * x), 3) + "";
                }
            }
            if((yPrint).indexOf("E") != -1) {
                double exponent = Double.parseDouble(yPrint.substring(yPrint.indexOf("E") + 1));
                if(exponent > -3 && exponent < 4) {
                    yPrint = round((yValues.get(functionIndex)[x]), 3) + "";
                }
            }
            if(xPrint.length() > 2) {
                if(xPrint.substring(xPrint.length() - 2).equals(".0")) { //If number has an empty fractional part, remove decimal point
			        xPrint = xPrint.substring(0, xPrint.length() - 2);
		        }
            }
            if(yPrint.length() > 2) {
                if(yPrint.substring(yPrint.length() - 2).equals(".0")) { //If number has an empty fractional part, remove decimal point
			        yPrint = yPrint.substring(0, yPrint.length() - 2);
		        } 
            }
            String textPrint = (xPrint + ", " + yPrint);
            int length = 4 * textPrint.length(); 
            pointVisualizerLabels[6].setBounds(x - length, y - 30, length * 2, 20);
            pointVisualizerLabels[6].setText("(" + textPrint + ")");
            pointVisualizerLabels[5].setBounds(x - length - 1, y - 31, length * 2 + 2, 22);
        }
        
        //Move the labels to make the circle 
        pointVisualizerLabels[0].setBounds(x - 1, y - 2, 3, 1);
        pointVisualizerLabels[1].setBounds(x + 2, y - 1, 1, 3);
        pointVisualizerLabels[2].setBounds(x - 1, y + 2, 3, 1);
        pointVisualizerLabels[3].setBounds(x - 2, y - 1, 1, 3);
        pointVisualizerLabels[4].setBounds(x - 1, y - 1, 3, 3);
    }

    //creates and labels all axes lines
	public static void createLabels() {
		for(int i = 0; i < 23; i++) {
			if(xLinePositions[i] > -1 && xLinePositions[i] < 601) { //If x line is within the graph window
				String labelText = formatNumber(xLines[i]);
				xLabels[i].setText(labelText);
				if(yLinePositions[23] > 600) { //If the y axis is above the graph, put the x labels at a set location so they are visible 
					xLabels[i].setBounds(xLinePositions[i] - 25, 4, 50, 20);
					drawRectangle(xLinePositions[i] - 25, 4, 50, 20, new Color(240, 240, 240));
				} else if(yLinePositions[23] < 37) {//If the y axis is below the graph
					xLabels[i].setBounds(xLinePositions[i] - 25, 575, 50, 20);
					drawRectangle(xLinePositions[i] - 25, 575, 50, 20, new Color(240, 240, 240));
				} else { //Put the x labels at the y axis 
					xLabels[i].setBounds(xLinePositions[i] - 25, 611 - yLinePositions[23], 50, 20);
					drawRectangle(xLinePositions[i] - 25, 611 - yLinePositions[23], 50, 20, new Color(240, 240, 240));
				}
				xLabels[i].setVisible(true);
			} else {
				xLabels[i].setVisible(false);
			}
			if(yLinePositions[i] > -1 && yLinePositions[i] < 601) {//If y line is within the graph window 
				String labelText = formatNumber(yLines[i]);
				yLabels[i].setText(labelText);
				if(xLinePositions[23] > 595) { //If the x axis is too far right of the graph, put the y labels at a set location so they are visible 
					yLabels[i].setBounds(545, 581 - yLinePositions[i], 50, 20);
					drawRectangle(545, 581 - yLinePositions[i], 50, 20, new Color(240, 240, 240));
				} else if(xLinePositions[23] < 50) {//If the x axis is too far left of the graph
					yLabels[i].setBounds(0, 581 - yLinePositions[i], 50, 20);
					drawRectangle(0, 581 - yLinePositions[i], 50, 20, new Color(240, 240, 240));
				} else {//Put the y labels at the x axis 
					yLabels[i].setBounds(xLinePositions[23] - 53, 581 - yLinePositions[i], 50, 20);
					drawRectangle(xLinePositions[23] - 53, 581 - yLinePositions[i], 50, 20, new Color(240, 240, 240));
				}
				yLabels[i].setVisible(true);
			} else {
				yLabels[i].setVisible(false);
			}
		}
	}
	
    //rounds a double, leaving a certain amount of decimal points
	public static double round(double num, int amount) {
		double scale = Math.pow(10.0, amount);
		return Math.round(num * scale)/(scale);
	}
	
    //formats the axes label text so it fits within the JLabel 
	public static String formatNumber(double num) {
		String rounded = num + "";
		if(rounded.indexOf("E") != -1) { //If in scientific notation, round decimal component
			double decimal = Double.parseDouble(rounded.substring(0, rounded.indexOf("E")));
			String exponent = rounded.substring(rounded.indexOf("E") + 1);
			return round(decimal, 4 - exponent.length()) + "E" + exponent;
		}
		if(rounded.substring(rounded.length() - 2).equals(".0")) { //If number has an empty fractional part, remove decimal point
			rounded = rounded.substring(0, rounded.length() - 2);
		} 
		if(rounded.length() > 7) { //If number is too big for label, convert to scientific notation 
			int exponent = 0;
			if(Math.abs(num) > 1) {
				while(Math.abs(num) > 10) {
					num /= 10;
					exponent++;
				}
			} else {
				while(Math.abs(num) < 1) {
					num *= 10;
					exponent--;
				}
			}
			return round(num, 3 - (exponent + "").length()) + "E" + exponent;
		}
		return rounded;
	}
	
    //Fixes bugs associated with moving + pan, particularly where axes and labels are not in the right location initially
	public static void fixGraph() {
		double lineDiff = xLines[13] -xLines[12];
		double maxX = xLines[22] - maximumX;
		double minX = xLines[0] - minimumX;
		double maxY = yLines[22] - maximumY;
		double minY = yLines[0] - minimumY;
		
        //Shift all lines so that they are actually in the right location 
		while((maxX < 0 && minX < 0) | (maxX > 0 && minX > 0)) {
			if(maxX < 0 && minX < 0) { //if maxX is negative and minX is negative, shift right
				for(int i = 0; i < 23; i++) {
					xLines[i] += lineDiff;
				}
			} else if(maxX > 0 && minX > 0) { //if maxX is positive and minX is positive, shift left
				for(int i = 0; i < 23; i++) {
					xLines[i] -= lineDiff;
				}
			}
			maxX = xLines[22] - maximumX;
			minX = xLines[0] - minimumX;
		}
		while((maxY < 0 && minY < 0) | (maxY > 0 && minY > 0)) {
			if(maxY < 0 && minY < 0) { //if maxX is negative and minX is negative, shift right
				for(int i = 0; i < 23; i++) {
					yLines[i] += lineDiff;
				}
			} else if(maxY > 0 && minY > 0) { //if maxX is positive and minX is positive, shit left
				for(int i = 0; i < 23; i++) {
					yLines[i] -= lineDiff;
				}
			}
			maxY = yLines[22] - maximumY;
			minY = yLines[0] - minimumY;
		}
	}
	
    //creates the axes lines and puts them in the right location in the window
	public static void createLines() {
		double increment = (maximumY - minimumY)/600;
		double firstX = minimumX - increment * 6;
		double firstY = minimumY - increment * 6;
		for(int i = 0; i < 24; i++) {
			xLinePositions[i] = -1;
			yLinePositions[i] = -1;
			double xDiff = Math.abs(xLines[i] - firstX);
			double yDiff = Math.abs(yLines[i] - firstY);

            //Find the right pixel to represent the location of the line 
			for(int index = 0;  index < 602; index++) {
				double xVal = minimumX + increment * index;
				double yVal = minimumY + increment * index;
				double initialXDiff = Math.abs(xLines[i] - xVal);
				double initialYDiff = Math.abs(yLines[i] - yVal);
				if(initialXDiff < xDiff) {
					xDiff = initialXDiff;
					xLinePositions[i] = index;
				}
				if(initialYDiff < yDiff) {
					yDiff = initialYDiff;
					yLinePositions[i] = index;
				}
			}
			if(i != 23) { //If not axis, draw thin gray line
				drawRectangle(xLinePositions[i], 0, 1, 601, Color.gray);
				drawRectangle(0, 601 - yLinePositions[i], 601, 1, Color.gray);
			} else { //Draw thick black line to represent axis 
				if(yLinePositions[i] < 601) {
					drawRectangle(0, 601 - yLinePositions[i], 601, 3, Color.black);
				}
				drawRectangle(xLinePositions[i] - 1, 0, 3, 601, Color.black);
			}
		}
	}
	
    //removes the axes lines that are not needed
	public static void deleteLines() {
		for(int i = 0; i < 23; i++) {
            //Delete the lines 
			clearRectangle(xLinePositions[i], 0, 1, 601, Color.white);
			clearRectangle(0, 601 - yLinePositions[i], 601, 1, Color.white);

            //Delete the boxes for the labels
			if(yLinePositions[23] > 600) {
				clearRectangle(xLinePositions[i] - 25, 4, 50, 20, new Color(240, 240, 240));
			} else if(yLinePositions[23] < 37) {
				clearRectangle(xLinePositions[i] - 25, 575, 50, 20, new Color(240, 240, 240));
			} else { 
				clearRectangle(xLinePositions[i] - 25, 611 - yLinePositions[23], 50, 20, new Color(240, 240, 240));
			}
			if(xLinePositions[23] > 595) {
				clearRectangle(545, 581 - yLinePositions[i], 50, 20, new Color(240, 240, 240));
			} else if(xLinePositions[23] < 50) {
				clearRectangle(0, 581 - yLinePositions[i], 50, 20, new Color(240, 240, 240));
			} else {
				clearRectangle(xLinePositions[23] - 53, 581 - yLinePositions[i], 50, 20, new Color(240, 240, 240));
			}
		}
        //Delete the axis lines 
		clearRectangle(xLinePositions[23] - 1, 0, 3, 601, Color.white);
		clearRectangle(0, 601 - yLinePositions[23], 601, 3, Color.white);
	}
	
    //updates the line positions when needed 
	public static void updateLines(int changeBoundaries) {
		double increment = (maximumX - minimumX)/10;
		double newMinX = minimumX - increment;
		double newMinY = minimumY - increment;
		if(changeBoundaries == 0) { //If simply panning
			for(int i = 0; i < 23; i++) {
				xLines[i] = newMinX + increment * i;
				yLines[i] = newMinY + increment * i;
			}
		} else {
			if(changeBoundaries == 1) { //If zooming out 
				for(int i = 0; i < 23; i++) {
					xLines[i] *= 2;
					yLines[i] *= 2;
				}
				shiftX *= 2;
				shiftY *= 2;
			} else { //If zooming in 
				for(int i = 0; i < 23; i++) {
					xLines[i] /= 2;
					yLines[i] /= 2;
				}
				shiftX /= 2;
				shiftY /= 2;
			}
		}
	}
	
    //draws a rectangle one level below the graph, meant for the axes lines 
	public static void drawRectangle(int x, int y, int width, int height, Color color) {
		for(int xinc = x; xinc < x + width; xinc++) {
			for(int yinc = y; yinc < y + height; yinc++) {
				if(xinc > 0 && xinc < 601 && yinc > 0 && yinc < 601) {
					grid.setPixel(xinc, yinc, color, true);
				}
			}
		}
	}
	
    //erases a rectangle one level below the graph, meant for the axes lines
	public static void clearRectangle(int x, int y, int width, int height, Color color) {
		for(int xinc = x; xinc < x + width; xinc++) {
			for(int yinc = y; yinc < y + height; yinc++) {
				if(xinc > 0 && xinc < 601 && yinc > 0 && yinc < 601) {
					grid.setPixel(xinc, yinc, color, false);
				}
			}
		}
	}
	
    //used to set up panning up and down, adding a shift amount to range.
	public static void displaceYValues(int shiftAmount) {
		double increment = (maximumY - minimumY)/600;
        //Shift y range by the shift amount 
		if(shiftAmount > 0) {
			while(shiftAmount > 0) {
				shiftY += -increment;
				maximumY += increment;
				minimumY += increment;
				shiftAmount--;
			}
		}
		if(shiftAmount < 0) {
			while(shiftAmount < 0) {
				shiftY += increment;
				maximumY -= increment;
				minimumY -= increment;
				shiftAmount++;
			}
		}
	}
	
    //used to optimize panning sideways, moving all points to the left or right instead of recalculating everything
	public static void shiftYValues(int shiftAmount) {
		double increment = (maximumX - minimumX)/600.0;
		if(shiftAmount < 0) { //move function left
			while(shiftAmount < 0) {
				shiftX += -increment;
				parseIndex.clear();
				for(int functionIndex = 0; functionIndex < functionCollection.size(); functionIndex++) {
                    boolean isValid = true;
                    if(yValues.get(functionIndex).length == 0) {
                        isValid = false;
                    }
                    if(!functionCollection.get(functionIndex).equals("") && isValid) { //Only shift values if the function is not empty (causes exception if included)
                        for(int i = 1; i < yValues.get(0).length; i++) {
                            yValues.get(functionIndex)[i - 1] = yValues.get(functionIndex)[i];
                        }
                        parseIndex.add(0);
                        //Evaluate the edge value 
                        ArrayList<Object> formula = ParseFunction(functionCollection.get(functionIndex), functionIndex);
                        Function tempfunction = new Function(formula);
                        yValues.get(functionIndex)[yValues.get(functionIndex).length - 1] = tempfunction.evaluate(maximumX + increment, new ArrayList<Object>(formula), 0);
                    } else {
                        parseIndex.add(0); //Fixes bug where empty functions would throw an exception if the graph was panned because the parseIndex size did not match functionCollection size
                    }
				}
				minimumX += increment;
				maximumX += increment;
				shiftAmount++;
			}
		}
		if(shiftAmount > 0) { //move function right
			while(shiftAmount > 0) {
				shiftX += increment;
				parseIndex.clear();
				for(int functionIndex = 0; functionIndex < functionCollection.size(); functionIndex++) {
                    boolean isValid = true;
                    if(yValues.get(functionIndex).length == 0) {
                        isValid = false;
                    }
                    if(!functionCollection.get(functionIndex).equals("") && isValid) { //Only shift values if the function is not empty (causes exception if included)
                        for(int i = yValues.get(0).length - 1; i > 0; i--) {
                            yValues.get(functionIndex)[i] = yValues.get(functionIndex)[i - 1];
                        }
                        parseIndex.add(0);
                        //Evaluate the edge value 
                        ArrayList<Object> formula = ParseFunction(functionCollection.get(functionIndex), functionIndex);
                        Function tempfunction = new Function(formula);
                        yValues.get(functionIndex)[0] = tempfunction.evaluate(minimumX - increment, new ArrayList<Object>(formula), 0);
                    } else {
                        parseIndex.add(0); //Fixes bug where empty functions would throw an exception if the graph was panned because the parseIndex size did not match functionCollection size
                    }
				}
				minimumX -= increment;
				maximumX -= increment;
				shiftAmount--;
			}
		}
	}
	
    //check if a pixel's y coordinate is within a determined range of a function
	public static boolean isWithinPoint(double yVal, double yPoint, double increment) {
		return Math.abs(yPoint - yVal) <= increment;
	}
	
    public static void updateConsole(String text) {
        consoleText.setText(">>> " + text);
    }

    /*Sets up all variables to parse/graph all functions 
     *
     *  if showProgress is true, it will call the overloaded Function.findYValues 
     *      this will create a new thread for each evaluate call, and each thread will update the console as a function has been evaluated at a specific x value 
     *      once each thread is complete, it will graph by calling graph() and draw the labels/lines with createLabel() and createLines()
     *  
     *  if showProgress is false, it will compute each function's y values by calling the original Function.findYValues and will not graph afterwards
     */
	public static void setUpGraph(boolean showProgress) {
        grid.clearPixels();
        grid.clearPixels();
		parseIndex.clear();
		yValuePositions.clear();
		yPointPositions.clear();
		yValues.clear();
        yPairs.clear();
        xPairs.clear();
        allXPoints.clear();
        allYPoints.clear();
        allCoordinates.clear();
		initialX.clear();
		finalX.clear();

        ArrayList<CompletableFuture<double[]>> valuesList = new ArrayList<>();
        AtomicInteger globalProgress = new AtomicInteger(0);
        int totalWork = 601 * functionCollection.size();

		for(int functionIndex = 0; functionIndex < functionCollection.size(); functionIndex++) {
            yPairs.add(new ArrayList<Integer[]>());
            xPairs.add(new ArrayList<Integer[]>());
            allYPoints.add(new ArrayList<Integer>());
            allXPoints.add(new ArrayList<Integer>());
            allCoordinates.add(new HashSet<ArrayList<Integer>>());
			parseIndex.add(0);
			yValuePositions.add(new String[601]);
			yPointPositions.add(new int[601]);
            
            try {
			    ArrayList<Object> formula = ParseFunction(functionCollection.get(functionIndex), functionIndex);
			    Function input = new Function(formula);
                highlightRow(functionIndex, Color.white);
                if(showProgress) { //Create a new thread which will update the consoleText with the globalProgress once a function has been evaluated at an x 
                    CompletableFuture<double[]> futureValues = input.findYValues(minimumX, maximumX, consoleText, showProgress, globalProgress, totalWork, indexes.get(functionIndex));
                    valuesList.add(futureValues);
                } else { //Simply evaluate but do not create a seperate thread 
                    yValues.add(input.findYValues(minimumX, maximumX));
                }
            } catch (IllegalArgumentException e) {
                highlightRow(functionIndex, new Color(255, 170, 170));
                String error = e + "";
                error = error.substring(error.indexOf(":") + 2);
                indexes.get(functionIndex).updateError(error);
            }

            //Calculate all y values for the function's range
            initialX.add(minimumX);
			finalX.add(maximumX);
		}
        if(showProgress) {
            createLines();
            createLabels();
            CompletableFuture<Void> allDone = CompletableFuture.allOf(valuesList.toArray(new CompletableFuture[0]));
            allDone.thenRun(() -> { //Once all threads are done and all yValues have been computed, graph the functions and create the labels/lines
                for(CompletableFuture<double[]> c : valuesList) {
                    yValues.add(c.join());
                }
                for(int i = 0; i < functionCollection.size(); i++) {
                    if((yValues.get(i)[0] + "").equals("NaN")) {
                        highlightRow(i, new Color(255, 170, 170));
                    } else {
                        highlightRow(i, Color.white);
                    }
                    graph(i);
                }
                deleteLines();
                fixGraph();
                createLines();
                createLabels();
            });
        } 
	}
	
    public static void displayErrors() {
        for(int i = 0; i < indexes.size(); i++) {
            if(!indexes.get(i).getError().equals("")) {
                updateConsole(i + ": " + indexes.get(i).getError());

            }
        }
    }

    //changes the range variables depending on zooming in(+1) or zooming out(-1)
	public static void zoom(double sign) {
		double increment = sign * (maximumX - minimumX)/100;
		minimumX += increment;
		maximumX -= increment;
		minimumY += increment;
		maximumY -= increment;
		setUpGraph(false);
	}
    
    //returns the yValue for the pixel at a given yValue of a function 
    public static int getPoint(double increment, double yPosition, double yMin) {
        return (int)((yPosition - yMin)/increment);
    }

    //sets up the point pairs, used for line drawing later when anti-aliasing. 
	public static void graph(int functionIndex) {
		double range = finalX.get(functionIndex) - initialX.get(functionIndex);
		double increment = (range/600);
		double precision = increment/2;
		double yVal = minimumY;
        double initialY = minimumY;
		double outOfBoundsHigh = maximumY - precision;
		double outOfBoundsLow = minimumY + precision;

		for(int x = 0; x < 601; x++) {
            int pointer = yPairs.get(functionIndex).size() - 1;
			yValuePositions.get(functionIndex)[x] = "OUTSIDE";
			yPointPositions.get(functionIndex)[x] = 0;
            double yPosition = yValues.get(functionIndex)[x];

			if(yPosition <= outOfBoundsHigh && yPosition >= outOfBoundsLow) { //If y is within Graph GUI 
                int yPoint = getPoint(increment, yPosition, initialY);
                if(yPairs.get(functionIndex).size() == 0) { 
                    yPairs.get(functionIndex).add(new Integer[2]);
                    yPairs.get(functionIndex).get(0)[0] = yPoint;
                    xPairs.get(functionIndex).add(new Integer[2]);
                    xPairs.get(functionIndex).get(0)[0] = x;
                } else if(yPairs.get(functionIndex).get(pointer)[0] == null) {
                    yPairs.get(functionIndex).get(pointer)[0] = yPoint;
                    xPairs.get(functionIndex).get(pointer)[0] = x;
                } else {
                    Integer[] tempArr = yPairs.get(functionIndex).get(pointer);
                    if(tempArr[0] != null) {
                        if(tempArr[0] != yPoint) { //Only add point if it is defined and is not over another point  
                            yPairs.get(functionIndex).get(pointer)[1] = yPoint;
                            xPairs.get(functionIndex).get(pointer)[1] = x;
                            yPairs.get(functionIndex).add(new Integer[2]);
                            xPairs.get(functionIndex).add(new Integer[2]);
                            yPairs.get(functionIndex).get(pointer + 1)[0] = yPoint;
                            xPairs.get(functionIndex).get(pointer + 1)[0] = x;
                        }
                    }
                }
				yValuePositions.get(functionIndex)[x] = "INSIDE";
                yPointPositions.get(functionIndex)[x] = yPoint;
			} else if(!(yPosition + "").equals("NaN") && ((int)yPosition != Integer.MAX_VALUE && (int)yPosition != Integer.MIN_VALUE)) { //If outside of bounds but still defined 
                int yPoint = getPoint(increment, yPosition, initialY);
                if(yPairs.get(functionIndex).size() == 0) {
                    yPairs.get(functionIndex).add(new Integer[2]);
                    xPairs.get(functionIndex).add(new Integer[2]);
                    yPairs.get(functionIndex).get(0)[0] = yPoint; 
                    xPairs.get(functionIndex).get(0)[0] = x;
                } else {
                    Integer[] tempArr = yPairs.get(functionIndex).get(pointer);
                    if(tempArr[0] != null) {
                        if(tempArr[0] < 601 && tempArr[0] > -1) { //If first point is within bounds, create point pair
                            yPairs.get(functionIndex).get(pointer)[1] = yPoint;
                            xPairs.get(functionIndex).get(pointer)[1] = x;
                            yPairs.get(functionIndex).add(new Integer[2]);
                            xPairs.get(functionIndex).add(new Integer[2]);
                            yPairs.get(functionIndex).get(pointer + 1)[0] = yPoint;
                            xPairs.get(functionIndex).get(pointer + 1)[0] = x;
                        } else if((tempArr[0] < 0 && yPoint < 0) | (tempArr[0] > 600 && yPoint > 600) | Math.abs(tempArr[0] - yPoint) > 610) { //If distance between points is too great or both are outside of bounds, don't add to point pair 
                            yPairs.get(functionIndex).get(pointer)[0] = yPoint;
                            xPairs.get(functionIndex).get(pointer)[0] = x;
                        } 
                    } else { 
                        yPairs.get(functionIndex).get(pointer)[0] = yPoint;
                        xPairs.get(functionIndex).get(pointer)[0] = x;
                    }
                }
            } else if((yPosition + "").equals("NaN") | ((int)yPosition == Integer.MAX_VALUE | (int)yPosition == Integer.MIN_VALUE)) { //If point is undefined
                if(yPairs.get(functionIndex).size() != 0 && x != 0) {
                    if(yPairs.get(functionIndex).get(pointer)[0] != null) {
                        double yPrevious = yValues.get(functionIndex)[x - 1];
                        if(xPairs.get(functionIndex).get(pointer)[0] == x - 1) { //Remove last point from point pair 
                            yPairs.get(functionIndex).remove(pointer);
                            xPairs.get(functionIndex).remove(pointer);
                        } else { 
                            yPairs.get(functionIndex).get(pointer)[1] = getPoint(increment, yPrevious, initialY);
                            xPairs.get(functionIndex).get(pointer)[1] = x - 1;
                        }
                        yPairs.get(functionIndex).add(new Integer[2]);
                        xPairs.get(functionIndex).add(new Integer[2]);
                    }
                }
            }
		}
        //Handle edge cases where the last or first point pairs are incomplete  
        if(yPairs.get(functionIndex).size() > 0 && yPairs.get(functionIndex).get(yPairs.get(functionIndex).size() - 1)[1] == null && yPairs.get(functionIndex).get(yPairs.get(functionIndex).size() - 1)[0] != null) { //If the last point is null but the first point in the pair is defined 
            if(yPairs.get(functionIndex).get(yPairs.get(functionIndex).size() - 1)[0] < 601 && yPairs.get(functionIndex).get(yPairs.get(functionIndex).size() - 1)[0] > -1) { //If the first point is within the graph GUI, set the last point in the pair equal to the first's y value. 
                yPairs.get(functionIndex).get(yPairs.get(functionIndex).size() - 1)[1] = yPairs.get(functionIndex).get(yPairs.get(functionIndex).size() - 1)[0];
                xPairs.get(functionIndex).get(yPairs.get(functionIndex).size() - 1)[1] = 599;
            } else { //If not within the graph GUI, remove last pair 
                yPairs.get(functionIndex).remove(yPairs.get(functionIndex).size() - 1);
                xPairs.get(functionIndex).remove(xPairs.get(functionIndex).size() - 1);
            }
        } else if(yPairs.get(functionIndex).size() > 0 && yPairs.get(functionIndex).get(yPairs.get(functionIndex).size() - 1)[1] == null && yPairs.get(functionIndex).get(yPairs.get(functionIndex).size() - 1)[0] == null) { //If the last pair is completely empty, just remove it 
            yPairs.get(functionIndex).remove(yPairs.get(functionIndex).size() - 1);
            xPairs.get(functionIndex).remove(xPairs.get(functionIndex).size() - 1);
        }

        //Create the lines between the point pairs 
        fillLines(range, increment, functionIndex, precision);
	}
    
    //draws the lines between points (contained within the point pairs) and antialiases these lines 
    public static void fillLines(double range, double increment, int functionIndex, double precision) {
        for(int i = 0; i < yPairs.get(functionIndex).size(); i++) {

            //Coordinates for end points of line 
            double x1 = xPairs.get(functionIndex).get(i)[0];
            double x2 = xPairs.get(functionIndex).get(i)[1];
            double y1 = yPairs.get(functionIndex).get(i)[0];
            double y2 = yPairs.get(functionIndex).get(i)[1];
            
            //Plot end points 
            CreatePixel((int)x1, (int)y1, functionIndex, 1);
            CreatePixel((int)x2, (int)y2, functionIndex, 1);

            //Slope of line between end points 
            double middleSlope = (y2 - y1)/(x2 - x1);
            if(middleSlope < 1 && middleSlope > -1) {
                CreatePixel((int)x1, (int)y1 - 1, functionIndex, 1);
                for(int x = (int)x1; x <= (int)x2; x++) {
                    double point = middleSlope * (x - x1) + y1;

                    //Antialiasing, Wu's line algorithm where opacity is proportional to distance from line 
                    CreatePixel(x, (int)point + 1, functionIndex, Math.abs(1 - (((int)point + 1) - point)));

                    //Draw opaque pixel between top and bottom anti-aliased lines (used to increase thickness)
                    CreatePixel(x, (int)point, functionIndex, 1);

                    if(x != (int)x2) { //Add to all* arrays, used later for point visualization 
                        allXPoints.get(functionIndex).add(x);
                        allYPoints.get(functionIndex).add((int)point);
                        ArrayList<Integer> coordinate = new ArrayList<Integer>();
                        coordinate.add(x);
                        coordinate.add((int)point);
                        allCoordinates.get(functionIndex).add(coordinate);
                    }
                    point--;

                    //Antialiasing, Wu's line algorithm
                    CreatePixel(x, (int)point, functionIndex, Math.abs(1 - ((point - (int)point))));
                }
            } else if(Math.abs(middleSlope) == 1) { //Edge case that needs to be handled seperately, manual antialiasing 
                allXPoints.get(functionIndex).add((int)x1);
                allYPoints.get(functionIndex).add((int)y1);
                ArrayList<Integer> coordinate = new ArrayList<Integer>();
                coordinate.add((int)x1);
                coordinate.add((int)y1);
                allCoordinates.get(functionIndex).add(coordinate);

                //Manual antialiasing
                CreatePixel((int)x1, (int)y1 + 1, functionIndex, 0.5);
                CreatePixel((int)x1, (int)y1 - 1, functionIndex, 0.5);
                CreatePixel((int)x2, (int)y2 + 1, functionIndex, 0.5);
                CreatePixel((int)x2, (int)y2 - 1, functionIndex, 0.5);
            } else {
                CreatePixel((int)x1 + 1, (int)y1, functionIndex, 1);
                middleSlope = 1/middleSlope;
                int counter = 0; 
                if(middleSlope > 0) {
                    for(int y = Math.max((int)y1, 0); y <= Math.min(601, (int)y2); y++) {
                        double point = x1 + middleSlope * counter;
                        //Antialiasing, Wu's line algorithm
                        CreatePixel((int)point + 1, y, functionIndex, Math.abs(1 - (((int)point + 1) - point)));

                        //Draw opaque pixel between antialiased lines, used to increase thickness 
                        CreatePixel((int)point, y, functionIndex, 1);
                        if(y != Math.min(601, (int)y2)) { //Add to all* arrays, used later for point visualization 
                            allXPoints.get(functionIndex).add((int)point);
                            allYPoints.get(functionIndex).add(y);
                            ArrayList<Integer> coordinate = new ArrayList<Integer>();
                            coordinate.add((int)point);
                            coordinate.add(y);
                            allCoordinates.get(functionIndex).add(coordinate);
                        }
                        point--;
                        
                        //Antialiasing, Wu's line algorithm 
                        CreatePixel((int)point, y, functionIndex, Math.abs(1 - ((point - (int)point))));

                        counter++;
                    }
                } else {
                    for(int y = Math.max((int)y2, 0); y <= Math.min((int)y1, 601); y++) {
                        double point = x2 + middleSlope * counter;
                        //Antialiasing, Wu's line algorithm
                        CreatePixel((int)point + 1, y, functionIndex, Math.abs(1 - (((int)point + 1) - point)));

                        //Draw opaque pixel 
                        CreatePixel((int)point, y, functionIndex, 1);
                        if(y != Math.max((int)y2, 0)) {
                            allXPoints.get(functionIndex).add((int)point);
                            allYPoints.get(functionIndex).add(y);
                            ArrayList<Integer> coordinate = new ArrayList<Integer>();
                            coordinate.add((int)point);
                            coordinate.add(y);
                            allCoordinates.get(functionIndex).add(coordinate);
                        }
                        point--;

                        //Antialiasing, Wu's line algorithm 
                        CreatePixel((int)point, y, functionIndex, Math.abs(1 - ((point - (int)point))));

                        counter++;
                    }
                }
            }
        }
    }
    
    public static void highlightRow(int functionIndex, Color color) {
        selectPanels.get(functionIndex).setBackground(color);
        rowPanels.get(functionIndex).setBackground(color);
        textFields.get(functionIndex).setBackground(color);
        removePanels.get(functionIndex).setBackground(color);
    }

    //Creates a pixel at the default level for the graph, used only for the function's pixels 
    public static void CreatePixel(int x, int y, int functionIndex, double intensity) {
        Color tempColor = functionColor[functionIndex % 7];
        int r = tempColor.getRed();
        int g = tempColor.getGreen();
        int b = tempColor.getBlue();
        if(intensity == 1) {
		    grid.setPoint(x, 601 - y, tempColor, true, functionIndex);
        } else {
            //Prevent opacity outside of bounds of 0 and 1 
            if(intensity > 1) { 
                intensity = 1;
            } else if(intensity < 0) {
                intensity = 0;
            }
            grid.setPoint(x, 601 - y, new Color(r, g, b, (int)(intensity * 255)), true, functionIndex);
        }
	}
	
    /* 
     * Parses the text-inputted function into an arrayList of operations and numbers, used later to compute values in the Function class 
     * 
     * Can only parse in standard format, i.e. 2+2 or 2*sin(x). That is, the number is followed by an operation and then another number.
     *
     * Uses a state machine to parse, where: 
     *  ST = At first character, has no previous state 
     *  NUM = Creating an Integer
     *  DEC = Adding a decimal part to the integer 
     *  IDLE = Done with creating the number (Default state)
     *  FUNC = A function has been declared, expecting a "(" afterwards as an argument
     *  OP = An operator has been declared, expecting a function or value afterwards
     *
     */
	public static ArrayList<Object> ParseFunction(String function, int functionIndex) {
		ArrayList<Object> operation = new ArrayList<Object>();
		String state = "ST";
		int pointer = 1;
		double num = 0;
		double dec = 0;
		int decimalcounter = 0;
		while(pointer < function.length()) {
            //System.out.print(pointer + ": ");
			char tempval = function.charAt(pointer);
            //System.out.println(tempval);
            if(state.equals("FUNC")) {
                if(tempval != '(') {
                    throw new IllegalArgumentException("A function must be followed by parentheses");
                }
            }
			if (tempval == '+' | tempval == '-' | tempval == '*' | tempval == '/' | tempval == '^') { //If the character is a basic operation
                if(state.equals("OP")) {
                    throw new IllegalArgumentException("Operator after another operator");
                }
				if(state.equals("DEC")) { //Add decimal component to number 
					num += dec/(Math.pow(10, decimalcounter));
					state = "NUM";
					decimalcounter = 0;
					dec = 0;
				}
	            if(state.equals("NUM")) { //Add number to array list 
					operation.add(num);
					num = 0;
				}  
				operation.add("" + tempval); //Once done with previous number, add operation 
				state = "OP";
			} else if(tempval == '.') { //Enter decimal state if there is a decimal point after the number
                if(!state.equals("NUM")) {
                    throw new IllegalArgumentException("Decimal point without an integer before it");
                }
				state = "DEC";
			} else if(tempval == 's' | tempval == 'c' | tempval == 't' | 
                      tempval == 'a' | tempval == 'd' | (tempval == 'p' && function.charAt(pointer + 1) == 'r') | 
                      tempval == 'f' | tempval == 'i') { //All functions like sin, tan, der, int, pro
                if(!(state.equals("OP") | state.equals("FUNC") | state.equals("ST"))) {
                    throw new IllegalArgumentException("Function must follow an operator or other function");
                }
				if(tempval != 'a' | function.charAt(pointer + 1) == 'b') {  //For all functions except arcsin,...,arccot. Includes abs as an edge case
					operation.add("" + tempval + function.charAt(pointer + 1) + function.charAt(pointer + 2));
					pointer += 2;
                } else { //If function has a length of six, such as arcsin, arctan, arcsec, etc.
					String tempadder = "";
					for(int i = 1; i < 6; i++) {
						tempadder += function.charAt(pointer + i);
					}
					operation.add("" + tempval + tempadder);
					pointer += 5;
				}
				state = "FUNC";
			} else if(tempval == 'l') { //If ln or log
                if(!(state.equals("ST") | state.equals("OP"))) {
                    throw new IllegalArgumentException("Function is being declared but not following an operator");
                }
                if(function.charAt(pointer + 1) == 'n') { //If ln
                    operation.add("" + tempval + function.charAt(pointer + 1));
                    pointer++;
                } else if(function.charAt(pointer + 1) == 'o' && function.charAt(pointer + 2) == 'g') { //If log
                    operation.add("" + tempval + function.charAt(pointer + 1) + function.charAt(pointer + 2));
                    pointer += 2;
                } else {
                    throw new IllegalArgumentException("Invalid function");
                }
                state = "FUNC";
            } else if (tempval == ')') { //If at end of expression, add remaining number if it exists to the ArrayList
				if(state.equals("DEC")) {
					num += dec/(Math.pow(10, decimalcounter));
					state = "NUM";
					dec = 0;
					decimalcounter = 0;
				}
				if(state.equals("NUM")) {
					operation.add(num);
					num = 0;
				}
                //Move pointer beyond the ) to prevent an infinite loop
				parseIndex.set(functionIndex, parseIndex.get(functionIndex) + pointer);
				return operation;
			} else if (tempval == '(') { //Parse the expression within the parentheses and add result to ArrayList
                if(!state.equals("ST")) {
                    if(!(function.charAt(pointer - 1) == ')' | state.equals("FUNC") | state.equals("OP"))) {
                        throw new IllegalArgumentException("Parentheses must be after a function or operator");
                    }
                }
				operation.add(ParseFunction(function.substring(pointer), functionIndex));
                //Move pointer beyond the last ) so that it moves to the next expression
				pointer += parseIndex.get(functionIndex);
				parseIndex.set(functionIndex, 0);
                state = "IDLE";
			} else if (tempval > 47 && tempval < 58) { //Only add to number/decimal if the character is a number
                //System.out.println(state);
				if(state.equals("DEC")) {
					dec = dec * 10 + Double.parseDouble("" + tempval);
					decimalcounter++;
				} else {
					num = num * 10 + Integer.parseInt("" + tempval);
					state = "NUM";
				}
			} else if (tempval == 'x' | tempval == 'e' | tempval == 'n') { //If variable/constant with length 1
                if(!(state.equals("OP") | state.equals("ST"))) {
                    throw new IllegalArgumentException("Constant after a number without an operator inbetween");
                }
				operation.add(tempval);
				state = "IDLE";
			} else if (tempval == 'p' && function.charAt(pointer + 1) == 'i') { //If constant with length 2
				operation.add("" + tempval + function.charAt(pointer + 1));
				pointer++;
				state = "IDLE";
			} else {
                throw new IllegalArgumentException(tempval + " is not a valid input");
            }
			pointer++;
		}
		return operation;
	}

	
}
