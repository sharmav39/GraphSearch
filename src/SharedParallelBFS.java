import org.pcollections.Empty;
import org.pcollections.PStack;

import java.util.*;

/**
 * Created by Sharma on 12/19/13.
 */
public class SharedParallelBFS extends Solver {

	@Override
	public String getType() {
		return "Shared Parallel BFS";
	}

	@Override
	public ArrayList<String> solve(FifteenPuzzle puzzle){
		DataStore2 dataStore = new DataStore2();
		BossThread2 thread = new BossThread2(puzzle, dataStore);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return dataStore.getSolution();
	}

}

class BossThread2 extends Thread {
	private DataStore2 dataStore;// = new DataStore();
	private FifteenPuzzle puzzle;
	private int numThreads = Runtime.getRuntime().availableProcessors();
	ArrayDeque<Object[]> tryQueue = new ArrayDeque<Object[]>();
	ArrayList<ArrayDeque<Object[]>> tryQueues = new ArrayList<ArrayDeque<Object[]>>();
	ArrayList<WorkerThread2> workers = new ArrayList<WorkerThread2>();

	BossThread2(FifteenPuzzle puzzle, DataStore2 dataStore){
		this.puzzle = puzzle;
		this.dataStore = dataStore;
	}

	public void run(){
		tryQueue.push(new Object[]{puzzle.getState(), Empty.stack()});
		dataStore.AddToSeen(puzzle.getState());
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

				if(!dataStore.IsInSeen(serBoard)){
					tryQueue.addLast(new Object[]{serBoard, solStack.plus(move)});

					dataStore.AddToSeen(serBoard);
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
			workers.add(new WorkerThread2(dataStore, queue, puzzle.getN()));
		}
		for(WorkerThread2 worker : workers){
			worker.start();
		}
		for(WorkerThread2 worker : workers){
			try{
				worker.join();
			}
			catch(Throwable e){e.printStackTrace();}
		}
	}
}

class WorkerThread2 extends Thread {
	private DataStore2 dataStore;
	ArrayDeque<Object[]> tryQueue;
	FifteenPuzzle puzzle;

	WorkerThread2(DataStore2 dataStore, ArrayDeque tryQueue, int n){
		this.dataStore = dataStore;
		this.tryQueue = tryQueue;
		puzzle = new FifteenPuzzle(n);
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
				if(!dataStore.IsInSeen(serBoard)){
					tryQueue.addLast(new Object[]{serBoard, solStack.plus(move)});

					dataStore.AddToSeen(serBoard);
				}
				puzzle.unmakeMove(move);
			}
		}
	}
}

class DataStore2{
	List<String> seen;
	List<ArrayList<String>> solutions;
	int curSolDepth;

	DataStore2(){
		seen = Collections.synchronizedList(new ArrayList<String>());
		solutions = Collections.synchronizedList(new ArrayList<ArrayList<String>>());
		curSolDepth = 0;
	}

	boolean IsInSeen(String s){
		return seen.contains(s);
	}

	void AddToSeen(String s){
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
		if(solutions.size()==0)
			return false;
		return (depth>curSolDepth);
	}

}