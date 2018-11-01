//Author: Scott Spencer (wsspence)
//NOTE:  Prints to output file (per instructions), NOT console

import java.io.*;
import java.util.*;

//Program to build a representation of a binary tree data structure and answer queries based on 
//that tree given its preorder and postorder traversal
public class proj2 {
	//Nested class representation of a single tree node (or leaf/root)
	public static class Node {
		//Node properties
		char data;
		boolean mark;
		Node parent;
		Node next;  //only used for queue stuff
		int level;  //used to keep track of level (distance from root) 
		//Need a list for children because this is not a binary tree, we can have any number of 
		//branches from on ancestor to their heirs (as long as it doesn't exceed our memory limit)
		List<Node> children = new ArrayList<Node>();
		
		//Node without parent constructor
		public Node(char d) {
			data = d;
			mark = false;
			level = 0; //only used for parent
			//setting parent to self for easy checking of root node
			//(addChild is used for all other nodes in data tree)
			this.parent = this;
		}
		
		//method for adding child to this instance given that child's data
		public void addChild(char data) {
			Node child = new Node(data);
			//child's level (distance from root) should be that of the parent (this) + 1
			child.level = this.level + 1;
			child.parent = this;
			//add child to this instance's list of child nodes
			this.children.add(child);
		}
		
	}
	
	//Nested class representation of a single node for a queue data structure (used for printing tree)
	public static class Queue {
		//queueNode properties
		Node front;
		Node back;
		int size;
		
		//Using normal Nodes here so we can make references to the same item occupying two data 
		//structures (Queues and Trees), and enqueues Nodes instead of chars to simplify the rest
		//of the program (Queue is only used once tree is build so no use creating new Nodes for
		//data that already exists).
		//enqueue method 
		public void enqueue(Node newEntry) {
			Node prevBack = back;
			back = newEntry;
			if (this.isEmpty()) {
				front = back;
			}
			else {
				prevBack.next = back;
			}
			size++;
		}
		
		//dequeue method (returns node that is dequeued)
		public Node dequeue() {
			Node value = front;
			front = front.next;
			size--;
			return value;
		}
		
		
		//isEmpty method to check if queue is empty
		public boolean isEmpty() {
			if (this.size == 0) {
				return true;
			}
			else {
				return false;
			}
		}
		
		
		//constructor for queue with no data (dummy head)
		public Queue() {
			front = null;
			back = null;
			size = 0;
		}
	}
	
	
	//class variables
	static Node root; //root of data structure
	static Node currentParent; //current subparent
	static Queue q; //our queue structure used when printing data structure out
	static char[] pretrav = new char[256]; //preorder tree traversal (256 is char limit)
	static char[] posttrav = new char[256]; //postorder tree traversal (256 is char limit)
	static String currentTree = ""; //string for keeping track of currentTree found in tree
	static PrintWriter output; //what we will use to write to our output file
	
	//Main method, prompt user for files and queries
	//(We assume input is correct and do not need to handle errors that are the fault of the user,
	// We also assume no repeated data in leafs, as to make our position more easily notable. 
	// Since there are only 256 different characters, that will be our bound for now)
	public static void main(String[] args) {
		//prompt user for input/output files
		System.out.println("Please enter the filename (in working directory with file extension) of your input here: ");
		Scanner input = new Scanner(System.in);
		String filename = input.nextLine();
		System.out.println("Please enter the filename (to be saved in working directory) of your output file here: ");
		String outFilename = input.nextLine();
		
		
		File inputFile = new File(filename);
		try {
			output = new PrintWriter(new FileWriter(outFilename));
		} catch (IOException e) {
			System.out.println("IO error");
		}
		//change scanner to the file user wants to use
		try {
			input = new Scanner(inputFile);
		} catch(FileNotFoundException e) {
			System.out.println("Invalid file.  Please try again");
			System.exit(1);
		}

		//Read file using file scanner. First 2 lines of input into pretrav and posttrav	
		String lineOne = input.nextLine();
		//removes first and last char (don't use replace because < and . may be added to tree)
		lineOne = lineOne.substring(1, lineOne.length() - 1); 
		lineOne = lineOne.replace(",", ""); //remove commas from string
		lineOne = lineOne.replace(" ", ""); //remove spaces from string
		pretrav = lineOne.toCharArray(); //change pretrav to a char array
		
		String lineTwo = input.nextLine();
		//removes first and last char (don't use replace because < and . may be added to tree)
		lineTwo = lineTwo.substring(1, lineTwo.length() - 1); 
		lineTwo = lineTwo.replace(",", ""); //split line two into char array delimited by ","
		lineTwo = lineTwo.replace(" ", ""); //remove spaces from string
		posttrav = lineTwo.toCharArray(); //change posttrav into a char array
		
		//initialize queue
		q = new Queue();
		
		//build most-left tree (keep track of it in currentTree string). incrementing prestart to 
		//lowest child. Decrement size each time a node is added to the tree.  (size is initially 
		//pretrav.length)
		int size = pretrav.length;
		int prestart = 0;
		int poststart = 0;
		
		root = new Node(pretrav[prestart]);
		prestart++;
		size--;
		currentParent = root;
		currentTree = currentTree + root.data;
		//Build most-left tree
		while (pretrav[prestart] != posttrav[poststart]) {
			currentParent.addChild(pretrav[prestart]);
			currentParent = currentParent.children.get(0);
			currentTree = currentTree + pretrav[prestart];
			prestart++;
			size--;
		} //EXAMPLE:  prestart = 3, poststart = 0, size = 12, currentParent.data = 'B'
		
		//call buildtree(size, prestart, poststart) which will run recursively
		root = buildTree(size, prestart, poststart);
		
		//answer queries
		String currentQuery = "";

		//loop while there is still user input (SHOULD all be queries about node relationships)
		while (input.hasNextLine()) {
			//reads current query from input
			currentQuery = input.nextLine();
			//removes ? and .
			currentQuery = currentQuery.replace("?", "");
			currentQuery = currentQuery.replace(".", "");
			//remove "," and ""
			currentQuery = currentQuery.replace(",", "");
			currentQuery = currentQuery.replace(" ", "");
			//find relationship between 2 chars in query
			if (currentQuery.length() == 2) {
				findRelationship(currentQuery.charAt(0), currentQuery.charAt(1));
			}
			else {
				System.out.println("Query error: # chars: " + currentQuery.length());
			}
		}
		
		//print tree to output in level order, skipping a line for each level
		output.println();
		printTree(root);
		input.close();
		output.close();
	}
	
	//RECURSIVE funtion to build tree, returning a reference to the root node of said subtree  
	//This is a somewhat fragile function that depends on 
	public static Node buildTree(int size, int prestart, int poststart) {
		//size: number of nodes left to be built
		//prestart: place in pretrav where the preorder traversal of this tree begins 
		//poststart: place in posttrav where the postorder traversal of this tree begins 
		
		
		//If prestart char and poststart char are the same, add it to currentParent's children
		//Also move prestart and poststart forward and decrement size
		if (pretrav[prestart] == posttrav[poststart]) {
			currentParent.addChild(posttrav[poststart]);
			currentTree = currentTree + posttrav[poststart]; //add node to currentTree
			prestart++;
			poststart++;
			size--;
		}
		
		//If prestart char and poststart char are not the same, it must be the case that either:
		//poststart char is on the currentParent node, or
		//prestart char is on the NEXT parent node
		else {
			//Check if poststart is on the parent node, if it is, we're done with this parent so
			//move poststart forward, and find the next parent (necessary because pretrav may 
			//find the root to a subtree and this will move currentParent to the proper 
			//leftist node to attach that subtree root to)
			if (currentTree.indexOf(posttrav[poststart]) != -1) {
				poststart++;
				int i = poststart;
				
				while(currentTree.indexOf(posttrav[i]) == -1) {
					i++;
				}
				currentParent = findNode(posttrav[i], root);
			}			
			//if posttrav was not the problem it must be that pretrav is sitting on the next parent
			else {
				currentParent.addChild(pretrav[prestart]);
				currentTree = currentTree + pretrav[prestart];
				//need to change current parent because "if" above may not catch and update it
				currentParent = findNode(pretrav[prestart], root);
				prestart++;
				size--;
			}
		}		
		
		//Check if we've reached the end of the traversal, if so, recursively call this function.
		//If not, return root.
		if (size > 0) {
			return buildTree(size, prestart, poststart);
		}
		else {
			return root;
		}

	}
	
	//Find the relationship between two Nodes (or leafs/roots) given what they are
	public static void findRelationship(char A, char B) {
		Node first = findNode(A, root);
		Node second = findNode(B, root);
		int counterF = 0; //because root level is 0 and we don't want while to run if node is root
		int counterS = 0;
		
		//loop through first's ancestors (marking all of them up to root, including first itself)
		//then look for the common ancestor and count how many far away the two are
		Node traveler = first;
		while (!traveler.mark) {
			traveler.mark = true;
			traveler = traveler.parent;
		} 
		//loop through second's ancestors until we find a mark, count each loop
		traveler = second;
		while (!traveler.mark) {
			traveler = traveler.parent;
			counterS++;
		}
		//find first's distance from common ancestor
		Node common = traveler;
		counterF = first.level - common.level;
		
		//clear marks, return counter data 
		traveler = first;
		while (traveler.mark) {
			traveler.mark = false;
			traveler = traveler.parent;	
		} 
		//use counter data to print the relationship between "A" and "B"
		//(be sure to use real chars in answer)
		//Relationships:
		if (counterF == counterS) { //F is some S/sibling/cousin
			if (counterF == 0) {
				output.println(A + " is " + B + ".");
			}
			else if (counterF == 1) {
				output.println(A + " is " + B + "'s sibling.");
			}
			else if (counterF >= 2) {  //because second = first, we use first - 1 (doesn't matter)
				output.println(A + " is " + B + "'s " + (counterF - 1) + "th cousin " +
										(counterF - counterS) + " times removed.");
			}
		}
		else if (counterF > counterS) { //F is some parent/aunt/uncle
			 if (counterF == 1) { //obvious here that counterS would equal 0
				 output.println(A + " is " + B + "'s child.");
			 }
			 else if (counterF == 2 && counterS == 0) {
				 output.println(A + " is " + B + "'s grandchild.");
			 }
			 else if (counterF == 2 && counterS == 1) {
				 output.println(A + " is " + B + "'s niece/nephew.");
			 }
			 else if (counterF >= 3 && counterS == 0) {
				 output.println(A + " is " + B + "'s (great)^" + (counterF - 2) + "-grandchild.");
			 }
			 else if (counterF >= 2 && counterS == 1) {
				 output.println(A + " is " + B + "'s (great)^" + (counterF - 2) + "-niece/nephew.");
			 }
			 else if (counterF >= 2 && counterS >= 2) { //because second < first, we use second - 1
														//and counterF - counterS
				 output.println(A + " is " + B + "'s " + (counterS - 1) + "th cousin " + 
										(counterF - counterS) + " times removed.");
			 }
		}
		//These should mirror operations of counterF > counterS, but flipped signs and change 
		//"parent/aunt/uncle" to "child/niece/nephew"
		else if (counterF < counterS) { //F is some child/niece/nephew
			 if (counterS == 1) { //obvious that counterF would equal 0
				 output.println(A + " is " + B + "'s parent.");
			 }
			 else if (counterF == 0 && counterS == 2) {
				 output.println(A + " is " + B + "'s grandchild.");
			 }
			 else if (counterF == 1 && counterS == 2) {
				 output.println(A + " is " + B + "'s aunt/uncle.");
			 }
			 else if (counterF == 0 && counterS >= 3) {
				 output.println(A + " is " + B + "'s (great)^" + (counterF - 2) + "-grandparent.");
			 }
			 else if (counterF == 1 && counterS >= 2) {
				 output.println(A + " is " + B + "'s (great)^" + (counterF - 2) + "-aunt/uncle.");
			 }
			
			else if (counterF >= 2 && counterS >= 2) {  //because second > first, we use second - 1
														//and counterS - counterF
				output.println(A + " is " + B + "'s " + (counterF - 1) + "th cousin " +
										(counterS - counterF) + " times removed.");
			}
		}
	}
	
	//Prints tree by enqueuing children of roots and printing in queue order
	public static void printTree(Node root) {
		//add root as first element of queue and print it on line 1
		q.enqueue(root);
		output.println(q.front.data);
		//int keeping track of the line being printed (distance from root)
		int level = 0;
		//loop for as long as queue isn't empty
		while (q.size > 0) {
			//set current to next node in queue (removing it from queue in the process) 
			Node current = q.dequeue();
			//if a child of current is of a higher level than currentLevel, print line and
			//increment current level
			if (current.level > level) {
				level++;
				output.println();
			}
			//loop for all of current node's children
			for (int i = 0; i < current.children.size(); i++) {
				//if the child at this index of current's children list is not null, print it
				//and put it in the back of the queue
				if (current.children.get(i) != null) {
					output.print(current.children.get(i).data);
					q.enqueue(current.children.get(i));
				}
			}
		}
	}
	
	//Find and return node that holds the value passed to this method
	public static Node findNode(char data, Node traversal) {
		//if passed Node has the data we're looking for, return it
		if (traversal.data == data) {
			return traversal;
		}
		//if it doesn't, loop through its children and call the function again recursively
		else {
			for (int i = 0; i < traversal.children.size(); i++) {
				Node child = traversal.children.get(i);
				Node found = findNode(data, child);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
	
}
