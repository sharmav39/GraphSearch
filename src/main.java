/**
 * Created by Sharma on 12/19/13.
 */
public class main {

	public static void main(String[] args){
		FifteenPuzzle puzzle = new FifteenPuzzle(4);

		/*
		puzzle.makeMove(puzzle.LEFT);
		puzzle.print();
		puzzle.makeMove(puzzle.UP);
		puzzle.print();
		puzzle.makeMove(puzzle.LEFT);
		puzzle.print();
		puzzle.makeMove(puzzle.UP);
		puzzle.print();
		puzzle.makeMove(puzzle.LEFT);
		puzzle.print();
		puzzle.makeMove(puzzle.UP);
		puzzle.print();
		*/
		String solved = puzzle.getState();

		//puzzle.makeMove(puzzle.DOWN);
		//puzzle.makeMove(puzzle.RIGHT);
		//puzzle.print();
		//ArrayList<int[]> avail = puzzle.availableMoves();
		//for(int i = 0; i<avail.size(); i++)
		//	System.out.println(puzzle.moveName(avail.get(i)));
		//System.out.println(puzzle.isSolved());
		//puzzle.solve();

		/*while(true){
			puzzle = new FifteenPuzzle(4);
			puzzle.shuffle(10);
		}*/
		for(int i = 0; i<10; i++){
			puzzle.setState(solved);
			puzzle.shuffle(2);
			//puzzle.print();
			puzzle.test(50);
		}


		//puzzle.shuffle(1000);
		//System.out.println(puzzle.getGoodHeuristic());

	}
}
