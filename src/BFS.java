import org.pcollections.Empty;
import org.pcollections.PStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Sharma on 12/30/13.
 */
public class BFS extends Solver{

	@Override
	public String getType() {
		return "BFS";
	}

	@Override
	public ArrayList<String> solve(FifteenPuzzle puzzle){
		ArrayList<String> seen = new ArrayList<String>();
		seen.add(puzzle.getState());//serialize(board));

		ArrayDeque<Object[]> tryQueue = new ArrayDeque<Object[]>();
		tryQueue.push(new Object[]{puzzle.getState(), Empty.stack()});
		while(true){
			Object[] tmp = tryQueue.pop();
			String cur = (String)tmp[0];
			//board = deserialize(cur);
			//for(int i = 0; i<board.length; i++)
			//	for(int j = 0; j<board[i].length; j++)
			//		if(board[i][j]==-1)
			//			empty = new int[]{i,j};
			puzzle.setState(cur);

			PStack<int[]> solStack = (PStack<int[]>)tmp[1];

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

					tryQueue.addLast(new Object[]{serBoard, solStack.plus(move)});

					seen.add(serBoard);
				}
				puzzle.unmakeMove(move);
			}
		}
	}
}
