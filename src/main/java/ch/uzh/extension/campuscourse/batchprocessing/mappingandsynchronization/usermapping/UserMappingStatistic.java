package ch.uzh.extension.campuscourse.batchprocessing.mappingandsynchronization.usermapping;

import ch.uzh.extension.campuscourse.service.usermapping.UserMappingResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Martin Schraner
 */
public class UserMappingStatistic {

	// Atomic integers for thread safety
	private final AtomicInteger alreadyMapped = new AtomicInteger();
	private final AtomicInteger newMappingByEmail = new AtomicInteger();
	private final AtomicInteger newMappingByMatriculationNumber = new AtomicInteger();
	private final AtomicInteger newMappingByPersonalNumber = new AtomicInteger();
	private final AtomicInteger newMappingByAdditionalPersonalNumber = new AtomicInteger();
	private final AtomicInteger couldBeMappedManually = new AtomicInteger();
	private final AtomicInteger couldNotMap = new AtomicInteger();

	void addUserMappingResults(List<UserMappingResult> userMappingResults) {
		updateCounterOfMappingType(userMappingResults, UserMappingResult.ALREADY_MAPPED, alreadyMapped);
		updateCounterOfMappingType(userMappingResults, UserMappingResult.NEW_MAPPING_BY_EMAIL, newMappingByEmail);
		updateCounterOfMappingType(userMappingResults, UserMappingResult.NEW_MAPPING_BY_MATRICULATION_NUMBER, newMappingByMatriculationNumber);
		updateCounterOfMappingType(userMappingResults, UserMappingResult.NEW_MAPPING_BY_PERSONAL_NUMBER, newMappingByPersonalNumber);
		updateCounterOfMappingType(userMappingResults, UserMappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NUMBER, newMappingByAdditionalPersonalNumber);
		updateCounterOfMappingType(userMappingResults, UserMappingResult.COULD_BE_MAPPED_MANUALLY, couldBeMappedManually);
		updateCounterOfMappingType(userMappingResults, UserMappingResult.COULD_NOT_MAP, couldNotMap);
	}

	private void updateCounterOfMappingType(List<UserMappingResult> userMappingResults, UserMappingResult mappingTypeToBeUpdated, AtomicInteger counterToBeUpdated) {
		int numberToBeAdded = 0;
		for (UserMappingResult userMappingResult : userMappingResults) {
			if (userMappingResult == mappingTypeToBeUpdated) {
				numberToBeAdded++;
			}
		}
		if (numberToBeAdded != 0) {
			// Thread safe update
			counterToBeUpdated.getAndAdd(numberToBeAdded);
		}
	}

	@Override
	public String toString() {
		return "already mapped: " + alreadyMapped.get() +
				", new mapping by email: " + newMappingByEmail.get() +
				", new mapping by matriculation number: " + newMappingByMatriculationNumber.get() +
				", new mapping by personal number: " + newMappingByPersonalNumber.get() +
				", new mapping by additional personal number: " + newMappingByAdditionalPersonalNumber.get() +
				", could be mapped manually: " + couldBeMappedManually.get() +
				", could not map: " + couldNotMap.get();
	}

	public AtomicInteger getAlreadyMapped() {
		return alreadyMapped;
	}

	public AtomicInteger getNewMappingByEmail() {
		return newMappingByEmail;
	}

	public AtomicInteger getNewMappingByMatriculationNumber() {
		return newMappingByMatriculationNumber;
	}

	public AtomicInteger getNewMappingByPersonalNumber() {
		return newMappingByPersonalNumber;
	}

	public AtomicInteger getNewMappingByAdditionalPersonalNumber() {
		return newMappingByAdditionalPersonalNumber;
	}

	public AtomicInteger getCouldBeMappedManually() {
		return couldBeMappedManually;
	}

	public AtomicInteger getCouldNotMap() {
		return couldNotMap;
	}
}
