import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Sharma on 12/19/13.
 */
public class FifteenPuzzle {
	private static final char NEXT_ITEM = ' ';
	static int[] UP   ={-1,0};
	static int[] DOWN ={1, 0};
	static int[] LEFT ={0,-1};
	static int[] RIGHT={0, 1};

	private int[][] board;
	private int n;
	private int[] empty;

	FifteenPuzzle(int n){
		board = new int[n][n];
		this.n = n;
		empty = new int[]{n-1 , n-1};
		int tmp = 1;
		for(int i = 0; i<n; i++){
			for(int j = 0; j<n; j++){
				board[i][j]=tmp++;
			}
		}
		this.board[n-1][n-1]=-1;
	}

	int getN(){
		return n;
	}

	String moveName(int[] move){
		if(Arrays.equals(move,UP))
			return "UP";
		else if(Arrays.equals(move,DOWN))
			return "DOWN";
		else if(Arrays.equals(move,LEFT))
			return "LEFT";
		else if(Arrays.equals(move,RIGHT))
			return "RIGHT";
		else
			return "ERROR";
	}

	void makeMove(int[] move){
		int swap = board[empty[0]+move[0]][empty[1]+move[1]];
		board[empty[0]][empty[1]] = swap;
		board[empty[0]+move[0]][empty[1]+move[1]] = -1;
		empty = new int[]{empty[0]+move[0], empty[1]+move[1]};
	}

	void unmakeMove(int[] move){
		this.makeMove(new int[]{move[0]*-1, move[1]*-1});
	}

	ArrayList<int[]> availableMoves(){
		ArrayList<int[]> moves = new ArrayList<int[]>();
		int row = empty[0];
		int col = empty[1];

		if(row>0)
			moves.add(UP);
		if(row<n-1)
			moves.add(DOWN);
		if(col>0)
			moves.add(LEFT);
		if(col<n-1)
			moves.add(RIGHT);

		return moves;
	}

	void shuffle(int moves){
		Random rand = new Random();
		ArrayList<String> prevMoves = new ArrayList<String>();
		while(prevMoves.size()<moves){
			ArrayList<int[]> availMoves = this.availableMoves();
			int randNum = rand.nextInt(availMoves.size());
			this.makeMove(availMoves.get(randNum));
			if(prevMoves.contains(this.getState()))
				this.unmakeMove(availMoves.get(randNum));
			else
				prevMoves.add(this.getState());
		}
	}

	boolean isSolved(){
		if(board[n-1][n-1]!=-1)
			return false;
		int tmp = 1;
		for(int i = 0; i<n; i++)
			for(int j = 0; j<n; j++)
				if(!(i==n-1 && j==n-1))
					if(board[i][j]!=tmp++)
						return false;

		return true;
	}

	int getFastHeuristic(){
		return ((n-1)-empty[0])+((n-1)-empty[1]);
	}

	int getGoodHeuristic(){
		int heuristic = 0;
		for(int i = 0; i<n; i++){
			for(int j = 0; j<n; j++){
				int cur = board[i][j];
				if(cur!=-1){
					int row = (cur-1)/n;
					int col = (cur-1)%n;
					heuristic += Math.abs(i-row) + Math.abs(j-col);
				}
			}
		}
		return heuristic;
	}

	void print(){
		for(int i = 0; i<n; i++){
			for(int j = 0; j<n; j++){
				System.out.print(board[i][j]+"\t");
			}
			System.out.println();
		}
		System.out.println(getGoodHeuristic());
	}

	String getState(){
		return (empty[0]+"/"+empty[1]+"/"+serialize(board));
	}

	void setState(String state){
		String[] split = state.split("/");
		if(split.length==3){
			empty[0] = Integer.parseInt(split[0]);
			empty[1] = Integer.parseInt(split[1]);
			board = deserialize(split[2]);
		}
	}
/*
	void solve(){
		String state = getState();

		long startTime = System.nanoTime();
		ArrayList<String> sols = BFS.solveBFS(this);
		long endTime = System.nanoTime();

		for(String sol:sols)
			System.out.print(sol+", ");

		System.out.println("\n"+(endTime-startTime)/1000000000.);

		setState(state);

		startTime = System.nanoTime();
		sols = ParallelBFS.solveParallelBFS(this);
		endTime = System.nanoTime();

		for(String sol:sols)
			System.out.print(sol+", ");

		System.out.println("\n"+(endTime-startTime)/1000000000.);
	}
*/
	void test(long num){


		String state = getState();

		Solver[] solvers = {
				new AStar(AStar.DIJKSTRA),
				new AStar(AStar.GOOD_HEURISTIC),
				new AStar(AStar.FAST_HEURISTIC),
				new ParallelBFS(),
				new SharedParallelBFS(),
				new BFS()
		};

		ArrayList<String> sols = new ArrayList<String>();
		//long sum = 0;
		ArrayList<Long> sum = new ArrayList<Long>();

		//System.out.println();

		//System.out.print("Method,Solution Depth,Time");

		for(Solver solver : solvers){
			try{
				Thread.sleep(100);
			} catch(Throwable e){e.printStackTrace();}
			//sum = 0;
			//ArrayList<String> curSol = new ArrayList<String>(sols);
			for(int i = 0; i<num; i++){
				setState(state);
				long startTime = System.nanoTime();
				sols = solver.solve(this);
				long endTime = System.nanoTime();
				//if(curSol.equals(sols))
				sum.add(endTime-startTime);
				//else
				//	System.exit(-1234);
				//sum+=(endTime-startTime);
			}
			//ave = sum/num;
			long ave = 0;
			for(long cur : sum){
				ave+=cur;
			}
			ave=ave/sum.size();

			//System.out.println("*********************************");
			//System.out.println(solver.getType());
			//for(String sol:sols)
			//	System.out.print(sol+", ");
			//System.out.println("\n"+(ave)/1000000000.);

			System.out.print("\n"+solver.getType()+","+sols.size()+","+(ave)/1000000000.);
		}
	}






	String serialize(int[][] array) {
		StringBuilder s = new StringBuilder();
		s.append(array.length).append(NEXT_ITEM);

		for(int[] row : array) {
			s.append(row.length).append(NEXT_ITEM);

			for(int item : row) {
				s.append(String.valueOf(item)).append(NEXT_ITEM);
			}
		}
		//s.append(this.empty[0]).append(NEXT_ITEM);
		//s.append(this.empty[1]).append(NEXT_ITEM);
		return s.toString();
	}

	int[][] deserialize(String str) {
		try{
			StreamTokenizer tok = new StreamTokenizer(new StringReader(str));
			tok.resetSyntax();
			tok.wordChars('0', '9');
			tok.whitespaceChars(NEXT_ITEM, NEXT_ITEM);
			tok.parseNumbers();

			tok.nextToken();

			int     rows = (int) tok.nval;
			int[][] out  = new int[rows][];

			for(int i = 0; i < rows; i++) {
				tok.nextToken();

				int   length = (int) tok.nval;
				int[] row    = new int[length];
				out[i]       = row;

				for(int j = 0; j < length; j++) {
					tok.nextToken();
					row[j] = (int) tok.nval;
				}
			}

			return out;
		}
		catch(IOException e){
			return null;
		}
	}

}
