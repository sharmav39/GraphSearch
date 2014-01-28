import org.pcollections.Empty;
import org.pcollections.PStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by Sharma on 1/8/14.
 */
public class AStar extends Solver {

	public static final int DIJKSTRA = 0;
	public static final int GOOD_HEURISTIC = 1;
	public static final int FAST_HEURISTIC = 2;

	private int heuristic;

	public AStar(int heuristicType){
		this.heuristic = heuristicType;
	}

	@Override
	public String getType() {
		if(this.heuristic==this.DIJKSTRA)
			return "Dijkstra's Algorithm";
		else if(this.heuristic==this.GOOD_HEURISTIC)
			return "AStar Good Heuristic";
		else if(this.heuristic==this.FAST_HEURISTIC)
			return "AStar Fast Heuristic";
		else
			return "ERROR";
	}

	private int getHeuristic(FifteenPuzzle puzzle){
		if(this.heuristic==this.DIJKSTRA)
			return 0;
		else if(this.heuristic==this.GOOD_HEURISTIC)
			return puzzle.getGoodHeuristic();
		else if(this.heuristic==this.FAST_HEURISTIC)
			return puzzle.getFastHeuristic();
		else
			return 0;
	}

	@Override
	public ArrayList<String> solve(FifteenPuzzle puzzle){
		ArrayList<String> seen = new ArrayList<String>();
		seen.add(puzzle.getState());//serialize(board));

		//ArrayDeque<Object[]> tryQueue = new ArrayDeque<Object[]>();
		PriorityQueue<Node> tryQueue = new PriorityQueue<Node>();
		//tryQueue.push(new Object[]{puzzle.getState(), Empty.stack()});

		tryQueue.add(new Node(puzzle.getState(), Empty.stack(), this.getHeuristic(puzzle)));
		while(true){
			//Object[] tmp = tryQueue.pop();
			Node tmp = tryQueue.poll();
			//String cur = (String)tmp[0];
			String cur = tmp.getBoard();
			//board = deserialize(cur);
			//for(int i = 0; i<board.length; i++)
			//	for(int j = 0; j<board[i].length; j++)
			//		if(board[i][j]==-1)
			//			empty = new int[]{i,j};
			puzzle.setState(cur);

			PStack<int[]> solStack = tmp.getSolStack();//PStack<int[]>)tmp[1];

			if(puzzle.isSolved()){
				//print();
				ArrayList<String> ret = new ArrayList<String>();

				while(!solStack.isEmpty()){
					ret.add(puzzle.moveName(solStack.get(0)));
					solStack = solStack.minus(0);
				}
				Collections.reverse(ret);
				return ret;
			}

			ArrayList<int[]> moves = puzzle.availableMoves();
			for(int[] move:moves){
				puzzle.makeMove(move);
				String serBoard = puzzle.getState();//serialize(board);

				if(!seen.contains(serBoard)){
					//this.print();

					//System.out.println(moveName(move)+"\n");

					//solStack.add(move);

					tryQueue.add(new Node(serBoard, solStack.plus(move), this.getHeuristic(puzzle)));
					//tryQueue.addLast(new Object[]{serBoard, solStack.plus(move)});

					seen.add(serBoard);
				}
				puzzle.unmakeMove(move);
			}
		}
	}
}


class Node implements Comparable<Node>{

	private String board;
	private PStack<int[]> solStack;
	private int prevDist;
	private int heuristic;
	private int totalDist;

	public Node(String board, Object solStack, int heuristic){
		this.board = board;
		this.solStack = (PStack<int[]>)solStack;
		this.prevDist = this.solStack.size();
		this.heuristic = heuristic;
		this.totalDist = this.prevDist + this.heuristic;
	}
	
	public int getTotalDist(){
		return totalDist;
	}

	public PStack<int[]> getSolStack(){
		return solStack;
	}

	public String getBoard(){
		return board;
	}
	
	@Override
	public int compareTo(Node node) {
		return this.getTotalDist() - node.getTotalDist();
	}
}


class PriorityQueueComparator implements Comparator<Object[]>
{
	@Override
	public int compare(Object[] x, Object[] y)
	{
		// Assume neither string is null. Real code should
		// probably be more robust
		/*if (x.length() < y.length())
		{
			return -1;
		}
		if (x.length() > y.length())
		{
			return 1;
		}*/
		return 0;
	}
}
