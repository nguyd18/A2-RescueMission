package ca.mcmaster.se2aa4.island.team023;

public class Heading {

	private HeadingStates heading;

	public enum HeadingStates {

		N {
			@Override
			public Point<Integer> getNextPoint() {return new Point<>(0, -1);}

			@Override
			public HeadingStates next() {return HeadingStates.E;}
			
			@Override
			public HeadingStates prev() {return HeadingStates.W;}
		},
		E {
			@Override
			public Point<Integer> getNextPoint() {return new Point<>(1, 0);}
			
			@Override
			public HeadingStates next() {return HeadingStates.S;}
			
			@Override
			public HeadingStates prev() {return HeadingStates.N;}
		},
		S {
			@Override
			public Point<Integer> getNextPoint() {return new Point<>(0, 1);}
			
			@Override
			public HeadingStates next() {return HeadingStates.W;}
			
			@Override
			public HeadingStates prev() {return HeadingStates.E;}
		},
		W {
			@Override
			public Point<Integer> getNextPoint() {return new Point<>(-1, 0);}
			
			@Override
			public HeadingStates next() {return HeadingStates.N;}
			
			@Override
			public HeadingStates prev() {return HeadingStates.S;}
		};

		public abstract Point<Integer> getNextPoint();
		public abstract HeadingStates next();
		public abstract HeadingStates prev();

	}

	public Heading(String startState) {
		heading = HeadingStates.valueOf(startState);

	}

	public HeadingStates getHeadingState() {
		return heading;
	}

	public void setHeadingState(HeadingStates newState) {
		heading = newState;
	}
	
	public void setHeadingState(String newState) {
		heading = HeadingStates.valueOf(newState);
	}

	public void turnClockwise() {
		heading = heading.next();
	}
	
	public void turnCounterClockwise() {
		heading = heading.prev();
	}

}
