package com.hearth.objects;

public class MissionLog {
    String memberUid;
    String missionTitle;
    long timestamp;

    public MissionLog() {
    }

    public MissionLog(String memberUid, String missionTitle, long timestamp) {
        this.memberUid = memberUid;
        this.missionTitle = missionTitle;
        this.timestamp = timestamp;
    }

    public String getMemberUid() {
        return memberUid;
    }

    public void setMemberUid(String memberUid) {
        this.memberUid = memberUid;
    }

    public String getMissionTitle() {
        return missionTitle;
    }

    public void setMissionTitle(String missionTitle) {
        this.missionTitle = missionTitle;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
