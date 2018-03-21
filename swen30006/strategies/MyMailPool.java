/* SWEN30006 Software Modelling and Design
 * Project Part A
 * 
 * MyMailPool class is to arrange mailitems for robots delivering.
 * There are two priority queue for sorting priority and non-priority mailitems.
 * Sorting order:	1. priority level (descending)
 * 				  	2. destionation floor (ascending)
 * 					3. weight (descending)
 * Find the four most satisfied mailitems if possible,
 * And deliver the mailitems by different situations:
 * 1. Deliver priority mailitems upstairs, non-priority mailitems downstairs
 * 2. Deliver only priority or non-priority mailitems upstairs
 * 
 * @author Wenqing Xue
 */

package strategies;

import java.util.*;
import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailPool implements IMailPool {
	// priority queue for pool to store priority mailitem
	private PriorityQueue<MailItem> priorityPool;
	// priority queue for pool to store non-priority mailitem
	private PriorityQueue<MailItem> nonPriorityPool;
	// maximum mailitem number for robot to carry
	private static final int MAX_TAKE = 4;
	
	/**
	 * Constructor
	 */
	public MyMailPool() {
		
		// priority queue for pool to store priority mailitem
		priorityPool = new PriorityQueue<MailItem>(new Comparator<MailItem>() {
			// sort by priority level (descending),
			// destination floor (ascending), weight (descending)
			public int compare(MailItem m1, MailItem m2) {
				if (((PriorityMailItem)m2).getPriorityLevel() != 
					((PriorityMailItem)m1).getPriorityLevel()) {
					return ((PriorityMailItem)m2).getPriorityLevel() - 
				((PriorityMailItem)m1).getPriorityLevel();
				} else if (m1.getDestFloor() != m2.getDestFloor()) {
					return m1.getDestFloor() - m2.getDestFloor();
				} else {
					return m2.getWeight() - m1.getWeight();
				}
			}
		});
		
		// priority queue for pool to store non-priority mailitem
		nonPriorityPool = new PriorityQueue<MailItem>(new Comparator<MailItem>() {
			// sort by destination floor (ascending), weight (descending)
			public int compare(MailItem m1, MailItem m2) {
				if (m1.getDestFloor() != m2.getDestFloor()) {
					return m1.getDestFloor() - m2.getDestFloor();
				} else {
					return m2.getWeight() - m1.getWeight();
				}
			}
		});
	}

	/*
	 * Add the mailitem into the corresponding pool
	 * @param mailItem item need to add in pool
	 */
	public void addToPool(MailItem mailItem) {
		if (mailItem instanceof PriorityMailItem) {
			priorityPool.add(mailItem);
		} else {
			nonPriorityPool.add(mailItem);
		}
	}
	
	/*
	 * Check the priority pool size with restrict of weight limit
	 * @param weightLimit maximum weight limit for robot to carry
	 * @return available priority pool size within weight limit
	 */
	private int getPriorityPoolSize(int weightLimit) {
		if (!priorityPool.isEmpty() && priorityPool.peek().getWeight() > weightLimit) {
			return 0;
		}
		return priorityPool.size();
	}
	
	/*
	 * Check the non-priority pool size with restrict of weight limit
	 * @param weightLimit maximum weight limit for robot to carry
	 * @return available non-priority pool size within weight limit
	 */
	private int getNonPriorityPoolSize(int weightLimit) {
		if (!nonPriorityPool.isEmpty() && nonPriorityPool.peek().getWeight() > weightLimit) {
			return 0;
		}
		return nonPriorityPool.size();
	}
	
	/*
	 * Get the first priority mailitem with restrict of weight limit
	 * @param weightLimit maximum weight limit for robot to carry
	 * @return first priority mailitem within weight limit
	 */
	private MailItem getPriorityMail(int weightLimit) {
		if (getPriorityPoolSize(weightLimit) > 0) {
			return priorityPool.poll();
		} else {
			return null;
		}
	}

	/*
	 * Get the first non-priority mailitem with restrict of weight limit
	 * @param weightLimit maximum weight limit for robot to carry
	 * @return first non-priority mailitem within weight limit
	 */
	private MailItem getNonPriorityMail(int weightLimit) {
		if (getNonPriorityPoolSize(weightLimit) > 0) {
			return nonPriorityPool.poll();
		} else {
			return null;
		}
	}
	
	/*
	 * Comparator for sorting priority queue of priority mailitem
	 * Sort by destination floor (ascending)
	 * Priority mailitem will be placed firstly in tube
	 * As the priority level is sorted in priorityPool
	 * Destination floor order will be only consideration
	 */
	private class floorComparePri implements Comparator<MailItem> {
		public int compare(MailItem m1, MailItem m2) {
			return m1.getDestFloor() - m2.getDestFloor();
		}
	}
	
	/*
	 * Comparator for sorting priority queue of non-priority mailitem
	 * Sort by destination floor (ascending), arrival time (ascending)
	 * For case when there is no priority mailitem in tube
	 */
	private class floorCompareAs implements Comparator<MailItem> {
		public int compare(MailItem m1, MailItem m2) {
			if (m1.getDestFloor() == m2.getDestFloor()) {
				return m1.getArrivalTime() - m2.getArrivalTime();
			}
			return m1.getDestFloor() - m2.getDestFloor();
		}
	}
	
	/*
	 * Comparator for sorting priority queue of non-priority mailitem
	 * Sort by destination floor (descending), arrival time (ascending)
	 * For case when delivering priority mailitem upstairs
	 * So non-priority mailitem can be deliver when downstairs
	 */
	private class floorCompareDe implements Comparator<MailItem> {
		public int compare(MailItem m1, MailItem m2) {
			if (m1.getDestFloor() == m2.getDestFloor()) {
				return m1.getArrivalTime() - m2.getArrivalTime();
			}
			return m2.getDestFloor() - m1.getDestFloor();
		}
	}
	
	/*
	 * Fill the tube with priority mailitem
	 * Check the weight of mailitem in case of robot dropping
	 * @param priorityTube priority queue for priority mailitem
	 * @param max maximum weight limit for robot to carry
	 */
	private void fillPriorityTube(LinkedList<MailItem> priorityTube, int max) {
		
		while (priorityTube.size() < MAX_TAKE && getPriorityPoolSize(max) > 0) {

			MailItem mail = getPriorityMail(max);
			
			// check the weight of each mailitem is satisfied, then add
			if (mail.getWeight() <= max) {
				priorityTube.add(mail);
			} else {
				addToPool(mail);
			}
		}
	}
	
	/**
	 * Fill the tube with non-priority mailitem
	 * Check the weight of mailitem in case of robot dropping
	 * @param priorityTube priority queue for priority mailitem
	 * @param nonPriorityTube priority queue for non-priority mailitem
	 * @param max maximum weight limit for robot ro carry
	 */
	private void fillNonPriorityTube(LinkedList<MailItem> priorityTube, 
		LinkedList<MailItem> nonPriorityTube, int max) {
		
		while ((nonPriorityTube.size() + priorityTube.size()) < MAX_TAKE && 
			getNonPriorityPoolSize(max) > 0) {
			
			MailItem mail = getNonPriorityMail(max);
			
			// check the weight of each mailitem is satisfied, then add
			if (mail.getWeight() <= max) {
				nonPriorityTube.add(mail);
			} else {
				addToPool(mail);
			}
		}
	}
	
	/**
	 * Sort both priority and non-priority tube in order
	 * Since StorageTube is stack structure (first in last out)
	 * Therefore reverse the order into stack to store
	 * @param priorityTube priority queue for priority mailitem
	 * @param nonPriorityTube priority queue for non-priority mailitem
	 * @return stack for both priority and non-priority mailitem sorted in order 
	 */
	private Stack<MailItem> sortTwoTube(LinkedList<MailItem> priorityTube, 
		LinkedList<MailItem> nonPriorityTube) {
		Boolean priorityExist = (priorityTube.isEmpty()) ? false : true;
		Boolean nonPriorityExist = (nonPriorityTube.isEmpty()) ? false : true;
		Stack<MailItem> reverse = new Stack<MailItem>();
		
		// priority mailitems exist case
		if (priorityExist) {
			Collections.sort(priorityTube, new floorComparePri());
			// both priority and non-priority mailitems exist case
			if (nonPriorityExist) {
				Collections.sort(nonPriorityTube, new floorCompareDe());
			}
		// only non-priority mailitems exist case
		} else {
			if (nonPriorityExist) {
				Collections.sort(nonPriorityTube, new floorCompareAs());
			}
		}
		
		// use a stack to record the reverse delivery order
		while (reverse.size() < MAX_TAKE) {
			if (!priorityTube.isEmpty()) {
				reverse.push(priorityTube.poll());
			} else if (!nonPriorityTube.isEmpty()) {
				reverse.push(nonPriorityTube.poll());
			} else {
				break;
			}
		}
		return reverse;
	}
	
    /**
     * Main function for filling StorageTube for different robot
     * Check the tube is empty before adding any mailitems
     * Using two LinkedList structure to get two types of mailitems
     * Take the most satisfied items into a reverse stack then in tube
     * Since tube is a stack structure (first in last out)
     * @param tube refers to the pack the robot uses to deliver mail.
     * @param strong is whether the tube belongs to a strong robot.
     */
	@Override
	public void fillStorageTube(StorageTube tube, boolean strong) {
		
		int max = strong ? Integer.MAX_VALUE : 2000;
		
		try {
			while(!tube.isEmpty()) {
				addToPool(tube.pop());
			}
			
			// initialist two LinkedList of tubes
			LinkedList<MailItem> priorityTube = new LinkedList<MailItem>();
			LinkedList<MailItem> nonPriorityTube = new LinkedList<MailItem>();

			// fill the priority tube first, then fill the non-priority tube
			fillPriorityTube(priorityTube, max);
			fillNonPriorityTube(priorityTube, nonPriorityTube, max);
			
			// use a stack to reverse the orders of mailitems
			Stack<MailItem> rev = sortTwoTube(priorityTube, nonPriorityTube);
			
			// fill the storage tube from a stack
			while (tube.getSize() < MAX_TAKE && rev.size() > 0) {
				tube.addItem(rev.pop());	
			}
			
		} catch (TubeFullException e) {
			e.printStackTrace();
		}
	}
}

