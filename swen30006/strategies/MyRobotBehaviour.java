/* SWEN30006 Software Modelling and Design
 * Project Part A
 * 
 * MyRobotBehaviour class is to change the behaviour of robots
 * When priority mailitem arrives, robots can return to mail room
 * After a series of testing the score, it is unnessary to ask
 * robots to return the mail room, since return to mail room will
 * be inefficient for robots delivering the close destination floors
 * In conclusion, it is better to just return false in my code
 * 
 * @author Wenqing Xue
 */

package strategies;

//import automail.PriorityMailItem;
import automail.StorageTube;

public class MyRobotBehaviour implements IRobotBehaviour {
	// robot type, strong robot if true else weak robot
	private boolean strong;
		
	/**
	 * Constructor
	 * @param strong boolean type for robot type
	 */
	public MyRobotBehaviour(boolean strong) {
		strong = this.strong;
	}
	
	/**
	 * startDelivery() provides the robot the opportunity to initialise state
	 * in support of the other methods below. 
	 */
	public void startDelivery() {
	}
	
    /**
     * @param priority is that of the priority mail item which just arrived.
     * @param weight is that of the same item.
     * The automail system broadcasts this information to all robots
     * when a new priority mail items arrives at the building.
     */
	@Override
    public void priorityArrival(int priority, int weight) {
    }
 
	/** 
	 * @param tube refers to the pack the robot uses to deliver mail.
	 * @return When this is true, the robot is returned to the mail room.
	 * The robot will always return to the mail room when the tube is empty.
	 * This method allows the robot to return with items still in the tube,
	 * if circumstances make this desirable.
	 */
	@Override
	public boolean returnToMailRoom(StorageTube tube) {
		return false;
	}
}
