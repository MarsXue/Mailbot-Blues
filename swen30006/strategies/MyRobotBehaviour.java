package strategies;

import automail.PriorityMailItem;
import automail.StorageTube;

public class MyRobotBehaviour implements IRobotBehaviour {
	
	private boolean newPriority;

	public MyRobotBehaviour() {
		newPriority = false;
	}

	@Override
	public void startDelivery() {
		newPriority = false;	
	}
	
	@Override
	public void priorityArrival(int priority, int weight) {
		newPriority = true;
	}

	@Override
	public boolean returnToMailRoom(StorageTube tube) {
		
		if (tube.isEmpty()) {
			return false; // Empty tube means we are returning anyway
		} else {
			// Return if we don't have a priority item and a new one came in
			Boolean priority = (tube.peek() instanceof PriorityMailItem);
			return !priority && newPriority;
		}
	}

}
