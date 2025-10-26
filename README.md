**Java Graphing Calculator**

An interactive graphing calculator built in Java that combines console-based function input with a fully interactive GUI. This project is designed for advanced mathematical calculations and visualization. 

![ezgif-7ba98c2faab6c8](https://github.com/user-attachments/assets/c8b50ff8-4756-4dd1-b91c-27db69fc676c)

**Features**

- Uses Swing to handle function input from the GUI. Includes error handling and highlights functions which are improperly written.
- **Custom Math Engine**: Handles advanced operations including:
  - Integrals
  - Derivatives
  - Products
  - Sums
  - Factorials (Computed with the Gamma function).
- Custom Function Parser: Parses explicit functions for computation.  
- **Interactive GUI:**
  - Graphing with multiple functions, combined with antialiasing for a smooth look.
  - Pan and zoom to explore the graph in detail.
  - Labeled grid lines to keep track of points/location of the graph.
- **Multithreading**
  - Uses multithreading to compute the values for each function, increasing efficiency.

 **Usage**
 - Run the program with an IDE or from the command line.
 - Enter your function in the panel to the right, e.g:
    y=2*(5+x+sin(tan(x)))       This will output the following: <img width="1194" height="1196" alt="20251014_18h43m12s_grim" src="https://github.com/user-attachments/assets/35c33a21-6d53-4f83-8cb9-9a99b5199278" />

- Use your mouse to move the graph around with left click and pan. 
- Use your mousewheel to zoom in/out, or press i to zoom in and o to zoom out.
- Close the window to end the program.
- Press ENTER to add a new function
- Press the "X" button to the right of each function to delete it, or press backspace to delete the last function.
