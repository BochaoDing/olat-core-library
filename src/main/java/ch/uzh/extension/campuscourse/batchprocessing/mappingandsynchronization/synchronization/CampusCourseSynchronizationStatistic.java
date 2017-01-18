package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.synchronization;

import ch.uzh.extension.campuscourse.service.synchronization.CampusCourseSynchronizationResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Martin Schraner
 */
public class CampusCourseSynchronizationStatistic {

	// Atomic integers for thread safety
	private final AtomicInteger addedCoaches = new AtomicInteger();
	private final AtomicInteger removedCoaches = new AtomicInteger();
	private final AtomicInteger addedParticipants = new AtomicInteger();
	private final AtomicInteger removedParticipants = new AtomicInteger();

	void addCampusCourseSynchronizationResults(List<CampusCourseSynchronizationResult> campusCourseSynchronizationResults) {
		updateAddedCoaches(campusCourseSynchronizationResults);
		updateRemovedCoaches(campusCourseSynchronizationResults);
		updateAddedParticipants(campusCourseSynchronizationResults);
		updateRemovedParticipants(campusCourseSynchronizationResults);
	}

	private void updateAddedCoaches(List<CampusCourseSynchronizationResult> campusCourseSynchronizationResults) {
		int numberToBeAdded = 0;
		for (CampusCourseSynchronizationResult campusCourseSynchronizationResult : campusCourseSynchronizationResults) {
			numberToBeAdded += campusCourseSynchronizationResult.getAddedCoaches();
		}
		if (numberToBeAdded != 0) {
			addedCoaches.getAndAdd(numberToBeAdded);
		}
	}

	private void updateRemovedCoaches(List<CampusCourseSynchronizationResult> campusCourseSynchronizationResults) {
		int numberToBeAdded = 0;
		for (CampusCourseSynchronizationResult campusCourseSynchronizationResult : campusCourseSynchronizationResults) {
			numberToBeAdded += campusCourseSynchronizationResult.getRemovedCoaches();
		}
		if (numberToBeAdded != 0) {
			removedCoaches.getAndAdd(numberToBeAdded);
		}
	}

	private void updateAddedParticipants(List<CampusCourseSynchronizationResult> campusCourseSynchronizationResults) {
		int numberToBeAdded = 0;
		for (CampusCourseSynchronizationResult campusCourseSynchronizationResult : campusCourseSynchronizationResults) {
			numberToBeAdded += campusCourseSynchronizationResult.getAddedParticipants();
		}
		if (numberToBeAdded != 0) {
			addedParticipants.getAndAdd(numberToBeAdded);
		}
	}

	private void updateRemovedParticipants(List<CampusCourseSynchronizationResult> campusCourseSynchronizationResults) {
		int numberToBeAdded = 0;
		for (CampusCourseSynchronizationResult campusCourseSynchronizationResult : campusCourseSynchronizationResults) {
			numberToBeAdded += campusCourseSynchronizationResult.getRemovedParticipants();
		}
		if (numberToBeAdded != 0) {
			removedParticipants.getAndAdd(numberToBeAdded);
		}
	}

	@Override
	public String toString() {
		return "added coaches: " + addedCoaches.get() +
				", removed coaches: " + removedCoaches.get() +
				", added participants: " + addedParticipants.get() +
				", removed participants: " + removedParticipants.get();
	}

	public AtomicInteger getAddedCoaches() {
		return addedCoaches;
	}

	public AtomicInteger getRemovedCoaches() {
		return removedCoaches;
	}

	public AtomicInteger getAddedParticipants() {
		return addedParticipants;
	}

	public AtomicInteger getRemovedParticipants() {
		return removedParticipants;
	}
}
