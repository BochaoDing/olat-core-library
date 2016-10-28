package ch.uzh.campus.service.core.impl.syncer.statistic;

import java.util.ArrayList;
import java.util.List;

/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 *
 * Initial Date: 25.06.2012 <br>
 * 
 * @author cg
 */
public class OverallSynchronizeStatistic {

    private List<SynchronizedGroupStatistic> courseStatisticList;

    public OverallSynchronizeStatistic() {
        courseStatisticList = new ArrayList<>();
    }

    public void add(SynchronizedGroupStatistic courseSynchronizeStatistic) {
        courseStatisticList.add(courseSynchronizeStatistic);
    }

    public String calculateOverallStatistic() {
        return "overallAddedCoaches=" + getAddedCoaches() + " , overallRemovedCoaches=" + getRemovedChoaches()
                + " ; overallAddedParticipants=" + getAddedParticipants() + " , overallRemovedParticipants=" + getRemovedParticipants();
    }

    private int getAddedCoaches() {
        int overallAddedCoaches = 0;
        for (SynchronizedGroupStatistic groupStatistic : courseStatisticList) {
            if (groupStatistic.getCoachGroupStatistic() != null) {
                overallAddedCoaches += groupStatistic.getCoachGroupStatistic().getAddedStatistic();
            }
        }
        return overallAddedCoaches;
    }

    private int getRemovedChoaches() {
        int overallRemovedOwners = 0;
        for (SynchronizedGroupStatistic groupStatistic : courseStatisticList) {
            if (groupStatistic.getCoachGroupStatistic() != null) {
                overallRemovedOwners += groupStatistic.getCoachGroupStatistic().getRemovedStatistic();
            }
        }
        return overallRemovedOwners;
    }

    private int getAddedParticipants() {
        int overallAddedParticipants = 0;
        for (SynchronizedGroupStatistic groupStatistic : courseStatisticList) {
            overallAddedParticipants += groupStatistic.getParticipantGroupStatistic().getAddedStatistic();
        }
        return overallAddedParticipants;
    }

    private int getRemovedParticipants() {
        int overallRemovedParticipants = 0;
        for (SynchronizedGroupStatistic groupStatistic : courseStatisticList) {
            overallRemovedParticipants += groupStatistic.getParticipantGroupStatistic().getRemovedStatistic();
        }
        return overallRemovedParticipants;
    }
}
