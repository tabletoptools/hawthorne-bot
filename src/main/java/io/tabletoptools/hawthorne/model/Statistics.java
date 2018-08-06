package io.tabletoptools.hawthorne.model;

public class Statistics {

    private Long eventCount = 0L;
    private Long messageCount = 0L;
    private Long presenceUpdateCount = 0L;
    private Long typingStartCount = 0L;

    public Long getEventCount() {
        return eventCount;
    }

    public void setEventCount(Long eventCount) {
        this.eventCount = eventCount;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public Long getPresenceUpdateCount() {
        return presenceUpdateCount;
    }

    public void setPresenceUpdateCount(Long presenceUpdateCount) {
        this.presenceUpdateCount = presenceUpdateCount;
    }

    public Long getTypingStartCount() {
        return typingStartCount;
    }

    public void setTypingStartCount(Long typingStartCount) {
        this.typingStartCount = typingStartCount;
    }

    public void bumpEventCount() {
        this.eventCount++;
    }

    public void bumpMessageCount() {
        this.messageCount++;
    }

    public void bumpPresenceUpdateCount() {
        this.presenceUpdateCount++;
    }

    public void bumpTypingStartCount() {
        this.typingStartCount++;
    }

    public void reset() {
        this.eventCount = 0L;
        this.messageCount = 0L;
        this.presenceUpdateCount = 0L;
        this.typingStartCount = 0L;
    }

}
