package app.openjfx.jclient.model;

public class PulsarBatchFactory {
    private final int messageTimeOut, queueSize, maxPendingMessages, delayMS;
    private final boolean blockQueueIfFull;

    public boolean isBlockQueueIfFull() {
        return blockQueueIfFull;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public PulsarBatchFactory(int messageTimeOut, int connectionTimeOut, int maxPendingMessages, int delayMS, boolean blockQueueIfFull) {
        this.messageTimeOut = messageTimeOut;
        this.queueSize = connectionTimeOut;
        this.maxPendingMessages = maxPendingMessages;
        this.delayMS = delayMS;
        this.blockQueueIfFull = blockQueueIfFull;
    }

    public int getMessageTimeOut() {
        return messageTimeOut;
    }

    public int getMaxPendingMessages() {
        return maxPendingMessages;
    }

    public int getDelayMS() {
        return delayMS;
    }

}
