import org.pcollections.Empty;
import org.pcollections.PStack;

import java.util.*;

/**
 * Created by Sharma on 12/19/13.
 */
public class ParallelBFS extends Solver{

	@Override
	public String getType() {
		return "Parallel BFS";
	}

	@Override
	public ArrayList<String> solve(FifteenPuzzle puzzle){
		DataStore dataStore = new DataStore();
		BossThread thread = new BossThread(puzzle, dataStore);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return dataStore.getSolution();
	}
	/*
	ArrayList<String> solveParallelBFS(){
		seen = Collections.synchronizedList(new ArrayList<String>());
		seen.add(serialize(board));

		tryQueue = new ConcurrentLinkedDeque<Object[]>();
		tryQueue.push(new Object[]{serialize(board), Empty.stack()});
		while(true){
			Object[] tmp = tryQueue.pop();
			String cur = (String)tmp[0];
			board = deserialize(cur);
			for(int i = 0; i<board.length; i++)
				for(int j = 0; j<board[i].length; j++)
					if(board[i][j]==-1)
						empty = new int[]{i,j};


			PStack<int[]> solStack = (PStack<int[]>)tmp[1];

			if(this.isSolved()){
				print();
				ArrayList<String> ret = new ArrayList<String>();

				while(!solStack.isEmpty()){
					ret.add(moveName(solStack.get(0)));
					solStack = solStack.minus(0);
				}
				Collections.reverse(ret);
				return ret;
			}

			ArrayList<int[]> moves = this.availableMoves();
			for(int[] move:moves){
				this.makeMove(move);
				String serBoard = serialize(board);

				if(!seen.contains(serBoard)){
					//this.print();

					//System.out.println(moveName(move)+"\n");

					//solStack.add(move);

					tryQueue.addLast(new Object[]{serBoard, solStack.plus(move)});

					seen.add(serBoard);
				}
				this.unmakeMove(move);
			}
		}

	}*/
}

class BossThread extends Thread {
	private DataStore dataStore;// = new DataStore();
	private FifteenPuzzle puzzle;
	private int numThreads = Runtime.getRuntime().availableProcessors();
	ArrayDeque<Object[]> tryQueue = new ArrayDeque<Object[]>();
	ArrayList<ArrayDeque<Object[]>> tryQueues = new ArrayList<ArrayDeque<Object[]>>();
	ArrayList<WorkerThread> workers = new ArrayList<WorkerThread>();
	ArrayList<String> seen = new ArrayList<String>();

	BossThread(FifteenPuzzle puzzle, DataStore dataStore){
		this.puzzle = puzzle;
		this.dataStore = dataStore;
	}

	public void run(){
		tryQueue.push(new Object[]{puzzle.getState(), Empty.stack()});
		seen.add(puzzle.getState());
		while(tryQueue.size()<numThreads){
			Object[] tmp = tryQueue.pop();
			String cur = (String)tmp[0];
			puzzle.setState(cur);

			//puzzle.print();

			PStack<int[]> solStack = (PStack<int[]>)tmp[1];

			if(puzzle.isSolved()){

				//puzzle.print();
				ArrayList<String> ret = new ArrayList<String>();

				while(!solStack.isEmpty()){
					ret.add(puzzle.moveName(solStack.get(0)));
					solStack = solStack.minus(0);
				}
				Collections.reverse(ret);
				dataStore.AddSolution(ret);
				return;
			}

			ArrayList<int[]> moves = puzzle.availableMoves();
			for(int[] move:moves){
				puzzle.makeMove(move);
				String serBoard = puzzle.getState();

				if(!seen.contains(serBoard)){
					tryQueue.addLast(new Object[]{serBoard, solStack.plus(move)});

					seen.add(serBoard);
				}
				puzzle.unmakeMove(move);
			}
		}
		int per = tryQueue.size()/numThreads;
		int rem = tryQueue.size()%numThreads;
		//FifteenPuzzle tmp = new FifteenPuzzle(puzzle.getN());

		for(int i = 0; i<numThreads; i++){
			ArrayDeque<Object[]> queue = new ArrayDeque<Object[]>();
			int size = per;
			if(i<rem) size++;
			for(int j = 0; j<size; j++){
				queue.push(tryQueue.pop());
			}
			//tryQueues.add(queue);
			//System.out.println("************************");
			//for(Object[] obj : queue){
			//	tmp.setState((String) obj[0]);
			//	tmp.print();
			//}
			workers.add(new WorkerThread(dataStore, queue,seen, puzzle.getN()));
		}	
		for(WorkerThread worker : workers){
			worker.start();
		}
		for(WorkerThread worker : workers){
			try{
			worker.join();
			}
			catch(Throwable e){e.printStackTrace();}
		}
	}
}

class WorkerThread extends Thread {
	private DataStore dataStore;
	ArrayDeque<Object[]> tryQueue;
	FifteenPuzzle puzzle;
	List<String> seen;

	WorkerThread(DataStore dataStore, ArrayDeque tryQueue, ArrayList<String> seen, int n){
		this.dataStore = dataStore;
		this.tryQueue = tryQueue;
		puzzle = new FifteenPuzzle(n);
		//this.seen = seen;
		this.seen = new ArrayList<String>(seen);
	}

	public void run(){
		while(true){
			Object[] tmp = tryQueue.pop();
			String cur = (String)tmp[0];
			puzzle.setState(cur);

			PStack<int[]> solStack = (PStack<int[]>)tmp[1];

			if(puzzle.isSolved()){

				//puzzle.print();


				ArrayList<String> ret = new ArrayList<String>();

				while(!solStack.isEmpty()){
					ret.add(puzzle.moveName(solStack.get(0)));
					solStack = solStack.minus(0);
				}
				Collections.reverse(ret);
				//for(String s : ret)
				//
				// 	System.out.print(s+", ");
				dataStore.AddSolution(ret);
				return;
			}

			if(dataStore.IsDone(solStack.size()))
				return;

			ArrayList<int[]> moves = puzzle.availableMoves();
			for(int[] move:moves){
				puzzle.makeMove(move);
				String serBoard = puzzle.getState();
				//puzzle.print();
				if(!seen.contains(serBoard)){
					tryQueue.addLast(new Object[]{serBoard, solStack.plus(move)});

					seen.add(serBoard);
				}
				puzzle.unmakeMove(move);
			}
		}
	}
}

class DataStore{
	List<String> seen;
	List<ArrayList<String>> solutions;
	int curSolDepth;

	DataStore(){
		seen = Collections.synchronizedList(new ArrayList<String>());
		solutions = Collections.synchronizedList(new ArrayList<ArrayList<String>>());
		curSolDepth = 0;
	}

	boolean IsInSeen2(String s){
		return seen.contains(s);
	}

	void AddToSeen2(String s){
		seen.add(s);
		//AddSolution(new ArrayList<String>(Arrays.asList("a98wehnfaihef89awe8ofhwae")));
	}

	void AddSolution(ArrayList<String> sol){
		solutions.add(sol);
		int depth = sol.size();
		if(curSolDepth>depth)
			curSolDepth = depth;
	}

	ArrayList<String> getSolution(){
		if(solutions.size()==0)
			return new ArrayList<String>();
		int min = 0;
		int minIndex = 0;
		for(int i = 0; i<solutions.size(); i++){
			if(solutions.get(i).size()<min){
				min=solutions.get(i).size();
				minIndex = i;
			}
		}
		return solutions.get(minIndex);
	}

	//true stop; false keep going
	boolean IsDone(int depth){
		//System.out.println(depth);
		/*
		for(Iterator<ArrayList<String>> it = solutions.iterator(); it.hasNext();)
			if(depth>it.next().size())
				return true;
		return true;*/
		if(solutions.size()==0)
			return false;
		return (depth>curSolDepth);
	}

}